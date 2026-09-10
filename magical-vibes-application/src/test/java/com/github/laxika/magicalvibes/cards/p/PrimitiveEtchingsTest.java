package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimitiveEtchings.class, Forest.class, GrizzlyBears.class})
class PrimitiveEtchingsTest extends BaseCardTest {

    @Test
    @DisplayName("The first creature drawn each turn causes an additional draw")
    void firstDrawnCreatureDrawsAdditionalCard() {
        harness.addToBattlefield(player1, new PrimitiveEtchings());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gameLogContains("reveals Grizzly Bears")).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The first noncreature drawn each turn does not cause an additional draw")
    void firstDrawnNoncreatureDoesNotDrawAdditionalCard() {
        harness.addToBattlefield(player1, new PrimitiveEtchings());
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gameLogContains("reveals Forest")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A later draw in the same turn is not revealed")
    void laterDrawDoesNotTrigger() {
        harness.addToBattlefield(player1, new PrimitiveEtchings());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("reveals Forest")).isFalse();
    }

    @Test
    @DisplayName("The first draw during an opponent's turn is revealed")
    void firstDrawDuringOpponentsTurnTriggers() {
        harness.addToBattlefield(player1, new PrimitiveEtchings());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest()));
        gd.activePlayerId = player2.getId();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();

        assertThat(gameLogContains("reveals Grizzly Bears")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.stack).isEmpty();
    }
}
