package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect that makes creatures in the selected scope every creature type. The own-creatures
 * mode also applies to that controller's creature spells and creature cards they own outside the
 * battlefield; the self mode applies to the source card in every zone.
 */
public record GrantAllCreatureTypesToOwnCreaturesEffect(GrantScope scope) implements CardEffect {

    public GrantAllCreatureTypesToOwnCreaturesEffect() {
        this(GrantScope.OWN_CREATURES);
    }

    public static GrantAllCreatureTypesToOwnCreaturesEffect toSelf() {
        return new GrantAllCreatureTypesToOwnCreaturesEffect(GrantScope.SELF);
    }
}
