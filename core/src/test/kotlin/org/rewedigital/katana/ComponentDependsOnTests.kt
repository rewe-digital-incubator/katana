package org.rewedigital.katana

import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldThrow
import org.rewedigital.katana.dsl.factory
import org.rewedigital.katana.dsl.get
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object ComponentDependsOnTests : Spek(
    {
        describe("Injection with multiple components") {

            it("should inject dependencies") {
                val module1 = Module {

                    factory { "Hello world" }

                    factory("another") { "Hello world 2" }
                }

                val component1 = Component(module1)

                val module2 = Module {

                    factory { 1337 }
                }

                val component2 = Component(module2)

                val module3 = Module {

                    factory { MyComponentC<String, Int>(get(), get()) }
                }

                val component3 = Component(
                    modules = listOf(module3),
                    dependsOn = listOf(component1, component2)
                )

                component3.canInject<MyComponentC<String, Int>>() shouldBeEqualTo true
                component3.canInject<Int>() shouldBeEqualTo true
                component3.canInject<String>() shouldBeEqualTo true
                component3.canInject<String>("another") shouldBeEqualTo true
                component3.canInject<MyComponent>() shouldBeEqualTo false

                val myComponent: MyComponentC<String, Int> by component3.inject()

                myComponent.value shouldBeEqualTo "Hello world"
                myComponent.value2 shouldBeEqualTo 1337
            }

            it("should inject dependencies over multiple component tiers") {

                val module1 = Module {

                    factory { "Hello world" }
                }

                val component1 = Component(module1)

                val component2 = Component(component1)

                val module3 = Module {

                    factory { 1337 }
                }

                val component3 = Component(
                    modules = listOf(module3),
                    dependsOn = listOf(component2)
                )

                component3.canInject<String>() shouldBeEqualTo true
                component3.canInject<Int>() shouldBeEqualTo true
                val string: String by component3.inject()
                val int: Int by component3.inject()

                string shouldBeEqualTo "Hello world"
                int shouldBeEqualTo 1337
            }

            it("should throw override exception for overrides in components") {

                val module1 = Module {

                    factory<MyComponent> { MyComponentA() }
                }

                val component1 = Component(module1)

                val module2 = Module("module1") {

                    factory<MyComponent> { MyComponentA() }
                }

                val component2 = Component(module2)

                val module3 = Module {

                    factory { "Hello world" }
                }

                val fn = {
                    Component(
                        modules = listOf(module3),
                        dependsOn = listOf(component1, component2)
                    )
                }

                fn shouldThrow OverrideException::class
            }

            it("should inject null values over multiple component tiers") {

                val module = Module {

                    factory<MyComponent?> { null }
                }

                val component = Component(module)
                val component2 = Component(component)

                val injection: MyComponent? = component2.injectNow()

                injection shouldBeEqualTo null
            }

            it("should not inject internal bindings over multiple component tiers") {
                val module = Module {

                    factory(name = "internal", internal = true) { "Hello world" }

                    factory<MyComponent> { MyComponentB<String>(get("internal")) }
                }

                val component = Component(module)
                val component2 = Component(component)

                component2.canInject<String>("internal") shouldBeEqualTo false
                component2.canInject<MyComponent>() shouldBeEqualTo true

                component2.injectNow<MyComponent>() shouldBeInstanceOf MyComponentB::class

                val fn = {
                    component2.injectNow<String>("internal")
                }

                fn shouldThrow InjectionException::class
            }

            it("plus operator should work as expected") {

                val module1 = Module {

                    factory { "Hello world" }
                }

                val module2 = Module {

                    factory { 1234 }
                }

                val module3 = Module {

                    factory("NAME") { 4321 }
                }

                val module4 = Module {

                    factory("NAME2") { 1337 }
                }

                val component1 = Component(module1)
                val component2 = component1 + listOf(module2)
                val component3 = component1 + module2

                component2.injectNow<String>() shouldBeEqualTo "Hello world"
                component2.injectNow<Int>() shouldBeEqualTo 1234

                component3.injectNow<String>() shouldBeEqualTo "Hello world"
                component3.injectNow<Int>() shouldBeEqualTo 1234

                val component4 = Component(module3)
                val component5 = listOf(component2, component4) + listOf(module4)
                val component6 = listOf(component2, component4) + module4

                component5.injectNow<String>() shouldBeEqualTo "Hello world"
                component5.injectNow<Int>() shouldBeEqualTo 1234
                component5.injectNow<Int>("NAME") shouldBeEqualTo 4321
                component5.injectNow<Int>("NAME2") shouldBeEqualTo 1337

                component6.injectNow<String>() shouldBeEqualTo "Hello world"
                component6.injectNow<Int>() shouldBeEqualTo 1234
                component6.injectNow<Int>("NAME") shouldBeEqualTo 4321
                component6.injectNow<Int>("NAME2") shouldBeEqualTo 1337
            }

            // TODO: Fix this
            xit("should allow \"empty\" component when it only has transitive dependencies") {

                val module = Module {

                    factory { "Hello world" }
                }

                val component = Component(module)
                val component2 = Component(component)
                val component3 = Component(component2)

                val injection: String = component3.injectNow()

                injection shouldBeEqualTo "Hello world"
            }
        }
    })
