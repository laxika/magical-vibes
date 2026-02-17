package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

public class DemonsHorn extends Card {

    public DemonsHorn() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new MayEffect(new GainLifeOnColorSpellCastEffect(CardColor.BLACK, 1), "Gain 1 life?"));
    }
}
