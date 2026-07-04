package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "SOS", collectorNumber = "163")
public class TenuredConcocter extends Card {

    public TenuredConcocter() {
        // Vigilance is auto-loaded from Scryfall.
        // Whenever this creature becomes the target of a spell or ability an opponent controls,
        // you may draw a card.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new MayEffect(new DrawCardEffect(), "Draw a card?"));

        // Infusion — This creature gets +2/+0 as long as you gained life this turn.
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new GainedLifeThisTurn(),
                new StaticBoostEffect(2, 0, GrantScope.SELF)));
    }
}
