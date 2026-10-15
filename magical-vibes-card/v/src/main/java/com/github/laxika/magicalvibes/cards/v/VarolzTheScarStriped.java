package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScavengeEqualToManaCostToCreatureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "112")
public class VarolzTheScarStriped extends Card {

    public VarolzTheScarStriped() {
        // Each creature card in your graveyard has scavenge. The scavenge cost is equal to its mana cost.
        addEffect(EffectSlot.STATIC, new GrantScavengeEqualToManaCostToCreatureCardsEffect());

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
