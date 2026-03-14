package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;

import java.util.Set;

@CardRegistration(set = "M10", collectorNumber = "194")
public class MoldAdder extends Card {

    public MoldAdder() {
        // Whenever an opponent casts a blue or black spell, you may put a +1/+1 counter on Mold Adder.
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL,
                new MayEffect(
                        new PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect(
                                Set.of(CardColor.BLUE, CardColor.BLACK),
                                1,
                                false
                        ),
                        "Put a +1/+1 counter on Mold Adder?"
                ));
    }
}
