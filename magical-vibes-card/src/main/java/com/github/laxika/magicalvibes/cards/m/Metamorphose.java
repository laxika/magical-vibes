package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentMayPlayCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

@CardRegistration(set = "SCG", collectorNumber = "40")
public class Metamorphose extends Card {

    public Metamorphose() {
        target(new PermanentPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate()),
                "Target must be a permanent an opponent controls"
        )).addEffect(EffectSlot.SPELL, new PutTargetOnTopOfLibraryEffect())
                .addEffect(EffectSlot.SPELL,
                        new OpponentMayPlayCreatureEffect(
                                new CardIsPermanentPredicate(),
                                "artifact, creature, enchantment, or land"));
    }
}
