package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VodalianHypnotist.class, Forest.class})
class VodalianHypnotistTest extends BaseCardTest {

    private Permanent readyHypnotist() {
        Permanent hypnotist = addCreatureReady(player1, new VodalianHypnotist());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return hypnotist;
    }

    @Test
    @DisplayName("Target opponent discards a card")
    void opponentDiscards() {
        harness.setHand(player2, List.of(new Forest()));
        readyHypnotist();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Can target any player, including its controller")
    void controllerCanBeTargeted() {
        harness.setHand(player1, List.of(new Forest()));
        readyHypnotist();

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Empty hand causes no discard")
    void emptyHandNoDiscard() {
        harness.setHand(player2, List.of());
        readyHypnotist();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Tapping is part of the activation cost")
    void activationTapsHypnotist() {
        harness.setHand(player2, List.of());
        Permanent hypnotist = readyHypnotist();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(hypnotist.isTapped()).isTrue();

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Discards exactly one card from a larger hand")
    void discardsExactlyOneCard() {
        harness.setHand(player2, List.of(new Forest(), new Forest()));
        readyHypnotist();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        harness.assertInHand(player2, "Forest");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Cannot target a permanent")
    void targetMustBePlayer() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        readyHypnotist();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be activated while already tapped")
    void cannotActivateWhenTapped() {
        Permanent hypnotist = readyHypnotist();
        hypnotist.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be activated while the stack is nonempty")
    void cannotActivateWithNonemptyStack() {
        harness.setHand(player2, List.of());
        addCreatureReady(player1, new VodalianHypnotist());
        addCreatureReady(player1, new VodalianHypnotist());
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot be activated during the opponent's turn")
    void cannotActivateOnOpponentTurn() {
        addCreatureReady(player1, new VodalianHypnotist());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot be activated outside a main phase")
    void cannotActivateOutsideMainPhase() {
        addCreatureReady(player1, new VodalianHypnotist());
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
