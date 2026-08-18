package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EpicEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOK", collectorNumber = "129")
public class EndlessSwarm extends Card {

    public EndlessSwarm() {
        addEffect(EffectSlot.SPELL, new CreateTokenEffect(
                new CardsInHand(CountScope.CONTROLLER), "Snake", 1, 1,
                CardColor.GREEN, List.of(CardSubtype.SNAKE), Set.of(), Set.of()));
        addEffect(EffectSlot.SPELL, new EpicEffect());
    }
}
