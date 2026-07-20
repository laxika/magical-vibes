package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "60")
public class LabyrinthGuardian extends Card {

    public LabyrinthGuardian() {
        // When this creature becomes the target of a spell, sacrifice it.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL, new SacrificeSelfEffect());

        // Embalm {3}{U} ({3}{U}, Exile this card from your graveyard: Create a token that's a copy
        // of it, except it's a white Zombie Illusion Warrior with no mana cost. Embalm only as a sorcery.)
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopyOfSourceEffect(false, 1, CardColor.WHITE, CardSubtype.ZOMBIE, true)
                ),
                "Embalm {3}{U} ({3}{U}, Exile this card from your graveyard: Create a token that's a copy of it, "
                        + "except it's a white Zombie Illusion Warrior with no mana cost. Embalm only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
