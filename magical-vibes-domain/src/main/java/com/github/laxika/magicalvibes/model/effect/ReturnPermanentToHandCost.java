package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

/**
 * Cost: return a matching permanent to its owner's hand. As an additional cost to cast a spell it is a permanent the
 * caster controls, supplied via {@code PlayCardRequest.sacrificePermanentId} and returned before mana is paid. As the
 * ransom of an {@code UnlessPaysEffect} ("sacrifice it unless you return an artifact you control to its owner's
 * hand") the payer chooses it as the effect resolves: one they control, or, with {@code anyController}, any matching
 * permanent on the battlefield ("unless you return an enchantment to its owner's hand": not targeted, so an
 * opponent's or an untargetable one may be returned).
 *
 * @param filter        the permanents that may be returned, judged by their current characteristics (CR 613)
 * @param description   the noun the ransom's prompt and log name ("artifact you control", "another creature you
 *                      control"), or {@code null} for "permanent you control"
 * @param anyController whether a permanent any player controls may be returned (a resolution ransom only)
 */
public record ReturnPermanentToHandCost(PermanentPredicate filter, String description, boolean anyController)
        implements CostEffect {

    private static final PermanentPredicate PERMANENT_FILTER = new PermanentTruePredicate();

    public ReturnPermanentToHandCost(PermanentPredicate filter) {
        this(filter, null, false);
    }

    public ReturnPermanentToHandCost() {
        this(PERMANENT_FILTER);
    }

    /** "Return a {@code description} to its owner's hand", one the payer controls. */
    public static ReturnPermanentToHandCost youControl(PermanentPredicate filter, String description) {
        return new ReturnPermanentToHandCost(filter, description, false);
    }

    /** "Return a {@code description} to its owner's hand", any player's ("return an enchantment"). */
    public static ReturnPermanentToHandCost anyPlayers(PermanentPredicate filter, String description) {
        return new ReturnPermanentToHandCost(filter, description, true);
    }

    @Override
    public PermanentPredicate consumedPermanentFilter() {
        return filter;
    }
}
