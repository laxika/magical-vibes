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

@CardRegistration(set = "ECL", collectorNumber = "18")
public class GoldmeadowNomad extends Card {

    public GoldmeadowNomad() {
        // {W}, Exile this card from your graveyard: Create a 1/1 green and white Kithkin creature
        // token. Activate only as a sorcery.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{W}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenEffect(1, "Kithkin", 1, 1, CardColor.GREEN,
                                Set.of(CardColor.GREEN, CardColor.WHITE), List.of(CardSubtype.KITHKIN))
                ),
                "{W}, Exile this card from your graveyard: Create a 1/1 green and white Kithkin "
                        + "creature token. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
