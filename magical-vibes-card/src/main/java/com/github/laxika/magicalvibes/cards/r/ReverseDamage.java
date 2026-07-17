package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageFromChosenSourceEffect;

@CardRegistration(set = "9ED", collectorNumber = "35")
@CardRegistration(set = "5ED", collectorNumber = "55")
@CardRegistration(set = "7ED", collectorNumber = "34")
@CardRegistration(set = "6ED", collectorNumber = "39")
public class ReverseDamage extends Card {

    public ReverseDamage() {
        addEffect(EffectSlot.SPELL, new PreventNextDamageFromChosenSourceEffect(true));
    }
}
