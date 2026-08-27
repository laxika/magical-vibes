package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CollectEvidenceCostPaid;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongControllerCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "MKM", collectorNumber = "84")
public class ExtractAConfession extends Card {

    public ExtractAConfession() {
        addEffect(EffectSlot.SPELL, new CollectEvidenceCost(6, true));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new CollectEvidenceCostPaid(), new SacrificePermanentsEffect(1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasGreatestPowerAmongControllerCreaturesPredicate())),
                        SacrificeRecipient.EACH_OPPONENT)));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new CollectEvidenceCostPaid()),
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                        SacrificeRecipient.EACH_OPPONENT)));
    }
}
