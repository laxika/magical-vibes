package com.github.laxika.magicalvibes.model.filter;

import java.util.Objects;

/**
 * Matches a spell that could target a permanent matching {@code filter} other than the permanents it targets, the
 * permanent judged from the ability's controller ("you control", "an opponent controls") and targeted as the spell's
 * controller would target it. With {@code otherThanTargetController} the permanent must also be controlled by a player
 * other than the controller of the permanent the spell targets ("another opponent").
 * <p>
 * The intervening "if" of a copy-onto-another-target cast trigger, written as the trigger's stack-entry filter:
 * Beamsplitter Mage's "if you control one or more other creatures that spell could target", Exterminator Magmarch's "if
 * another opponent controls one or more nonland permanents that spell could target". The copy effect checks it again
 * as it resolves (CR 603.4): with no such permanent left it makes no copy. Judged only by the spell-cast trigger
 * collector, which can enumerate the spell's legal targets; any other evaluator matches nothing.
 *
 * @param filter                    what the other permanent must be
 * @param otherThanTargetController whether its controller must differ from the controller of the spell's target
 */
public record StackEntryCouldTargetAnotherPermanentPredicate(PermanentPredicate filter,
                                                            boolean otherThanTargetController)
        implements StackEntryPredicate {

    public StackEntryCouldTargetAnotherPermanentPredicate {
        Objects.requireNonNull(filter, "filter");
    }
}
