package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EscalateManaCost;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MRD", collectorNumber = "74")
public class PromiseOfPower extends Card {

    public PromiseOfPower() {
        addEffect(EffectSlot.SPELL, new EscalateManaCost("{4}"));
        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "You draw five cards and you lose 5 life",
                        List.of(new DrawCardEffect(5), new LoseLifeEffect(5))),
                new ChooseOneEffect.ChooseOneOption(
                        "Create an X/X black Demon creature token with flying, where X is the number of cards in your hand",
                        new CreateTokenEffect(
                                "Demon",
                                new CardsInHand(CountScope.CONTROLLER),
                                new CardsInHand(CountScope.CONTROLLER),
                                CardColor.BLACK,
                                List.of(CardSubtype.DEMON),
                                Set.of(Keyword.FLYING),
                                Set.of()))
        )));
    }
}
