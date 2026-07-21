package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfFromGraveyardCost;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "19")
public class DauntlessCathar extends Card {

    public DauntlessCathar() {
        // {1}{W}, Exile this card from your graveyard: Create a 1/1 white Spirit creature token
        // with flying. Activate only as a sorcery.
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        CreateTokenEffect.whiteSpirit(1)
                ),
                "{1}{W}, Exile this card from your graveyard: Create a 1/1 white Spirit creature "
                        + "token with flying. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
