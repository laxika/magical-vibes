package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "117")
public class PutridWarrior extends Card {

    public PutridWarrior() {
        addEffect(EffectSlot.ON_SELF_DEALS_DAMAGE, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Each player loses 1 life.",
                        new LoseLifeEffect(1, LoseLifeRecipient.EACH_PLAYER)),
                new ChooseOneEffect.ChooseOneOption(
                        "Each player gains 1 life.",
                        SequenceEffect.of(
                                new GainLifeEffect(1),
                                new GainLifeEffect(new Fixed(1), GainLifeRecipient.OPPONENT)))
        )));
    }
}
