package com.github.laxika.magicalvibes.model.effect;

/**
 * "Move a counter from target creature onto a second target creature." (e.g. Leech Bonder's
 * {@code {U}, {Q}} ability).
 *
 * <p>Reads two targets from the activated ability's flat multi-target list: position 0 is the
 * creature a counter is removed from, position 1 is the creature it is placed on. A single counter
 * of one kind is moved; if the first creature has more than one kind of counter, the handler moves
 * the first present kind. If the first creature has no counters (or either target is gone) when the
 * ability resolves, nothing happens.</p>
 */
public record MoveCounterFromTargetCreatureToTargetCreatureEffect() implements CardEffect {

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
