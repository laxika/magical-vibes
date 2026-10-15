package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Chronosavant.class})
@DisplayName("Chronosavant")
class ChronosavantTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself from the graveyard to the battlefield tapped")
    void returnsFromGraveyardTapped() {
        harness.setGraveyard(player1, List.of(new Chronosavant()));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent chronosavant = findPermanent(player1, "Chronosavant");
        assertThat(chronosavant.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Chronosavant");
    }

    @Test
    @DisplayName("Returns only the card whose graveyard ability was activated")
    void returnsOnlyTheActivatedCard() {
        Chronosavant activatedCard = new Chronosavant();
        Chronosavant otherCard = new Chronosavant();
        harness.setGraveyard(player1, List.of(activatedCard, otherCard));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Chronosavant").getCard()).isSameAs(activatedCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherCard);
    }

    @Test
    @DisplayName("Queues a skip of its controller's next turn")
    void queuesSkipOfNextTurn() {
        harness.setGraveyard(player1, List.of(new Chronosavant()));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.skipNextTurnCount.getOrDefault(player2.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Skips its controller's next turn")
    void skipsNextTurn() {
        harness.setGraveyard(player1, List.of(new Chronosavant()));
        addActivationMana();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        endTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());

        endTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(0);
    }

    @Test
    @DisplayName("Multiple activations return the card once but skip two successive turns")
    void multipleActivationsSkipSuccessiveTurns() {
        harness.setGraveyard(player1, List.of(new Chronosavant()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Chronosavant").isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Chronosavant");

        endTurn();
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(2);
        endTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        endTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isZero();
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Still skips a turn when the source has left the graveyard before resolution")
    void skipsTurnWhenSourceIsAbsent() {
        Chronosavant card = new Chronosavant();
        harness.setGraveyard(player1, List.of(card));
        addActivationMana();
        harness.activateGraveyardAbility(player1, 0);

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(card));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Chronosavant");
        harness.assertNotInGraveyard(player1, "Chronosavant");
        endTurn();
        endTurn();
        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.skipNextTurnCount.getOrDefault(player1.getId(), 0)).isZero();
    }

    private void addActivationMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void endTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
    }
}
