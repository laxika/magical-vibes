package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect;


@CardRegistration(set = "AKH", collectorNumber = "138")
public class HeartPiercerManticore extends Card {

    public HeartPiercerManticore() {
        // When this creature enters, you may sacrifice another creature. When you do, this creature
        // deals damage equal to that creature's power to any target.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect(),
                        "Sacrifice another creature?"));

        // Embalm {5}{R} ({5}{R}, Exile this card from your graveyard: Create a token that's a copy
        // of it, except it's a white Zombie Manticore with no mana cost. Embalm only as a sorcery.)
        addEmbalm("{5}{R}", "Manticore");
    }
}
