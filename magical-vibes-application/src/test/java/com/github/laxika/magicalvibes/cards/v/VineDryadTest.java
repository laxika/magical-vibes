package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
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

@CardUsed({VineDryad.class, GrizzlyBears.class, Shock.class, Forest.class})
class VineDryadTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast by exiling a green card from hand instead of paying mana")
    void castsWithGreenCardExileAlternateCost() {
        harness.setHand(player1, List.of(new VineDryad(), new GrizzlyBears()));

        castWithAlternateExileFromHand(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vine Dryad");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exile -> exile.card().getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Alternate cost rejects exiling a non-green card")
    void alternateCostRequiresGreenCard() {
        harness.setHand(player1, List.of(new VineDryad(), new Shock()));

        assertThatThrownBy(() -> castWithAlternateExileFromHand(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flash allows casting during an opponent's turn")
    void flashAllowsCastingDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VineDryad(), new GrizzlyBears()));

        harness.getGameService().passPriority(harness.getGameData(), player2);
        castWithAlternateExileFromHand(player1, 0, 1);

        GameData game = harness.getGameData();
        assertThat(game.stack).hasSize(1);
        assertThat(game.stack.getFirst().getCard().getName()).isEqualTo("Vine Dryad");
    }

    @Test
    @DisplayName("Forestwalk prevents blocking while the defending player controls a Forest")
    void forestwalkPreventsBlockingWithForest() {
        Permanent vineDryad = addCreatureReady(player1, new VineDryad());
        vineDryad.setAttacking(true);
        harness.addToBattlefield(player2, new Forest());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(vineDryad);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Forestwalk does not prevent blocking when the defending player controls no Forest")
    void forestwalkAllowsBlockingWithoutForest() {
        Permanent vineDryad = addCreatureReady(player1, new VineDryad());
        vineDryad.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player2, 20);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(vineDryad);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private void castWithAlternateExileFromHand(com.github.laxika.magicalvibes.model.Player player,
                                                 int cardIndex, int exileHandCardIndex) {
        gs.playCard(gd, player, cardIndex, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), false, exileHandCardIndex);
    }
}
