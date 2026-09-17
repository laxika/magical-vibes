package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.l.LongTermPlans;
import com.github.laxika.magicalvibes.cards.s.ScornfulEgotist;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrimitiveEtchings.class, ScornfulEgotist.class, LongTermPlans.class})
class PrimitiveEtchingsTest extends BaseCardTest {

    @Test
    @DisplayName("The first creature drawn each turn causes an additional draw")
    void firstDrawnCreatureDrawsAdditionalCard() {
        harness.addToBattlefield(player1, new PrimitiveEtchings());
        harness.setLibrary(player1, List.of(new ScornfulEgotist(), new LongTermPlans()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gameLogContains("reveals Scornful Egotist")).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The additional creature draw is not revealed or chained")
    void additionalCreatureDrawIsNotRevealedOrChained() {
        harness.addToBattlefield(player1, new PrimitiveEtchings());
        harness.setLibrary(player1, List.of(
                new ScornfulEgotist(), new ScornfulEgotist(), new LongTermPlans()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.gameLog.stream()
                .filter(entry -> entry.plainText().contains("reveals Scornful Egotist")))
                .hasSize(1);
        assertThat(gameLogContains("reveals Long-Term Plans")).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The first noncreature drawn each turn does not cause an additional draw")
    void firstDrawnNoncreatureDoesNotDrawAdditionalCard() {
        harness.addToBattlefield(player1, new PrimitiveEtchings());
        harness.setLibrary(player1, List.of(new LongTermPlans(), new ScornfulEgotist()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gameLogContains("reveals Long-Term Plans")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering after the first draw does not reveal a later draw that turn")
    void enteringAfterFirstDrawDoesNotTriggerThatTurn() {
        harness.setLibrary(player1, List.of(new LongTermPlans(), new ScornfulEgotist()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.addToBattlefield(player1, new PrimitiveEtchings());
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gameLogContains("reveals Scornful Egotist")).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A later draw in the same turn is not revealed")
    void laterDrawDoesNotTrigger() {
        harness.addToBattlefield(player1, new PrimitiveEtchings());
        harness.setLibrary(player1, List.of(
                new ScornfulEgotist(), new LongTermPlans(), new ScornfulEgotist()));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("reveals Long-Term Plans")).isFalse();
    }

    @Test
    @DisplayName("The first draw during an opponent's turn is revealed")
    void firstDrawDuringOpponentsTurnTriggers() {
        harness.addToBattlefield(player1, new PrimitiveEtchings());
        harness.setLibrary(player1, List.of(new ScornfulEgotist(), new LongTermPlans()));
        gd.activePlayerId = player2.getId();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.passBothPriorities();

        assertThat(gameLogContains("reveals Scornful Egotist")).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.stack).isEmpty();
    }
}
