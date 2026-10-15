package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

/**
 * Biolume Serpent — back face of Biolume Egg.
 */
public class BiolumeSerpent extends Card {

    public BiolumeSerpent() {
        // Sacrifice two Islands: This creature can't be blocked this turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(2, new PermanentHasSubtypePredicate(CardSubtype.ISLAND)),
                        new MakeCreatureUnblockableEffect(true)
                ),
                "Sacrifice two Islands: This creature can't be blocked this turn."
        ));
    }
}
