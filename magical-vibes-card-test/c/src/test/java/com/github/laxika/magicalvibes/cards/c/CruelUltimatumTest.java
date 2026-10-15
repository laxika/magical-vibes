package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DregscapeZombie;

import com.github.laxika.magicalvibes.cards.p.Plains;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CruelUltimatum.class, CylianElf.class, DregscapeZombie.class, Plains.class})
class CruelUltimatumTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack targeting the opponent")
    void castingPutsOnStack() {
        castUltimatum();

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        harness.setHand(player1, List.of(new CruelUltimatum()));
        addUltimatumMana();

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, player1.getId(), null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");
    }

    @Test
    @DisplayName("Opponent sacrifices, discards three, loses 5; controller returns a creature, draws three, gains 5")
    void fullResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Opponent has a single creature (auto-sacrificed) and a hand to discard from.
        harness.addToBattlefield(player2, new CylianElf());
        harness.setHand(player2, new ArrayList<>(List.of(new Plains(), new Plains(), new Plains(), new Plains())));

        // Controller has a creature to return and cards to draw.
        harness.setGraveyard(player1, new ArrayList<>(List.of(new CylianElf())));
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains()));

        castUltimatum();
        harness.passBothPriorities();

        // Opponent discards three of their four cards.
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        // Controller returns the creature from their graveyard.
        harness.handleGraveyardCardChosen(player1, 0);

        // Opponent lost its only creature and three cards, and lost 5 life.
        harness.assertNotOnBattlefield(player2, "Cylian Elf");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);

        // Controller returned the creature to hand, drew three, and gained 5 life.
        harness.assertInHand(player1, "Cylian Elf");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4); // returned creature + three drawn
        harness.assertNotInGraveyard(player1, "Cylian Elf");
        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent chooses which creature to sacrifice when they control several")
    void opponentChoosesSacrifice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CylianElf());
        harness.addToBattlefield(player2, new DregscapeZombie());
        harness.setHand(player2, new ArrayList<>());
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains()));

        castUltimatum();
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, creature.getId());

        harness.assertNotOnBattlefield(player2, "Cylian Elf");
        harness.assertOnBattlefield(player2, "Dregscape Zombie");
    }

    @Test
    @DisplayName("Controller half still resolves when opponent has no creature and no cards")
    void controllerHalfResolvesWithEmptyOpponent() {
        harness.setLife(player1, 20);
        harness.setHand(player2, new ArrayList<>());
        harness.setGraveyard(player1, new ArrayList<>(List.of(new CylianElf())));
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains()));

        castUltimatum();
        harness.passBothPriorities();

        // No sacrifice/discard prompt; the controller half (return, draw, gain) is the only interaction.
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Cylian Elf");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning an available creature cannot be declined")
    void cannotDeclineCreatureReturn() {
        harness.setHand(player2, List.of());
        harness.setGraveyard(player1, List.of(new CylianElf()));
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains()));

        castUltimatum();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMayAbilityChosen(player1, false))
                .isInstanceOf(IllegalStateException.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Cylian Elf");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("No creature in the graveyard does not interrupt drawing or gaining life")
    void noCreatureInGraveyardStillDrawsAndGainsLife() {
        harness.setHand(player2, List.of(new Plains(), new Plains()));
        harness.setGraveyard(player1, List.of(new Plains()));
        harness.setLibrary(player1, List.of(new Plains(), new Plains(), new Plains()));

        castUltimatum();
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Plains");
        assertThat(gd.getLife(player1.getId())).isEqualTo(25);
        assertThat(gd.stack).isEmpty();
    }

    private void castUltimatum() {
        harness.setHand(player1, List.of(new CruelUltimatum()));
        addUltimatumMana();
        harness.castSorcery(player1, 0, player2.getId());
    }

    private void addUltimatumMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 2);
    }

}
