package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "AKH", collectorNumber = "36")
public class UnwaveringInitiate extends Card {

    public UnwaveringInitiate() {
        // Vigilance is an auto-loaded keyword; no engine wiring needed here.

        // Embalm {4}{W} ({4}{W}, Exile this card from your graveyard: Create a token that's a copy of it,
        // except it's a white Zombie Human Warrior with no mana cost. Embalm only as a sorcery.)
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopyOfSourceEffect(false, 1, CardColor.WHITE, CardSubtype.ZOMBIE, true)
                ),
                "Embalm {4}{W} ({4}{W}, Exile this card from your graveyard: Create a token that's a copy of it, "
                        + "except it's a white Zombie Human Warrior with no mana cost. Embalm only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
