package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInLibrary;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

@CardRegistration(set = "ZNR", collectorNumber = "67")
public class MaddeningCacophony extends Card {

    public MaddeningCacophony() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{3}{U}"));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Kicked(),
                new MillEffect(8, MillRecipient.EACH_OPPONENT),
                new MillEffect(
                        new HalvedRoundedUp(new CardsInLibrary(CountScope.OPPONENTS)),
                        MillRecipient.EACH_OPPONENT)
        ));
    }
}
