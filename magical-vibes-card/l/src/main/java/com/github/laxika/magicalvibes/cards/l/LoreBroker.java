package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;

import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "RAV", collectorNumber = "57")
public class LoreBroker extends Card {

    public LoreBroker() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DrawCardEffect(1, Subjects.eachPlayer()), new DiscardEffect(1, Subjects.eachPlayer())),
                "{T}: Each player draws a card, then discards a card."));
    }
}
