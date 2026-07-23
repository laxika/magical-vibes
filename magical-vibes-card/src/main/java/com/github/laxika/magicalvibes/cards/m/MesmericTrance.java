package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "83")
public class MesmericTrance extends Card {

    public MesmericTrance() {
        // Cumulative upkeep {1}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{1}"));

        // {U}, Discard a card: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new DrawCardEffect(1)
                ),
                "{U}, Discard a card: Draw a card."
        ));
    }
}
