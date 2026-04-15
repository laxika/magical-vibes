package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;

import java.util.List;

/**
 * Chalice of Death — back face of Chalice of Life.
 * Artifact.
 * {T}: Target player loses 5 life.
 */
public class ChaliceOfDeath extends Card {

    public ChaliceOfDeath() {
        // {T}: Target player loses 5 life.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new TargetPlayerLosesLifeEffect(5)),
                "{T}: Target player loses 5 life."
        ));
    }
}
