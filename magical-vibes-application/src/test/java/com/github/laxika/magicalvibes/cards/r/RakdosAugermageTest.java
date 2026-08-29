package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({RakdosAugermage.class, Forest.class, GrizzlyBears.class})
class RakdosAugermageTest extends BaseCardTest {

    @Test
    @DisplayName("Target opponent chooses from the controller's hand, then the controller chooses from the opponent's hand")
    void bothPlayersChooseTheDiscardedCard() {
        int index = setupReadyAugermage();
        harness.setHand(player1, new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));

        harness.activateAbility(player1, index, null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice firstChoice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(firstChoice).isNotNull();
        assertThat(firstChoice.choosingPlayerId()).isEqualTo(player2.getId());
        assertThat(firstChoice.targetPlayerId()).isEqualTo(player1.getId());

        harness.handleCardChosen(player2, 1);

        PendingInteraction.RevealedHandChoice secondChoice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(secondChoice).isNotNull();
        assertThat(secondChoice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(secondChoice.targetPlayerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Forest");
        assertThat(gd.playerHands.get(player2.getId())).extracting(card -> card.getName())
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("An empty controller hand still resolves the targeted opponent's discard")
    void emptyControllerHandStillResolvesSecondChoice() {
        int index = setupReadyAugermage();
        harness.setHand(player1, List.of());
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));

        harness.activateAbility(player1, index, null, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.RevealedHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(choice.targetPlayerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).extracting(card -> card.getName())
                .containsExactly("Forest");
    }

    @Test
    @DisplayName("The ability cannot target its controller")
    void cannotTargetController() {
        int index = setupReadyAugermage();

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability can be activated only at sorcery speed")
    void cannotActivateDuringOpponentTurn() {
        int index = addAugermage(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, index, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private int setupReadyAugermage() {
        int index = addAugermage(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return index;
    }

    private int addAugermage(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new RakdosAugermage());
        permanent.setSummoningSick(false);
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
