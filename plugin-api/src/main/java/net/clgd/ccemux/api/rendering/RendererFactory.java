package net.clgd.ccemux.api.rendering;

import java.util.HashMap;
import java.util.Map;

import net.clgd.ccemux.api.emulation.EmuConfig;
import net.clgd.ccemux.api.emulation.EmulatedComputer;

/**
 * A factory used to create a renderer for a given computer
 *
 * @param <T>
 *            The type of renderer created by this factory
 */
@FunctionalInterface
public interface RendererFactory<T extends Renderer> {
	/**
	 * A map of names to renderer factories
	 */
	public static final Map<String, RendererFactory<?>> implementations = new HashMap<>();

	/**
	 * Creates a renderer for the given computer and config
	 */
	public T create(EmulatedComputer computer, EmuConfig cfg);

	/**
	 * Creates a config editor window for the given config. Returns true if
	 * successful. If unsuccesful or unsupported, <code>false</code> should be
	 * returned.<br>
	 * <br>
	 * The default implementation returns false and has no side effects.
	 * 
	 * @param config
	 *            The config to let the user edit
	 * @return Whether an editor window was opened
	 */
	default boolean createConfigEditor(EmuConfig config) {
		return false;
	}
}