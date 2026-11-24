package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

/**
 * "For each color, put a +1/+1 counter on a Dragon you control of that color. If you put +1/+1 counters on five
 * Dragons this way, you win the game." (Call the Spirit Dragons): one choice of a Dragon of each color in turn
 * ({@code ChoosePermanentsEffect}, then the counter on {@code Subjects.thisWay(StepKind.CHOOSE)}), the Dragons that got
 * a counter tallied across the five counter steps, and the game won when five different Dragons did (the card's
 * ruling: a multicolored Dragon chosen for several colors counts once).
 *
 * @param nextColor         the index in {@code CardColor.values()} of the color chosen next; one past the last when
 *                          only the tally is left
 * @param dragonsCountered  the Dragons the earlier colors' counter steps put a counter on
 */
public record PutCountersOnDragonOfEachColorEffect(int nextColor, List<UUID> dragonsCountered) implements CardEffect {

    public PutCountersOnDragonOfEachColorEffect {
        dragonsCountered = List.copyOf(dragonsCountered);
    }

    public PutCountersOnDragonOfEachColorEffect() {
        this(0, List.of());
    }
}
