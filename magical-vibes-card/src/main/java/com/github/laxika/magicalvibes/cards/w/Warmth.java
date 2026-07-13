package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "52")
public class Warmth extends Card {

    public Warmth() {
        // Whenever an opponent casts a red spell, you gain 2 life.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new SpellCastTriggerEffect(new CardColorPredicate(CardColor.RED), List.of(new GainLifeEffect(2))));
    }
}
