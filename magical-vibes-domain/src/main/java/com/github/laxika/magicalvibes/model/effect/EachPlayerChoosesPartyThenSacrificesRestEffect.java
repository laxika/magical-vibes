package com.github.laxika.magicalvibes.model.effect;

/**
 * Each player chooses up to one creature of each party role, then sacrifices the other creatures
 * they control.
 *
 * <p>A creature chosen for one role is no longer available for another role, so each creature can
 * fill only one party slot. All choices are completed before the sacrifices happen simultaneously.
 */
public record EachPlayerChoosesPartyThenSacrificesRestEffect() implements CardEffect {
}
