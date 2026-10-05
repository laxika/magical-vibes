package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AvenRiftwatcher;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({NeedlepeakSpider.class, AvenRiftwatcher.class})
class NeedlepeakSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Needlepeak Spider can block a creature with flying")
    void canBlockFlyingCreature() {
        addCreatureReady(player1, new AvenRiftwatcher());
        addCreatureReady(player2, new NeedlepeakSpider());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Reach does not prevent a creature without flying from blocking Needlepeak Spider")
    void canBeBlockedByCreatureWithoutFlying() {
        addCreatureReady(player1, new NeedlepeakSpider());
        addCreatureReady(player2, new NeedlepeakSpider());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
