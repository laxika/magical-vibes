package com.github.laxika.magicalvibes.cards.t;
import com.github.laxika.magicalvibes.model.action.DelayedUntapPermanents;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({TeferiHeroOfDominaria.class, PrimordialWurm.class, LlanowarElves.class, Plains.class, Island.class})
class TeferiHeroOfDominariaTest extends BaseCardTest {

    @Test
    @DisplayName("+1 draws a card and registers delayed untap trigger")
    void plusOneDrawsCardAndRegistersDelayedTrigger() {
        Permanent teferi = addReadyTeferi(player1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Should have drawn a card
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        // Loyalty should increase
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(5); // 4 + 1
        // Delayed trigger should be registered
        assertThat(gd.getDelayedActions(DelayedUntapPermanents.class)).hasSize(1);
        assertThat(gd.getDelayedActions(DelayedUntapPermanents.class).getFirst().count()).isEqualTo(2);
        assertThat(gd.getDelayedActions(DelayedUntapPermanents.class).getFirst().controllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("+1 delayed trigger untaps tapped lands at end step")
    void plusOneDelayedTriggerUntapsLandsAtEndStep() {
        addReadyTeferi(player1);

        // Add two tapped lands
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        plains.tap();
        island.tap();

        // Activate +1
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Delayed trigger registered
        assertThat(gd.getDelayedActions(DelayedUntapPermanents.class)).hasSize(1);

        // Advance to end step to fire delayed trigger
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        // Delayed trigger should be on the stack
        assertThat(gd.stack).isNotEmpty();

        // Resolve the trigger — the controller is asked which lands to untap
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1, List.of(plains.getId(), island.getId()));

        // Both lands should be untapped
        assertThat(plains.isTapped()).isFalse();
        assertThat(island.isTapped()).isFalse();
        // Pending list should be cleared
        assertThat(gd.getDelayedActions(DelayedUntapPermanents.class)).isEmpty();
    }

    @Test
    @DisplayName("+1 delayed trigger untaps at most 2 lands when more than 2 are tapped")
    void plusOneDelayedTriggerUntapsAtMostTwoLands() {
        addReadyTeferi(player1);

        // Add three tapped lands
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        List<Permanent> lands = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Plains") || p.getCard().getName().equals("Island"))
                .toList();
        for (Permanent land : lands) {
            land.tap();
        }
        assertThat(lands).hasSize(3);

        // Activate +1
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        // Advance to end step
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.passBothPriorities();

        // The choice offers all three lands but caps the selection at 2
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrderElementsOf(
                lands.stream().map(Permanent::getId).toList());
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultiplePermanentsChosen(player1,
                lands.stream().limit(2).map(Permanent::getId).toList());

        // Only 2 should be untapped, 1 remains tapped
        long tappedCount = lands.stream().filter(Permanent::isTapped).count();
        assertThat(tappedCount).isEqualTo(1);
    }

    @Test
    @DisplayName("-3 puts target nonland permanent third from top of owner's library")
    void minusThreePutsNonlandThirdFromTop() {
        Permanent teferi = addReadyTeferi(player1);

        // Add a creature on opponent's battlefield
        harness.addToBattlefield(player2, new PrimordialWurm());
        UUID wurmId = harness.getPermanentId(player2, "Primordial Wurm");

        harness.activateAbility(player1, 0, 1, null, wurmId);
        harness.passBothPriorities();

        // Wurm should be gone from battlefield
        harness.assertNotOnBattlefield(player2, "Primordial Wurm");
        // Wurm should be third from top of opponent's library (index 2)
        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library).hasSizeGreaterThanOrEqualTo(3);
        assertThat(library.get(2).getName()).isEqualTo("Primordial Wurm");
        // Loyalty should decrease
        assertThat(teferi.getCounterCount(CounterType.LOYALTY)).isEqualTo(1); // 4 - 3
    }

    @Test
    @DisplayName("-3 can target own nonland permanent")
    void minusThreeCanTargetOwnPermanent() {
        addReadyTeferi(player1);
        harness.addToBattlefield(player1, new PrimordialWurm());
        UUID wurmId = harness.getPermanentId(player1, "Primordial Wurm");

        harness.activateAbility(player1, 0, 1, null, wurmId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Primordial Wurm");
        // Third from top of player1's library
        assertThat(gd.playerDecks.get(player1.getId()).get(2).getName()).isEqualTo("Primordial Wurm");
    }

    @Test
    @DisplayName("-3 cannot target a land")
    void minusThreeCannotTargetLand() {
        addReadyTeferi(player1);
        harness.addToBattlefield(player2, new Plains());
        UUID plainsId = harness.getPermanentId(player2, "Plains");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, plainsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-3 handles small library by placing at bottom when library has fewer than 2 cards")
    void minusThreeHandlesSmallLibrary() {
        addReadyTeferi(player1);

        // Clear opponent's library to just 1 card
        harness.setLibrary(player2, List.of(new PrimordialWurm()));

        harness.addToBattlefield(player2, new LlanowarElves());
        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        harness.activateAbility(player1, 0, 1, null, elvesId);
        harness.passBothPriorities();

        // With only 1 card in library, position 2 should clamp to library size
        List<Card> library = gd.playerDecks.get(player2.getId());
        assertThat(library).hasSize(2); // original card + tucked card
        // The tucked card should be at the end (position clamped)
        assertThat(library.get(1).getName()).isEqualTo("Llanowar Elves");
    }

    @Test
    @DisplayName("-8 with 8 loyalty causes Teferi to go to graveyard and emblem persists")
    void emblemPersistsAfterTeferiDies() {
        Permanent teferi = addReadyTeferi(player1);
        teferi.setCounterCount(CounterType.LOYALTY, 8);

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        // Teferi should be gone (8 - 8 = 0 loyalty)
        harness.assertNotOnBattlefield(player1, "Teferi, Hero of Dominaria");
        // Emblem persists
        assertThat(gd.emblems).hasSize(1);
    }

    @Test
    @DisplayName("Emblem triggers on controller draw, allowing exile of opponent's permanent")
    void emblemTriggersOnControllerDraw() {
        createEmblem();

        // Add target on opponent's battlefield
        harness.addToBattlefield(player2, new PrimordialWurm());
        UUID wurmId = harness.getPermanentId(player2, "Primordial Wurm");

        // Draw a card (triggers the emblem)
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        // Should be awaiting permanent choice for the emblem trigger target
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null).isTrue();

        // Choose the opponent's wurm
        harness.handlePermanentChosen(player1, wurmId);

        // Emblem trigger should be on stack
        assertThat(gd.stack).isNotEmpty();

        // Resolve the trigger
        harness.passBothPriorities();

        // Opponent's wurm should be exiled
        harness.assertNotOnBattlefield(player2, "Primordial Wurm");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Primordial Wurm"));
    }

    @Test
    @DisplayName("Emblem cannot target controller's own permanents")
    void emblemCannotTargetOwnPermanents() {
        createEmblem();

        // Only add a permanent on controller's battlefield (no opponent permanents)
        harness.addToBattlefield(player1, new PrimordialWurm());

        // Draw a card
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        // No valid targets — emblem trigger should be skipped
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate -8 with only 4 loyalty")
    void cannotActivateUltimateWithInsufficientLoyalty() {
        addReadyTeferi(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough loyalty");
    }

    @Test
    @DisplayName("Cannot activate two loyalty abilities on same planeswalker in one turn")
    void cannotActivateTwicePerTurn() {
        addReadyTeferi(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("one loyalty ability");
    }

    @Test
    void delayedUntapCanChooseAnOpponentsLand() {
        addReadyTeferi(player1);
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new Island());
        ownLand.tap();
        opposingLand.tap();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(ownLand.getId(), opposingLand.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(ownLand.getId(), opposingLand.getId()));
        assertThat(ownLand.isTapped()).isFalse();
        assertThat(opposingLand.isTapped()).isFalse();
    }

    @Test
    void delayedUntapCanChooseNoLandsAndOnlyTriggersOnce() {
        addReadyTeferi(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        land.tap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());
        assertThat(land.isTapped()).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    void minusThreeCanPutTeferiIntoItsOwnLibrary() {
        Permanent teferi = addReadyTeferi(player1);
        harness.activateAbility(player1, 0, 1, null, teferi.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Teferi, Hero of Dominaria");
        assertThat(gd.playerDecks.get(player1.getId()).get(2)).isSameAs(teferi.getCard());
    }

    @Test
    void emblemDoesNotTriggerForOpponentDraw() {
        createEmblem();
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Island());
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

    @Test
    void emblemCanExileAnOpponentsLand() {
        createEmblem();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Island");
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(land.getCard());
    }

    @Test
    void emblemTargetBecomesIllegalIfControllerGainsControlOfIt() {
        createEmblem();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.handlePermanentChosen(player1, creature.getId());

        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Primordial Wurm");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(creature.getCard());
    }

    private void createEmblem() {
        Permanent teferi = addReadyTeferi(player1);
        teferi.setCounterCount(CounterType.LOYALTY, 8);
        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
    }

    private Permanent addReadyTeferi(com.github.laxika.magicalvibes.model.Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new TeferiHeroOfDominaria());
        perm.setCounterCount(CounterType.LOYALTY, 4);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
