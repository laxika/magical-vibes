package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEM", collectorNumber = "114")
public class SaprolingCluster extends Card {

    public SaprolingCluster() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new CreateTokenEffect(
                                "Saproling", 1, 1,
                                CardColor.GREEN, List.of(CardSubtype.SAPROLING), Set.of(), Set.of()
                        )
                ),
                "{1}, Discard a card: Create a 1/1 green Saproling creature token. Any player may activate this ability."
        ).withActivatableByAnyPlayer());
    }
}
