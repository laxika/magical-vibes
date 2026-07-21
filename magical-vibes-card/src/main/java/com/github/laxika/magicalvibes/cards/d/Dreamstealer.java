package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "63")
public class Dreamstealer extends Card {

    public Dreamstealer() {
        // Menace is an auto-loaded keyword; no engine wiring needed here.

        // Whenever Dreamstealer deals combat damage to a player, that player discards that many cards.
        // EventValue reads the combat damage dealt, wired onto the trigger by CombatDamageService.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DiscardEffect(new EventValue(), DiscardRecipient.TARGET_PLAYER));

        // Eternalize {4}{B}{B} ({4}{B}{B}, Exile this card from your graveyard: Create a token that's a
        // copy of it, except it's a 4/4 black Zombie Human Wizard with no mana cost. Eternalize only as a sorcery.)
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{4}{B}{B}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopyOfSourceEffect(false, 1, CardColor.BLACK, CardSubtype.ZOMBIE, true, 4, 4)
                ),
                "Eternalize {4}{B}{B} ({4}{B}{B}, Exile this card from your graveyard: Create a token that's a "
                        + "copy of it, except it's a 4/4 black Zombie Human Wizard with no mana cost. Eternalize only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
