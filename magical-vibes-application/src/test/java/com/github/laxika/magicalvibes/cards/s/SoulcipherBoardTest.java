package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.r.RecklessScholar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulcipherBoard.class, Island.class, GrizzlyBears.class, AirElemental.class, RecklessScholar.class})
class SoulcipherBoardTest extends BaseCardTest {

    private Permanent addBoard(Player player) {
        Permanent board = harness.addToBattlefieldAndReturn(player, new SoulcipherBoard());
        board.setCounterCount(CounterType.OMEN, 3);
        board.setSummoningSick(false);
        return board;
    }

    private void drainStack() {
        int guard = 0;
        while (!gd.stack.isEmpty() && guard++ < 50) {
            harness.passBothPriorities();
        }
    }

    @Test
    @DisplayName("Enters with three omen counters when cast")
    void entersWithThreeOmenCounters() {
        harness.setHand(player1, List.of(new SoulcipherBoard()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent board = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(board.getCounterCount(CounterType.OMEN)).isEqualTo(3);
        assertThat(board.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Activated ability puts one of the top two into the graveyard")
    void activatedAbilityBinsOneOfTopTwo() {
        Permanent board = addBoard(player1);
        Card top = new Island();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top, second));

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, board), null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        // Put Island into GY — not a creature card, so omen trigger does not fire.
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(top.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getId()).isEqualTo(second.getId());
        assertThat(board.getCounterCount(CounterType.OMEN)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creature card to graveyard removes an omen counter")
    void creatureCardToGraveyardRemovesOmen() {
        Permanent board = addBoard(player1);
        Card creature = new GrizzlyBears();
        Card other = new Island();
        harness.setLibrary(player1, List.of(creature, other));

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, board), null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        drainStack();

        assertThat(board.getCounterCount(CounterType.OMEN)).isEqualTo(2);
        assertThat(board.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Transforms when the last omen counter is removed")
    void transformsWhenLastOmenRemoved() {
        Permanent board = addBoard(player1);
        board.setCounterCount(CounterType.OMEN, 1);
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, new Island()));

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, indexOf(player1, board), null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        drainStack();

        assertThat(board.isTransformed()).isTrue();
        assertThat(board.getCard().getName()).isEqualTo("Cipherbound Spirit");
        assertThat(board.getCounterCount(CounterType.OMEN)).isZero();
        assertThat(gqs.getEffectivePower(gd, board)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, board)).isEqualTo(2);
    }

    @Test
    @DisplayName("Back face draws two then discards one")
    void backFaceLoots() {
        Permanent spirit = createTransformedSpirit(player1);
        harness.setHand(player1, List.of(new Island()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new GrizzlyBears()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        int gyBefore = gd.playerGraveyards.get(player1.getId()).size();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, indexOf(player1, spirit), null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        // Draw 2, discard 1 → net +1 from the prior hand.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(gyBefore + 1);
    }

    @Test
    @DisplayName("Back face can block only creatures with flying")
    void backFaceBlocksOnlyFlying() {
        Permanent spirit = createTransformedSpirit(player2);
        Permanent flyer = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        flyer.setSummoningSick(false);
        flyer.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(indexOf(player2, spirit), 0)));
        assertThat(spirit.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Back face cannot block a creature without flying")
    void backFaceCannotBlockNonFlying() {
        Permanent spirit = createTransformedSpirit(player2);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(indexOf(player2, spirit), 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    void discardedCreatureRemovesOmenCounter() {
        Permanent board = addBoard(player1);
        Permanent scholar = harness.addToBattlefieldAndReturn(player1, new RecklessScholar());
        scholar.setSummoningSick(false);
        Card creature = new GrizzlyBears();
        harness.setHand(player1, List.of(creature));
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        harness.activateAbility(player1, indexOf(player1, scholar), null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        drainStack();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(board.getCounterCount(CounterType.OMEN)).isEqualTo(2);
        assertThat(board.isTransformed()).isFalse();
    }

    @Test
    void oneCardLibraryPutsItsOnlyCardIntoGraveyard() {
        Permanent board = addBoard(player1);
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, board), null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        drainStack();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(board.getCounterCount(CounterType.OMEN)).isEqualTo(2);
    }

    @Test
    void emptyLibraryDoesNothing() {
        Permanent board = addBoard(player1);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, indexOf(player1, board), null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(board.getCounterCount(CounterType.OMEN)).isEqualTo(3);
        assertThat(board.isTransformed()).isFalse();
    }

    @Test
    void creatureDeathTransformsBoardEvenWithoutCountersToRemove() {
        Permanent board = addBoard(player1);
        board.setCounterCount(CounterType.OMEN, 0);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setMarkedDamage(2);

        harness.runStateBasedActions();
        drainStack();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThat(board.isTransformed()).isTrue();
        assertThat(board.getCounterCount(CounterType.OMEN)).isZero();
    }

    @Test
    void opponentsCreatureDeathDoesNotRemoveCounter() {
        Permanent board = addBoard(player1);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        creature.setMarkedDamage(2);

        harness.runStateBasedActions();
        drainStack();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature.getCard());
        assertThat(board.getCounterCount(CounterType.OMEN)).isEqualTo(3);
        assertThat(board.isTransformed()).isFalse();
    }

    @Test
    void simultaneousCreatureDeathsDoNotTransformSpiritBack() {
        Permanent board = addBoard(player1);
        board.setCounterCount(CounterType.OMEN, 1);
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        first.setMarkedDamage(2);
        second.setMarkedDamage(2);

        harness.runStateBasedActions();
        drainStack();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first.getCard(), second.getCard());
        assertThat(board.isTransformed()).isTrue();
        assertThat(board.getCounterCount(CounterType.OMEN)).isZero();
    }

    @Test
    void tappedSpiritCanActivateAndDiscardCreatureWithoutTransformingBack() {
        Permanent spirit = createTransformedSpirit(player1);
        spirit.tap();
        Card discarded = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, indexOf(player1, spirit), null, null);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.handleCardChosen(player1, 0);
        drainStack();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(spirit.isTransformed()).isTrue();
        assertThat(spirit.isTapped()).isTrue();
    }

    private Permanent createTransformedSpirit(Player player) {
        Permanent board = addBoard(player);
        board.setCounterCount(CounterType.OMEN, 1);
        harness.setLibrary(player, List.of(new GrizzlyBears(), new Island()));

        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.activateAbility(player, indexOf(player, board), null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player, 0);
        drainStack();

        assertThat(board.isTransformed()).isTrue();
        board.untap();
        board.setSummoningSick(false);
        return board;
    }

    private int indexOf(Player player, Permanent perm) {
        return gd.playerBattlefields.get(player.getId()).indexOf(perm);
    }
}
