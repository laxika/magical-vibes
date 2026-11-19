package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * The payload of the layer-1 copy effect a resolving spell or ability makes on a permanent (CR 613.1a, 611.2a): a
 * floating effect on it with the stated duration ("until end of turn", "until your next turn", "for as long as that
 * creature remains tapped", none for "becomes a copy of"). The permanent shows the copiable values of its newest one
 * (CR 613.7).
 *
 * @param copiedCard  the card the permanent shows while this copy effect is the newest one on it
 * @param preCopyCard the card it showed before the first of its active copy effects of this kind: as the last of them
 *                    ends, it shows this again; as the newest of several ends, the newest one left applies again
 */
public record ResolvedCopyEffect(Card copiedCard, Card preCopyCard) implements CardEffect {
}
