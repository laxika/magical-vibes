package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

/**
 * Nezumi Shadow-Watcher — {B} Creature — Rat Warrior 1/1.
 * Sacrifice this creature: Destroy target Ninja.
 */
@CardRegistration(set = "BOK", collectorNumber = "74")
public class NezumiShadowWatcher extends Card {

    public NezumiShadowWatcher() {
        addActivatedAbility(new ActivatedAbility(false, null,
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "Sacrifice this creature: Destroy target Ninja.",
                new PermanentPredicateTargetFilter(
                        new PermanentHasSubtypePredicate(CardSubtype.NINJA),
                        "Target must be a Ninja.")));
    }
}
