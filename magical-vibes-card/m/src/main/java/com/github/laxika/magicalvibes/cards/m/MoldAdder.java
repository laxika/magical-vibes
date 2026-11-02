package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnSourceOnColorSpellCastEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "M10", collectorNumber = "194")
public class MoldAdder extends Card {

    public MoldAdder() {
        // Whenever an opponent casts a blue or black spell, you may put a +1/+1 counter on Mold Adder.
        addTrigger(Triggers.opponentCastsSpell(),
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
