package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageAndDefendingPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "151")
public class CloakOfConfusion extends Card {

    public CloakOfConfusion() {
        // Enchant creature you control
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentControlledBySourceControllerPredicate()
                )),
                "Target must be a creature you control"
        ))
                // Whenever enchanted creature attacks and isn't blocked, you may have it assign no
                // combat damage this turn. If you do, defending player discards a card at random.
                .addEffect(EffectSlot.ON_ENCHANTED_CREATURE_ATTACKS_UNBLOCKED,
                        new MayEffect(new AssignNoCombatDamageAndDefendingPlayerDiscardsEffect(),
                                "have it assign no combat damage this turn?"));
    }
}
