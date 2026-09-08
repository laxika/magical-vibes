package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "72")
public class Mortiphobia extends Card {

    public Mortiphobia() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}{B}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, false)
                ),
                "{1}{B}, Discard a card: Exile target card from a graveyard."
        ));
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new SacrificeSelfCost(),
                        ExileGraveyardCardsEffect.exactTargetedFromAnyGraveyard(1, null, false)
                ),
                "{1}{B}, Sacrifice this enchantment: Exile target card from a graveyard."
        ));
    }
}
