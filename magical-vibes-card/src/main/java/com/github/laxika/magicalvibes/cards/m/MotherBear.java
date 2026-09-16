package com.github.laxika.magicalvibes.cards.m;

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

@CardRegistration(set = "MH1", collectorNumber = "171")
public class MotherBear extends Card {

    public MotherBear() {
        addGraveyardActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}{G}",
                List.of(
                        new ExileSelfFromGraveyardCost(),
                        new CreateTokenEffect(2, "Bear", 2, 2, CardColor.GREEN,
                                List.of(CardSubtype.BEAR), Set.of(), Set.of())
                ),
                "{3}{G}{G}, Exile this card from your graveyard: Create two 2/2 green Bear creature "
                        + "tokens. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
