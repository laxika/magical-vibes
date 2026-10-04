package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EnnisDebateModerator.class, EternalStudent.class})
class EnnisDebateModeratorTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles another creature until the next end step and gets a counter")
    void flickersCreatureAndGetsCounter() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EternalStudent());
        castEnnis(List.of(creature.getId()));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Eternal Student");

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Eternal Student");
        Permanent ennis = findPermanent(player1, "Ennis, Debate Moderator");
        assertThat(ennis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only another creature you control is a legal ETB target")
    void onlyAnotherControlledCreatureIsLegalTarget() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new EternalStudent());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new EternalStudent());
        harness.setHand(player1, List.of(new EnnisDebateModerator()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature you control");

        harness.castCreature(player1, 0, List.of(ownCreature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Does not get a counter when no card was exiled this turn")
    void noCounterWithoutExile() {
        harness.addToBattlefield(player1, new EnnisDebateModerator());

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Ennis, Debate Moderator")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("May choose no ETB target even with another creature available")
    void mayChooseNoTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EternalStudent());
        castEnnis(List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Eternal Student").getId()).isEqualTo(creature.getId());
        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Ennis, Debate Moderator")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Opponent's graveyard exile before Ennis enters still grants one counter")
    void opponentExileBeforeEnnisEntersCounts() {
        harness.setGraveyard(player2, List.of(new EternalStudent(), new EternalStudent()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.activateGraveyardAbility(player2, 0);
        harness.passBothPriorities();
        harness.activateGraveyardAbility(player2, 0);
        harness.passBothPriorities();

        castEnnis(List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Ennis, Debate Moderator")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An exile during the opponent's turn does not trigger Ennis at their end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new EnnisDebateModerator());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player2, List.of(new EternalStudent()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.activateGraveyardAbility(player2, 0);
        harness.passBothPriorities();

        advanceToEndStep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Ennis, Debate Moderator")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A borrowed creature returns under its owner's control")
    void returnsBorrowedCreatureToOwner() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new EternalStudent());
        gd.stolenCreatures.put(creature.getId(), player2.getId());
        castEnnis(List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        advanceToEndStep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Eternal Student");
        harness.assertOnBattlefield(player2, "Eternal Student");
        assertThat(findPermanent(player2, "Eternal Student").getId()).isNotEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Exiling a token does not count as putting a card into exile")
    void tokenExileDoesNotGrantCounter() {
        harness.setGraveyard(player1, List.of(new EternalStudent()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        Permanent token = findPermanent(player1, "Inkling");

        castEnnis(List.of(token.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(findPermanents(player1, "Inkling")).hasSize(1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Inkling")).hasSize(1);
        assertThat(findPermanent(player1, "Ennis, Debate Moderator")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castEnnis(List<UUID> targetIds) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new EnnisDebateModerator()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castCreature(player1, 0, targetIds);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }
}
