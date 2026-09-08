package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardTypesAmongSpellsCastThisTurn;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "TMT", collectorNumber = "29")
@CardRegistration(set = "TMT", collectorNumber = "227")
@CardRegistration(set = "TMT", collectorNumber = "282")
@CardRegistration(set = "TMT", collectorNumber = "292")
public class AprilONeilHacktivist extends Card {

    public AprilONeilHacktivist() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new DrawCardEffect(new CardTypesAmongSpellsCastThisTurn()));
    }
}
