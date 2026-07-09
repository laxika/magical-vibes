package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GuileTest extends BaseCardTest {

    // ===== Counter replacement =====

    @Test
    @DisplayName("A counter you control exiles the spell instead and offers a free play (Guile)")
    void controlledCounterExilesAndOffersFreePlay() {
        harness.addToBattlefield(player2, new Guile());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities(); // Cancel resolves

        GameData gd = harness.getGameData();

        // Not countered to graveyard — exiled instead.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));

        // Guile's controller is offered the free play.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the free play casts the exiled spell under the counter's controller")
    void acceptingFreePlayCastsSpellUnderCounterController() {
        harness.addToBattlefield(player2, new Guile());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities(); // Cancel resolves
        harness.handleMayAbilityChosen(player2, true); // Accept the free play

        GameData gd = harness.getGameData();

        // Bears is now on the stack as player2's creature spell.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getId()).isEqualTo(bears.getId());
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        harness.passBothPriorities(); // Bears resolves

        // It ends up on player2's battlefield (they cast it).
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Declining the free play leaves the spell exiled")
    void decliningFreePlayLeavesSpellExiled() {
        harness.addToBattlefield(player2, new Guile());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities(); // Cancel resolves
        harness.handleMayAbilityChosen(player2, false); // Decline

        GameData gd = harness.getGameData();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Without Guile the counter goes to the graveyard normally and offers no free play")
    void withoutGuileNormalCounter() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.passBothPriorities(); // Cancel resolves

        GameData gd = harness.getGameData();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    // ===== Blocking restriction =====

    @Test
    @DisplayName("Guile can't be blocked by fewer than three creatures")
    void cannotBeBlockedByFewerThanThree() {
        Permanent guile = new Permanent(new Guile());
        guile.setSummoningSick(false);
        guile.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(guile);

        for (int i = 0; i < 3; i++) {
            Permanent blocker = new Permanent(new GrizzlyBears());
            blocker.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(blocker);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("3 or more creatures");
    }

    @Test
    @DisplayName("Guile can be blocked by three creatures")
    void canBeBlockedByThree() {
        Permanent guile = new Permanent(new Guile());
        guile.setSummoningSick(false);
        guile.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(guile);

        for (int i = 0; i < 3; i++) {
            Permanent blocker = new Permanent(new GrizzlyBears());
            blocker.setSummoningSick(false);
            gd.playerBattlefields.get(player2.getId()).add(blocker);
        }

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0),
                new BlockerAssignment(2, 0)));

        assertThat(gd.playerBattlefields.get(player2.getId()).get(0).isBlocking()).isTrue();
    }

    // ===== Put into graveyard =====

    @Test
    @DisplayName("When Guile is put into a graveyard it is shuffled into its owner's library")
    void putIntoGraveyardShufflesIntoLibrary() {
        harness.setLibrary(player2, new java.util.ArrayList<>());
        Permanent guile = harness.addToBattlefieldAndReturn(player2, new Guile());
        guile.setMarkedDamage(6);

        harness.runStateBasedActions();
        harness.passBothPriorities(); // trigger resolves

        harness.assertNotInGraveyard(player2, "Guile");
        assertThat(gd.playerDecks.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Guile"));
    }
}
