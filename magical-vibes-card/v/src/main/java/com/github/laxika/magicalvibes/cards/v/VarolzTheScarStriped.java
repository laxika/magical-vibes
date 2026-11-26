package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.GrantGraveyardAbilityToCardsEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "112")
public class VarolzTheScarStriped extends Card {

    public VarolzTheScarStriped() {
        // Each creature card in your graveyard has scavenge. The scavenge cost is equal to its mana cost.
        addStaticEffect(new GrantGraveyardAbilityToCardsEffect(new CardTypePredicate(CardType.CREATURE),
                GrantGraveyardAbilityToCardsEffect.PerCard.SCAVENGE_FOR_MANA_COST));

        // Sacrifice another creature: Regenerate Varolz, the Scar-Striped.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new RegenerateEffect()
                ),
                "Sacrifice another creature: Regenerate Varolz, the Scar-Striped."
        ));
    }
}
