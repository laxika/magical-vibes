package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageToCombatOpponentControllerEffect;

@CardRegistration(set = "CON", collectorNumber = "118")
public class Meglonoth extends Card {

    public Meglonoth() {
        // Vigilance and trample are auto-loaded from Scryfall.
        // Whenever this creature blocks a creature, it deals damage to that creature's controller
        // equal to this creature's power.
        addEffect(EffectSlot.ON_BLOCK, new DealDamageToCombatOpponentControllerEffect(new SourcePower()));
    }
}
