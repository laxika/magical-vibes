package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DjinnOfTheLamp.class, GiantSpider.class, GrizzlyBears.class})
class DjinnOfTheLampTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking Djinn of the Lamp")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new DjinnOfTheLamp());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flying allows Djinn of the Lamp to be blocked by a creature with reach")
    void canBeBlockedByReachCreature() {
        addCreatureReady(player1, new DjinnOfTheLamp());
        addCreatureReady(player2, new GiantSpider());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
