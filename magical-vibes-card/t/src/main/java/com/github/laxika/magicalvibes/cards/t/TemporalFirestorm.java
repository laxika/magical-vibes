package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutUpToNControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;

import java.util.List;

@CardRegistration(set = "DMU", collectorNumber = "147")
public class TemporalFirestorm extends Card {

    public TemporalFirestorm() {
        addEffect(EffectSlot.SPELL, new RepeatableAdditionalManaCost(
                List.of("{1}{W}", "{1}{U}"), true, 1));
        addEffect(EffectSlot.SPELL, new PhaseOutUpToNControlledPermanentsEffect(
                new Sum(new RepeatedAdditionalCostCount("{1}{W}"),
                        new RepeatedAdditionalCostCount("{1}{U}")),
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()))));
        addEffect(EffectSlot.SPELL, new MassDamageEffect(5, false, false, true, null));
    }
}
