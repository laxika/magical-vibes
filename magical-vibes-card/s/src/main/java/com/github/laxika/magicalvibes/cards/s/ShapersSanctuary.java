package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "XLN", collectorNumber = "206")
public class ShapersSanctuary extends Card {

    public ShapersSanctuary() {
        // Whenever a creature you control becomes the target of a spell or ability
        // an opponent controls, you may draw a card.
        addEffect(EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
