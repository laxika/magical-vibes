package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.CaptainAmericaUnbowed;
import com.github.laxika.magicalvibes.cards.s.SolemnSimulacrum;
import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LoyalGuardian.class, CaptainAmericaUnbowed.class, SolemnSimulacrum.class, SolRing.class})
class LoyalGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("Lieutenant puts a +1/+1 counter on each creature you control at combat")
    void putsCountersOnYourCreaturesWhenYouControlYourCommander() {
        Card commander = new CaptainAmericaUnbowed();
        gd.makeCommander(player1.getId(), commander);
        Permanent commanderPermanent = addCreatureReady(player1, commander);
        Permanent guardian = addCreatureReady(player1, new LoyalGuardian());
        Permanent otherCreature = addCreatureReady(player1, new SolemnSimulacrum());
        Permanent opponentCreature = addCreatureReady(player2, new SolemnSimulacrum());

        advanceToCombat(player1);
        harness.passBothPriorities();

        assertThat(commanderPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lieutenant does nothing without a commander")
    void doesNotTriggerWithoutYourCommander() {
        Permanent guardian = addCreatureReady(player1, new LoyalGuardian());
        Permanent ownCreature = addCreatureReady(player1, new SolemnSimulacrum());

        advanceToCombat(player1);
        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lieutenant does nothing during an opponent's combat")
    void doesNotTriggerOnOpponentTurn() {
        Card commander = new CaptainAmericaUnbowed();
        gd.makeCommander(player1.getId(), commander);
        Permanent guardian = addCreatureReady(player1, new LoyalGuardian());
        Permanent ownCreature = addCreatureReady(player1, new SolemnSimulacrum());
        addCreatureReady(player1, commander);

        advanceToCombat(player2);

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Controlling an opponent's commander does not satisfy lieutenant")
    void doesNotTriggerWithOnlyAnOpponentsCommander() {
        Card commander = new CaptainAmericaUnbowed();
        gd.makeCommander(player2.getId(), commander);
        Permanent stolenCommander = addCreatureReady(player1, commander);
        Permanent guardian = addCreatureReady(player1, new LoyalGuardian());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        harness.passBothPriorities();
        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(stolenCommander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A commander controlled by an opponent does not satisfy lieutenant")
    void doesNotTriggerWhenOpponentControlsYourCommander() {
        Card commander = new CaptainAmericaUnbowed();
        gd.makeCommander(player1.getId(), commander);
        addCreatureReady(player2, commander);
        Permanent guardian = addCreatureReady(player1, new LoyalGuardian());

        advanceToCombat(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lieutenant rechecks commander control when it resolves")
    void doesNothingIfYourCommanderChangesControlBeforeResolution() {
        Card commander = new CaptainAmericaUnbowed();
        gd.makeCommander(player1.getId(), commander);
        Permanent commanderPermanent = addCreatureReady(player1, commander);
        Permanent guardian = addCreatureReady(player1, new LoyalGuardian());

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(commanderPermanent);
        gd.playerBattlefields.get(player2.getId()).add(commanderPermanent);
        harness.passBothPriorities();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(commanderPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's commander cannot keep lieutenant active after yours is lost")
    void opponentsCommanderDoesNotSatisfyResolutionCondition() {
        Card ownCommander = new CaptainAmericaUnbowed();
        Card opponentCommander = new CaptainAmericaUnbowed();
        gd.makeCommander(player1.getId(), ownCommander);
        gd.makeCommander(player2.getId(), opponentCommander);
        Permanent ownCommanderPermanent = addCreatureReady(player1, ownCommander);
        Permanent stolenCommander = addCreatureReady(player2, opponentCommander);
        Permanent guardian = addCreatureReady(player1, new LoyalGuardian());

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(ownCommanderPermanent);
        gd.playerBattlefields.get(player2.getId()).remove(stolenCommander);
        gd.playerBattlefields.get(player1.getId()).add(stolenCommander);
        gd.playerBattlefields.get(player2.getId()).add(ownCommanderPermanent);
        harness.passBothPriorities();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(stolenCommander.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gaining your commander after combat begins does not create a trigger")
    void gainingCommanderAfterCombatBeginsDoesNotTrigger() {
        Card commander = new CaptainAmericaUnbowed();
        gd.makeCommander(player1.getId(), commander);
        Permanent guardian = addCreatureReady(player1, new LoyalGuardian());

        advanceToCombat(player1);
        assertThat(gd.stack).isEmpty();
        Permanent commanderPermanent = addCreatureReady(player1, commander);
        harness.passBothPriorities();

        assertThat(guardian.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(commanderPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Lieutenant affects creatures present on resolution and excludes noncreatures")
    void usesCreaturesPresentOnResolutionEvenIfGuardianLeaves() {
        Card commander = new CaptainAmericaUnbowed();
        gd.makeCommander(player1.getId(), commander);
        Permanent commanderPermanent = addCreatureReady(player1, commander);
        Permanent guardian = addCreatureReady(player1, new LoyalGuardian());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new SolRing());

        advanceToCombat(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(guardian);
        gd.playerGraveyards.get(player1.getId()).add(guardian.getCard());
        Permanent newCreature = addCreatureReady(player1, new SolemnSimulacrum());
        harness.passBothPriorities();

        assertThat(commanderPermanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(newCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(artifact.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void advanceToCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
