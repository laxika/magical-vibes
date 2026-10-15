package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TimesSourceMutated;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

@CardRegistration(set = "IKO", collectorNumber = "93")
public class InsatiableHemophage extends Card {

    public InsatiableHemophage() {
        addEffect(EffectSlot.ON_SELF_MUTATES, SequenceEffect.of(
                new LoseLifeEffect(new TimesSourceMutated(), LoseLifeRecipient.EACH_OPPONENT),
                new GainLifeEffect(new TimesSourceMutated())));
    }
}
