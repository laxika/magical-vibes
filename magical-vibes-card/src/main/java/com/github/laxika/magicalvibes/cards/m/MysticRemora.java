package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "ICE", collectorNumber = "87")
public class MysticRemora extends Card {

    public MysticRemora() {
        // Cumulative upkeep {1}
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new CumulativeUpkeepEffect("{1}"));

        // Whenever an opponent casts a noncreature spell, you may draw a card unless that player pays {4}.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new DrawCardUnlessPaysEffect(1, 4,
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE))));
    }
}
