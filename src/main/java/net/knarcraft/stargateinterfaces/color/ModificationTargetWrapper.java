package net.knarcraft.stargateinterfaces.color;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ModificationTargetWrapper<T> {

    private final T target;

    public static ModificationTargetWrapper<?> createFromString(String string) {
        Material possibleMaterial = findMaterial(string);
        if (possibleMaterial != null) {
            return new ModificationTargetWrapper<>(possibleMaterial);
        }
        return new ModificationTargetWrapper<>(string);
    }

    private static Material findMaterial(String materialString) {
        try {
            return Material.valueOf(materialString);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public ModificationTargetWrapper(@NotNull T target) {
        this.target = Objects.requireNonNull(target);
    }

    public boolean isOfTarget(Object other) {
        return target.equals(other);
    }

    public String getTargetString() {
        if (target instanceof Enum<?> targetEnum) {
            return targetEnum.toString();
        }
        if (target instanceof String allTarget) {
            return allTarget;
        }
        throw new UnsupportedOperationException("The type of this target is unsupported");
    }

    @Override
    public boolean equals(Object other) {
        if(other == this){
            return true;
        }
        if (other instanceof ModificationTargetWrapper<?> modificationTargetWrapper) {
            return target.equals(modificationTargetWrapper.target);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }
}
