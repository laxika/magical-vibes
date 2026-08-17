package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "112")
public class GhoulcallersAccomplice extends Card {

    public GhoulcallersAccomplice() {
        // {3}{B}, Exile this card from your graveyard: Create a 2/2 black Zombie creature token.
        // Activate only as a sorcery.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{B}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenEffect(1, "Zombie", 2, 2,
                                CardColor.BLACK, List.of(CardSubtype.ZOMBIE), Set.of(), Set.of())
                ),
                "{3}{B}, Exile this card from your graveyard: Create a 2/2 black Zombie creature token. "
                        + "Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
