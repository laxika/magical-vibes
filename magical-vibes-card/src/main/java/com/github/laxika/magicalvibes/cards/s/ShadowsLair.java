package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaAbilities;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceCost;

import java.util.List;

/** Back face of Grasping Shadows. */
public class ShadowsLair extends Card {

    public ShadowsLair() {
        addActivatedAbility(ManaAbilities.tapFor(ManaColor.BLACK));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{B}",
                List.of(
                        new RemoveCounterFromSourceCost(1, CounterType.DREAD),
                        new DrawCardEffect(1),
                        new LoseLifeEffect(1)
                ),
                "{B}, {T}, Remove a dread counter from this land: You draw a card and you lose 1 life."
        ));
    }
}
