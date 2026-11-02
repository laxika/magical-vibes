package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CumulativeUpkeepEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

/**
 * Tidal Control's "Pay 2 life or {2}" is modelled as two separate activated abilities — one paying
 * life, one paying mana — which offers exactly the same choice to the activating player without a
 * dedicated hybrid cost.
 */
@CardRegistration(set = "ALL", collectorNumber = "40")
public class TidalControl extends Card {

    public TidalControl() {
        // Cumulative upkeep {2}.
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new CumulativeUpkeepEffect("{2}"));

        // Pay 2 life: Counter target red or green spell. Any player may activate this ability.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new PayLifeCost(2), new CounterSpellEffect()),
                "Pay 2 life: Counter target red or green spell. Any player may activate this ability.",
                redOrGreenSpell()
        ).withActivatableByAnyPlayer());

        // {2}: Counter target red or green spell. Any player may activate this ability.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(new CounterSpellEffect()),
                "{2}: Counter target red or green spell. Any player may activate this ability.",
                redOrGreenSpell()
        ).withActivatableByAnyPlayer());
    }

    private static StackEntryPredicateTargetFilter redOrGreenSpell() {
        return new StackEntryPredicateTargetFilter(
                new StackEntryColorInPredicate(Set.of(CardColor.RED, CardColor.GREEN)),
                "Target must be a red or green spell.");
    }
}
