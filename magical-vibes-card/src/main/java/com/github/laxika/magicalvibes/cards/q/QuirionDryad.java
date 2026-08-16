package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;

import java.util.Set;

@CardRegistration(set = "10E", collectorNumber = "287")
@CardRegistration(set = "M13", collectorNumber = "184")
@CardRegistration(set = "M21", collectorNumber = "198")
public class QuirionDryad extends Card {

    public QuirionDryad() {
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                        Set.of(CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED),
                        1,
                        true
                ));
    }
}
