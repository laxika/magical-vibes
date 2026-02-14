package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

public class AngelsFeather extends Card {

    public AngelsFeather() {
        super("Angel's Feather", CardType.ARTIFACT, "{2}", null);

        setCardText("Whenever a player casts a white spell, you may gain 1 life.");
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL, new MayEffect(new GainLifeOnColorSpellCastEffect(CardColor.WHITE, 1), "Gain 1 life?"));
    }
}
