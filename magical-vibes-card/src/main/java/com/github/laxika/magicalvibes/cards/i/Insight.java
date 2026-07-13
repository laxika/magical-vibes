package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "75")
public class Insight extends Card {

    public Insight() {
        // Whenever an opponent casts a green spell, you draw a card.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.GREEN), List.of(new DrawCardEffect())));
    }
}
