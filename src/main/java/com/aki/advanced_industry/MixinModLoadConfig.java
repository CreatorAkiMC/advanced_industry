package com.aki.advanced_industry;

import net.minecraftforge.common.ForgeVersion;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@IFMLLoadingPlugin.MCVersion(ForgeVersion.mcVersion)
@IFMLLoadingPlugin.SortingIndex(1007)
public class MixinModLoadConfig implements IFMLLoadingPlugin {
    public static List<String> LateMixinMods = Arrays.asList(
            "mixins.vanillafixer.json");

    /**
     * Vanillaの変更
     * [modeloader] など、初期に実行するものは、jsonの[ "target" ]を [ "@env(INIT)" ]にしないといけない
     * GameSetting や RayTracing など、
     * */
    public List<String> MixinFiles = Arrays.asList(
            "mixins.advanced_industry.json",
            "mixins.modloaderfix.json"
    );

    public MixinModLoadConfig() {
        MixinBootstrap.init();
        for(String fileName : MixinFiles) {
            Mixins.addConfiguration(fileName);
        }
        //.addConfiguration("mixins." +  + ".json");
    }

   /* private static void fixMixinClasspathOrder() {
        // Move VanillaFix jar up in the classloader's URLs to make sure that the
        // latest version of Mixin is used (to avoid having to rename 'VanillaFix.jar'
        // to 'aaaVanillaFix.jar')
        URL url = MixinModLoadConfig.class.getProtectionDomain().getCodeSource().getLocation();
        givePriorityInClasspath(url, Launch.classLoader);
        givePriorityInClasspath(url, (URLClassLoader) ClassLoader.getSystemClassLoader());
    }*/

    /*private static void givePriorityInClasspath(URL url, URLClassLoader classLoader) {
        try {
            Field ucpField = URLClassLoader.class.getDeclaredField("ucp");
            ucpField.setAccessible(true);

            List<URL> urls = new ArrayList<>(Arrays.asList(classLoader.getURLs()));
            urls.remove(url);
            urls.add(0, url);
            URLClassPath ucp = new URLClassPath(urls.toArray(new URL[0]));

            ucpField.set(classLoader, ucp);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }*/


    @Override
    public String[] getASMTransformerClass() {
        return new String[]{};
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
        if (Boolean.FALSE.equals(data.get("runtimeDeobfuscationEnabled"))) {
            MixinBootstrap.init();
            MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
            CoreModManager.getReparseableCoremods().removeIf(s -> StringUtils.containsIgnoreCase(s, "mcutils"));
        }
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
