package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ForbiddenRitualTest extends BaseCardTest {

    private void castAt(UUID opponentId) {
        harness.setHand(player1, List.of(new ForbiddenRitual()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castSorcery(player1, 0, opponentId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("With one nontoken permanent, auto-sacrifices it and opponent loses life when that is the only option")
    void singleNontokenAutoSacrificesAndOpponentLosesLife() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castAt(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("With no nontoken permanent the spell does nothing")
    void noNontokenDoesNothing() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Mountain()));
        castAt(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Opponent may sacrifice any permanent including a land")
    void opponentMaySacrificeALand() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Mountain());
        castAt(player2.getId());

        UUID mountainId = findPermanent(player2, "Mountain").getId();
        harness.handleListChoice(player2, ChoiceContext.ForbiddenRitualPenaltyChoice.SACRIFICE);
        harness.handlePermanentChosen(player2, mountainId);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertNotOnBattlefield(player2, "Mountain");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Opponent may discard a card instead of losing life")
    void opponentMayDiscard() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new Mountain()));
        harness.addToBattlefield(player1, new GrizzlyBears());
        castAt(player2.getId());

        harness.handleListChoice(player2, ChoiceContext.ForbiddenRitualPenaltyChoice.DISCARD);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Mountain");
    }

    @Test
    @DisplayName("Controller may repeat: second cycle sacrifices another permanent")
    void mayRepeatProcess() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castAt(player2.getId());

        UUID first = findPermanent(player1, "Grizzly Bears").getId();
        harness.handlePermanentChosen(player1, first);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Declining the repeat ends the process")
    void declineRepeatEnds() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        castAt(player2.getId());

        UUID first = findPermanent(player1, "Grizzly Bears").getId();
        harness.handlePermanentChosen(player1, first);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
