package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongControllerCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "VOC", collectorNumber = "22")
@CardRegistration(set = "VOC", collectorNumber = "60")
public class ShadowgrangeArchfiend extends Card {

    public ShadowgrangeArchfiend() {
        var greatestPowerCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasGreatestPowerAmongControllerCreaturesPredicate()));

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new SacrificePermanentsEffect(1, greatestPowerCreature, SacrificeRecipient.EACH_OPPONENT)
                        .withRecordedSacrificedPower());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(new EventValue()));

        addCastingOption(new MadnessCast(List.of(
                new ManaCastingCost("{2}{B}"),
                new LifeCastingCost(8))));
    }
}
