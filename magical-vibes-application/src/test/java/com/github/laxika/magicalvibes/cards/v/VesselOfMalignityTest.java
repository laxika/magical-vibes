package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VesselOfMalignity.class, GrizzlyBears.class, Forest.class, Peek.class})
class VesselOfMalignityTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Vessel of Malignity sacrifices it and targets an opponent")
    void activatingSacrificesVesselAndTargetsOpponent() {
        addReadyVessel(player1);
        prepareSorcerySpeedActivation();

        harness.activateAbility(player1, 0, null, player2.getId());

        harness.assertInGraveyard(player1, "Vessel of Malignity");
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Target opponent chooses two cards to exile")
    void targetOpponentChoosesTwoCardsToExile() {
        addReadyVessel(player1);
        harness.setHand(player2, new ArrayList<>(List.of(
                new GrizzlyBears(), new Forest(), new Peek())));
        prepareSorcerySpeedActivation();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.ExileFromHandChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .singleElement()
                .extracting(card -> card.getName())
                .isEqualTo("Peek");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .containsExactlyInAnyOrder("Grizzly Bears", "Forest");
    }

    @Test
    @DisplayName("Cannot target the controller")
    void cannotTargetController() {
        addReadyVessel(player1);
        prepareSorcerySpeedActivation();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate outside sorcery speed")
    void cannotActivateOutsideSorcerySpeed() {
        addReadyVessel(player1);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("An opponent with one card exiles that card and completes resolution")
    void opponentWithOneCardExilesIt() {
        addReadyVessel(player1);
        harness.setHand(player2, List.of(new Forest()));
        prepareSorcerySpeedActivation();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName()).containsExactly("Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Vessel of Malignity");
    }

    @Test
    @DisplayName("An empty hand is a legal target and needs no choice")
    void emptyHandResolvesWithoutChoice() {
        addReadyVessel(player1);
        harness.setHand(player2, List.of());
        prepareSorcerySpeedActivation();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Vessel of Malignity");
    }

    @Test
    @DisplayName("Cannot activate during combat on the controller's turn")
    void cannotActivateDuringCombat() {
        addReadyVessel(player1);
        prepareSorcerySpeedActivation();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("main phase");
        harness.assertOnBattlefield(player1, "Vessel of Malignity");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate while another ability is on the stack")
    void cannotActivateWithNonemptyStack() {
        addReadyVessel(player1);
        addReadyVessel(player1);
        prepareSorcerySpeedActivation();
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, null, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.assertOnBattlefield(player1, "Vessel of Malignity");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Generic mana cannot pay the black activation cost")
    void requiresBlackMana() {
        addReadyVessel(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Vessel of Malignity");
        assertThat(gd.stack).isEmpty();
    }

    private void prepareSorcerySpeedActivation() {
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addReadyVessel(Player player) {
        harness.addToBattlefieldAndReturn(player, new VesselOfMalignity()).setSummoningSick(false);
    }
}
