package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TomeAnima.class, Island.class})
class TomeAnimaTest extends BaseCardTest {

    @Test
    @DisplayName("Tome Anima can't be blocked after its controller draws two cards")
    void cantBeBlockedAfterControllerDrawsTwoCards() {
        Permanent tomeAnima = addCreatureReady(player1, new TomeAnima());
        assertThat(gqs.hasCantBeBlocked(gd, tomeAnima)).isFalse();

        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        draw(player1);
        assertThat(gqs.hasCantBeBlocked(gd, tomeAnima)).isFalse();

        draw(player1);
        assertThat(gqs.hasCantBeBlocked(gd, tomeAnima)).isTrue();
    }

    @Test
    @DisplayName("Tome Anima remains blockable when only an opponent draws two cards")
    void opponentDrawsDoNotEnableUnblockable() {
        Permanent tomeAnima = addCreatureReady(player1, new TomeAnima());

        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        draw(player2);
        draw(player2);

        assertThat(gqs.hasCantBeBlocked(gd, tomeAnima)).isFalse();
    }

    @Test
    void drawsBeforeEnteringBattlefieldCountAndThirdDrawKeepsItUnblockable() {
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        draw(player1);
        draw(player1);

        Permanent tomeAnima = addCreatureReady(player1, new TomeAnima());
        assertThat(gqs.hasCantBeBlocked(gd, tomeAnima)).isTrue();

        draw(player1);
        assertThat(gqs.hasCantBeBlocked(gd, tomeAnima)).isTrue();
    }

    @Test
    void cannotDeclareBlockerAfterTwoDraws() {
        addCreatureReady(player1, new TomeAnima());
        addCreatureReady(player2, new TomeAnima());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        draw(player1);
        draw(player1);

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    void canDeclareBlockerAfterOnlyOneDraw() {
        addCreatureReady(player1, new TomeAnima());
        Permanent blocker = addCreatureReady(player2, new TomeAnima());
        harness.setLibrary(player1, List.of(new Island()));
        draw(player1);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void drawCountResetsOnNextTurn() {
        Permanent tomeAnima = addCreatureReady(player1, new TomeAnima());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Island(), new Island()));
        harness.setLibrary(player2, List.of(new Island(), new Island()));
        draw(player1);
        draw(player1);
        assertThat(gqs.hasCantBeBlocked(gd, tomeAnima)).isTrue();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.hasCantBeBlocked(gd, tomeAnima)).isFalse();
    }

    private void draw(com.github.laxika.magicalvibes.model.Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
    }
}
