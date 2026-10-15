package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RushwoodDryad;
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

@CardUsed({VineDryad.class, RushwoodDryad.class, Forest.class})
class VineDryadTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast by exiling a green card from hand instead of paying mana")
    void castsWithGreenCardExileAlternateCost() {
        harness.setHand(player1, List.of(new VineDryad(), new RushwoodDryad()));

        castWithAlternateExileFromHand(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vine Dryad");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exile -> exile.card().getName())
                .containsExactly("Rushwood Dryad");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Alternate cost rejects exiling a non-green card")
    void alternateCostRequiresGreenCard() {
        harness.setHand(player1, List.of(new VineDryad(), new Forest()));

        assertThatThrownBy(() -> castWithAlternateExileFromHand(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Flash allows casting during an opponent's turn")
    void flashAllowsCastingDuringOpponentsTurn() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VineDryad(), new RushwoodDryad()));

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
        Permanent blocker = addCreatureReady(player2, new VineDryad());

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
        Permanent blocker = addCreatureReady(player2, new VineDryad());
        harness.setLife(player2, 20);

        prepareDeclareBlockers();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(vineDryad);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can pay the printed mana cost without exiling a card")
    void castsWithPrintedManaCost() {
        harness.setHand(player1, List.of(new VineDryad(), new RushwoodDryad()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vine Dryad");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Rushwood Dryad");
        assertThat(gd.exiledCards).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    @DisplayName("Can exile a green card preceding the spell in hand")
    void exilesCardBeforeSpellInHand() {
        harness.setHand(player1, List.of(new RushwoodDryad(), new VineDryad()));

        castWithAlternateExileFromHand(player1, 1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vine Dryad");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exile -> exile.card().getName())
                .containsExactly("Rushwood Dryad");
    }

    @Test
    @DisplayName("Cannot exile the spell itself to pay its alternate cost")
    void cannotExileItself() {
        harness.setHand(player1, List.of(new VineDryad(), new RushwoodDryad()));

        assertThatThrownBy(() -> castWithAlternateExileFromHand(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("A second Vine Dryad can pay the alternate cost without spending available mana")
    void exilesAnotherVineDryadAndPreservesMana() {
        harness.setHand(player1, List.of(new VineDryad(), new VineDryad()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        castWithAlternateExileFromHand(player1, 0, 1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vine Dryad");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(exile -> exile.card().getName())
                .containsExactly("Vine Dryad");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(4);
    }

    private void castWithAlternateExileFromHand(com.github.laxika.magicalvibes.model.Player player,
                                                 int cardIndex, int exileHandCardIndex) {
        gs.playCard(gd, player, cardIndex, 0, null, null, List.of(), List.of(), false,
                null, null, List.of(), null, List.of(), false, exileHandCardIndex);
    }
}
