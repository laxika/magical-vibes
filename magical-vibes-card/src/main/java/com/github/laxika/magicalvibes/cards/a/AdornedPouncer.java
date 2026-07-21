package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "HOU", collectorNumber = "2")
public class AdornedPouncer extends Card {

    public AdornedPouncer() {
        // Double strike is an auto-loaded keyword; no engine wiring needed here.

        // Eternalize {3}{W}{W} ({3}{W}{W}, Exile this card from your graveyard: Create a token that's a
        // copy of it, except it's a 4/4 black Zombie Cat with no mana cost. Eternalize only as a sorcery.)
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{W}{W}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenCopyOfSourceEffect(false, 1, CardColor.BLACK, CardSubtype.ZOMBIE, true, 4, 4)
                ),
                "Eternalize {3}{W}{W} ({3}{W}{W}, Exile this card from your graveyard: Create a token that's a "
                        + "copy of it, except it's a 4/4 black Zombie Cat with no mana cost. Eternalize only as a sorcery.)",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
