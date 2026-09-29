package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that makes creatures in the selected scope every creature type. The own-creatures
 * mode also applies to that controller's creature spells and creature cards they own outside the
 * battlefield; the self mode applies to the source card in every zone. The optional filter narrows
 * the affected battlefield permanents.
 */
public record GrantAllCreatureTypesToOwnCreaturesEffect(GrantScope scope, PermanentPredicate filter)
        implements CardEffect {

    public GrantAllCreatureTypesToOwnCreaturesEffect() {
        this(GrantScope.OWN_CREATURES, null);
    }

    public GrantAllCreatureTypesToOwnCreaturesEffect(GrantScope scope) {
        this(scope, null);
    }

    public static GrantAllCreatureTypesToOwnCreaturesEffect toSelf() {
        return new GrantAllCreatureTypesToOwnCreaturesEffect(GrantScope.SELF, null);
    }
}
