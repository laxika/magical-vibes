package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;

@CardRegistration(set = "WTH", collectorNumber = "26")
public class SoulShepherd extends Card {

    public SoulShepherd() {
        // {W}, Exile a creature card from your graveyard: You gain 1 life.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(new ExileCardFromGraveyardCost(CardType.CREATURE), new GainLifeEffect(1)),
                "{W}, Exile a creature card from your graveyard: You gain 1 life."
        ));
    }
}
