package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCanDrawOnlyOneCardEachTurnEffect;

@CardRegistration(set = "UMA", collectorNumber = "202")
public class LeovoldEmissaryOfTrest extends Card {

    public LeovoldEmissaryOfTrest() {
        addEffect(EffectSlot.STATIC, new OpponentsCanDrawOnlyOneCardEachTurnEffect());
        addEffect(EffectSlot.ON_ALLY_PERMANENT_OR_PLAYER_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
                new MayEffect(new DrawCardEffect(), "Draw a card?"));
    }
}
