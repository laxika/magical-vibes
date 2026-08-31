package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "34")
public class Fascination extends Card {

    public Fascination() {
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Each player draws X cards.",
                        new EachPlayerDrawsCardEffect(new XValue())),
                new ChooseOneEffect.ChooseOneOption(
                        "Each player mills X cards.",
                        List.of(
                                new MillEffect(new XValue(), MillRecipient.CONTROLLER),
                                new MillEffect(new XValue(), MillRecipient.EACH_OPPONENT)))
        )));
    }
}
