package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "287")
public class QuirionDryad extends Card {

    public QuirionDryad() {
        addEffect(EffectSlot.ON_ANY_PLAYER_CASTS_SPELL,
                new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                        Set.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED),
                        1,
                        true
                ));
    }
}
