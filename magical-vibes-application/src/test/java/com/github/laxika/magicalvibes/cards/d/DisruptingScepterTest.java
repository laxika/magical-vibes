package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisruptingScepter.class, GrizzlyBears.class, Forest.class})
class DisruptingScepterTest extends BaseCardTest {

    @Test
    @DisplayName("Target player discards a card of their choice")
    void targetPlayerDiscardsACard() {
        addReadyScepter(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears(), new Forest())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).remainingCount()).isEqualTo(1);

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInHand(player2, "Forest");
    }

    @Test
    @DisplayName("Can target the controller as the player who discards")
    void canTargetController() {
        addReadyScepter(player1);
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Taps the Scepter and spends {3} as the cost")
    void tapsScepterAsCost() {
        Permanent scepter = addReadyScepter(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(scepter.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Can activate during any step of its controller's turn")
    void canActivateDuringOwnEndStep() {
        addReadyScepter(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate the Scepter while it is tapped")
    void cannotActivateWhenTapped() {
        Permanent scepter = addReadyScepter(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(scepter.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(3);
    }

    @Test
    @DisplayName("Target with empty hand results in no discard prompt")
    void targetWithEmptyHandNoPrompt() {
        addReadyScepter(player1);
        harness.setHand(player2, new ArrayList<>());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addReadyScepter(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2); // need 3

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a permanent instead of a player")
    void cannotTargetPermanent() {
        Permanent scepter = addReadyScepter(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(scepter.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate during opponent's turn")
    void cannotActivateDuringOpponentsTurn() {
        addReadyScepter(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    void controllerCanBeTargeted() {
        addReadyScepter(player1);
        harness.setHand(player1, new ArrayList<>(List.of(new Forest())));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    private Permanent addReadyScepter(Player player) {
        Permanent scepter = harness.addToBattlefieldAndReturn(player, new DisruptingScepter());
        scepter.setSummoningSick(false);
        return scepter;
    }
}
