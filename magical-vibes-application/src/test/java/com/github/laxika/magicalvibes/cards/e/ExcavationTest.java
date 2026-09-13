package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DivingGriffin;
import com.github.laxika.magicalvibes.cards.r.RhysticCave;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Excavation.class, RhysticCave.class, DivingGriffin.class})
class ExcavationTest extends BaseCardTest {

    @Test
    @DisplayName("Controller pays {1} and sacrifices a land to draw a card")
    void controllerSacrificesLandToDraw() {
        addExcavation(player1);
        harness.addToBattlefield(player1, new RhysticCave());
        prepareMainPhase(player1);
        harness.setLibrary(player1, List.of(new DivingGriffin(), new DivingGriffin()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInGraveyard(player1, "Rhystic Cave");
    }

    @Test
    @DisplayName("Any player may activate Excavation and pays with their own land and mana")
    void opponentMayActivate() {
        harness.addToBattlefield(player1, new DivingGriffin());
        addExcavation(player1);
        harness.addToBattlefield(player2, new RhysticCave());
        prepareMainPhase(player2);
        harness.setLibrary(player2, List.of(new DivingGriffin(), new DivingGriffin()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.activateAbility(player2, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        harness.assertInGraveyard(player2, "Rhystic Cave");
    }

    @Test
    @DisplayName("Any player may activate Excavation during the controller's turn")
    void opponentMayActivateDuringControllersTurn() {
        harness.addToBattlefield(player1, new DivingGriffin());
        addExcavation(player1);
        harness.addToBattlefield(player2, new RhysticCave());
        prepareMainPhase(player1);
        harness.setLibrary(player2, List.of(new DivingGriffin()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.activateAbility(player2, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        harness.assertInGraveyard(player2, "Rhystic Cave");
    }

    @Test
    @DisplayName("The activating player chooses which land to sacrifice when several are available")
    void choosesLandToSacrifice() {
        addExcavation(player1);
        Permanent firstLand = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player1, new RhysticCave());
        prepareMainPhase(player1);
        harness.setLibrary(player1, List.of(new DivingGriffin()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, secondLand.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(firstLand)
                .doesNotContain(secondLand);
    }

    @Test
    @DisplayName("Excavation cannot be activated without a land to sacrifice")
    void requiresLandToSacrifice() {
        addExcavation(player1);
        harness.addToBattlefield(player1, new DivingGriffin());
        prepareMainPhase(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addExcavation(Player owner) {
        harness.addToBattlefield(owner, new Excavation());
    }

    private void prepareMainPhase(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
