package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GodsHallGuardian.class})
class GodsHallGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Can be foretold and cast from exile on a later turn")
    void foretellsAndCastsOnLaterTurn() {
        GodsHallGuardian guardian = new GodsHallGuardian();
        harness.setHand(player1, List.of(guardian));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(guardian.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromExile(player1, guardian.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Gods' Hall Guardian");
    }

    @Test
    @DisplayName("Cannot cast a foretold Guardian on the turn it was exiled")
    void cannotCastOnForetellTurn() {
        GodsHallGuardian guardian = new GodsHallGuardian();
        harness.setHand(player1, List.of(guardian));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.foretell(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, guardian.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(guardian.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Foretell is unavailable during an opponent's turn")
    void cannotForetellOnOpponentsTurn() {
        GodsHallGuardian guardian = new GodsHallGuardian();
        harness.setHand(player1, List.of(guardian));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Gods' Hall Guardian");
        assertThat(gd.findExiledCard(guardian.getId())).isNull();
    }

    @Test
    @DisplayName("Foretelling requires two mana")
    void cannotForetellWithOnlyOneMana() {
        GodsHallGuardian guardian = new GodsHallGuardian();
        harness.setHand(player1, List.of(guardian));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Gods' Hall Guardian");
        assertThat(gd.findExiledCard(guardian.getId())).isNull();
    }

    @Test
    @DisplayName("Foretell does not allow casting this creature at instant speed")
    void foretoldGuardianStillRequiresSorceryTiming() {
        GodsHallGuardian guardian = new GodsHallGuardian();
        harness.setHand(player1, List.of(guardian));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, guardian.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(guardian.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Foretell casting still requires white mana")
    void foretellCostRequiresWhiteMana() {
        GodsHallGuardian guardian = new GodsHallGuardian();
        harness.setHand(player1, List.of(guardian));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castFromExile(player1, guardian.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(guardian.getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can foretell during upkeep without using the stack")
    void canForetellOutsideMainPhase() {
        GodsHallGuardian guardian = new GodsHallGuardian();
        harness.setHand(player1, List.of(guardian));
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.foretell(player1, 0);

        assertThat(gd.findExiledCard(guardian.getId())).isNotNull();
        harness.assertNotInHand(player1, "Gods' Hall Guardian");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Attacking with Guardian does not tap it")
    void vigilanceKeepsAttackerUntapped() {
        var guardian = addCreatureReady(player1, new GodsHallGuardian());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(guardian.isTapped()).isFalse();
    }
}
