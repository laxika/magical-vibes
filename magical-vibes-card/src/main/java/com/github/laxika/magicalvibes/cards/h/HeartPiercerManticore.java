package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnotherCreatureDealPowerDamageToAnyTargetEffect;

import java.util.List;

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
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{5}{R}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopyOfSourceEffect(false, 1, CardColor.WHITE, CardSubtype.ZOMBIE, true)
                ),
                "Embalm {5}{R} ({5}{R}, Exile this card from your graveyard: Create a token that's a copy of it, "
                        + "except it's a white Zombie Manticore with no mana cost. Embalm only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
