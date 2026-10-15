package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToControllerPerClericEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MOR", collectorNumber = "2")
public class BattletideAlchemist extends Card {

    public BattletideAlchemist() {
        // "If a source would deal damage to a player, you may prevent X of that damage,
        // where X is the number of Clerics you control."
        addEffect(EffectSlot.STATIC, new PreventDamageToControllerPerClericEffect());
    }
}
