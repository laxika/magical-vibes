package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "M21", collectorNumber = "68")
public class SanctumOfCalmWaters extends Card {

    public SanctumOfCalmWaters() {
        PermanentCount shrinesYouControl = new PermanentCount(
                new PermanentHasSubtypePredicate(CardSubtype.SHRINE), CountScope.CONTROLLER);

        addEffect(EffectSlot.PRECOMBAT_MAIN_TRIGGERED,
                new MayEffect(
                        SequenceEffect.of(
                                new DrawCardEffect(shrinesYouControl),
                                new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                        "Draw cards equal to the number of Shrines you control?"));
    }
}
