package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SpellCastLifeDrainEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

@CardRegistration(set = "7ED", collectorNumber = "171")
public class YawgmothsEdict extends Card {

    public YawgmothsEdict() {
        // Whenever an opponent casts a white spell, that player loses 1 life and you gain 1 life.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new SpellCastLifeDrainEffect(1, 1, new CardColorPredicate(CardColor.WHITE)));
    }
}
