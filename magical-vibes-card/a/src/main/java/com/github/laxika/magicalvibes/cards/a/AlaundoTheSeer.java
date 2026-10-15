package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromHandWithManaValueTimeCountersEffect;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "230")
public class AlaundoTheSeer extends Card {

    public AlaundoTheSeer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new DrawCardEffect(1), new ExileCardFromHandWithManaValueTimeCountersEffect()),
                "{T}: Draw a card, then exile a card from your hand and put a number of time counters on it equal to its mana value. It gains \"When the last time counter is removed from this card, if it's exiled, you may cast it without paying its mana cost. If you cast a creature spell this way, it gains haste until end of turn.\" Then remove a time counter from each other card you own in exile."
        ));
    }
}
