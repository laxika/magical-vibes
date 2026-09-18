package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "228")
@CardRegistration(set = "A25", collectorNumber = "150")
public class SkirkCommando extends Card {

    public SkirkCommando() {
        addMorph("{2}{R}");
        target(new PermanentPredicateTargetFilter(new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(), new PermanentControlledByDefendingPlayerPredicate())),
                "Target must be a creature the damaged player controls"))
                .addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(
                        new DealDamageToTargetCreatureEffect(2),
                        "You may have Skirk Commando deal 2 damage to target creature that player controls."
                ));
    }
}
