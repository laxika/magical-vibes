package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/**
 * "[This creature] fights another target creature that player controls": the source fights a target creature, other
 * than itself, controlled by the player the ability is about (the stack entry's triggering player: the player a
 * preceding action of the resolution that made this reflexive ability chose).
 */
public record SourceFightsTargetCreatureControlledByPlayerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature(), new PermanentAllOfPredicate(List.of(
                new PermanentControlledByTriggeringPlayerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()))));
    }
}
