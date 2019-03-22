package org.rewedigital.katana.dsl.compact

import org.rewedigital.katana.Component
import org.rewedigital.katana.Declaration.Type
import org.rewedigital.katana.Module
import org.rewedigital.katana.dsl.ProviderDsl
import org.rewedigital.katana.dsl.internal.moduleDeclaration

/**
 * Declares a dependency binding.
 * A new instance will be created every time the dependency is requested.
 *
 * @param name Optional name of binding
 * @param internal If `true` binding is only available in current module
 * @param body Body of binding declaration
 *
 * @see Module.singleton
 * @see Module.eagerSingleton
 */
inline fun <reified T> Module.factory(name: String? = null,
                                      internal: Boolean = false,
                                      crossinline body: ProviderDsl.() -> T) =
    moduleDeclaration(
        module = this,
        clazz = T::class.java,
        name = name,
        internal = internal,
        type = Type.FACTORY,
        provider = { context -> body.invoke(ProviderDsl(context)) }
    )

/**
 * Declares a dependency binding as a singleton.
 * Only one instance (per component) will be created.
 *
 * @param name Optional name of binding
 * @param internal If `true` binding is only available in current module
 * @param body Body of binding declaration
 *
 * @see Module.factory
 * @see Module.eagerSingleton
 */
inline fun <reified T> Module.singleton(name: String? = null,
                                        internal: Boolean = false,
                                        crossinline body: ProviderDsl.() -> T) =
    moduleDeclaration(
        module = this,
        clazz = T::class.java,
        name = name,
        internal = internal,
        type = Type.SINGLETON,
        provider = { context -> body.invoke(ProviderDsl(context)) }
    )

/**
 * Declares a dependency binding as a eager singleton.
 * Only once instance (per component) will be created.
 * The instance will be created when the [Component] is created and not lazily the first time it's requested.
 *
 * @param name Optional name of binding
 * @param internal If `true` binding is only available in current module
 * @param body Body of binding declaration
 *
 * @see Module.factory
 * @see Module.singleton
 */
inline fun <reified T> Module.eagerSingleton(name: String? = null,
                                             internal: Boolean = false,
                                             crossinline body: ProviderDsl.() -> T) =
    moduleDeclaration(
        module = this,
        clazz = T::class.java,
        name = name,
        internal = internal,
        type = Type.EAGER_SINGLETON,
        provider = { context -> body.invoke(ProviderDsl(context)) }
    )