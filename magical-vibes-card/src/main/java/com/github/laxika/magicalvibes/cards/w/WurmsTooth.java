package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "346")
public class WurmsTooth extends Card {

    public WurmsTooth() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new MayEffect(new GainLifeOnColorSpellCastEffect(CardColor.GREEN, 1), "Gain 1 life?"));
    }
}
