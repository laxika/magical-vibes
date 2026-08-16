package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ThenEffectRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueAtMostControlledCountPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "11")
public class LayDownArms extends Card {

    public LayDownArms() {
        PermanentAllOfPredicate targetFilter = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentManaValueAtMostControlledCountPredicate(
                        new PermanentHasSubtypePredicate(CardSubtype.PLAINS))));
        target(new PermanentPredicateTargetFilter(
                targetFilter,
                "Target creature's mana value must be at most the number of Plains you control."
        )).addEffect(EffectSlot.SPELL, new ExileTargetPermanentThenEffect(
                new GainLifeEffect(3), ThenEffectRecipient.TARGET_CONTROLLER));
    }
}
