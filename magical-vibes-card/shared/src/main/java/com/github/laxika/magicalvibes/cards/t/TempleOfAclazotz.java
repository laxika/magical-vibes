package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

/**
 * Temple of Aclazotz — back face of Arguel's Blood Fast.
 * Legendary Land.
 * {T}: Add {B}.
 * {T}, Sacrifice a creature: You gain life equal to the sacrificed creature's toughness.
 */
public class TempleOfAclazotz extends Card {

    public TempleOfAclazotz() {

        // {T}, Sacrifice a creature: You gain life equal to the sacrificed creature's toughness.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new SacrificeCreatureCost(false, false, true), new GainLifeEffect(new XValue())),
                "{T}, Sacrifice a creature: You gain life equal to the sacrificed creature's toughness."
        ));
        // {T}: Add {B}.
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
    }
}
