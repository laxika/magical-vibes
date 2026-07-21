package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesMustAttackThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/**
 * Grisly Anglerfish — back face of Grizzled Angler.
 * {6}: Creatures your opponents control attack this turn if able.
 */
public class GrislyAnglerfish extends Card {

    public GrislyAnglerfish() {
        var opponentCreatures = new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate());

        // {6}: Creatures your opponents control attack this turn if able.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{6}",
                List.of(new MatchingCreaturesMustAttackThisTurnEffect(opponentCreatures)),
                "{6}: Creatures your opponents control attack this turn if able."
        ));
    }
}
