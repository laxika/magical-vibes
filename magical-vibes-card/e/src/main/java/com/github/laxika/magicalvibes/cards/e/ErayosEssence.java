package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterOpponentFirstSpellEachTurnEffect;

public class ErayosEssence extends Card {

    public ErayosEssence() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new CounterOpponentFirstSpellEachTurnEffect.Marker());
    }
}
