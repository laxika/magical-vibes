package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.h.HakkaWhisperingRaven;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.ForetoldCardsInExile;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsOfChosenTypeToHandRestToBottomEffect;

import java.util.List;

@CardRegistration(set = "KHM", collectorNumber = "40")
public class AlrundGodOfTheCosmos extends Card {

    public AlrundGodOfTheCosmos() {
        setBackFaceCard(new HakkaWhisperingRaven());
        setModalDoubleFaced(true);

        var count = new Sum(
                new CardsInHand(CountScope.CONTROLLER),
                new ForetoldCardsInExile(CountScope.CONTROLLER));
        addEffect(EffectSlot.STATIC, new BoostSelfEffect(count, count));
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new RevealTopCardsOfChosenTypeToHandRestToBottomEffect(2));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption("Alrund, God of the Cosmos", List.of())
                        .withManaCost("{3}{U}{U}"),
                new ChooseOneEffect.ChooseOneOption("Hakka, Whispering Raven", List.of())
                        .withManaCost("{1}{U}")
        )));
    }

    @Override
    public String getBackFaceClassName() {
        return "HakkaWhisperingRaven";
    }
}
