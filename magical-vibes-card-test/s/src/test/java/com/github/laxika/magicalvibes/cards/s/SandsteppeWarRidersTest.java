package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AbzanFalconer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SandsteppeWarRiders.class, AbzanFalconer.class})
class SandsteppeWarRidersTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of combat bolsters for differently named artifact tokens")
    void bolstersForDistinctArtifactTokenNames() {
        harness.addToBattlefield(player1, artifactToken("Clue"));
        harness.addToBattlefield(player1, artifactToken("Treasure"));
        harness.addToBattlefield(player1, artifactToken("Treasure"));
        harness.addToBattlefield(player1, new SandsteppeWarRiders());
        Permanent target = harness.addToBattlefieldAndReturn(player1, targetCreature());

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not count ordinary artifacts or opponent's artifact tokens")
    void countsOnlyControlledArtifactTokens() {
        harness.addToBattlefield(player1, artifactPermanent("Ordinary Artifact"));
        harness.addToBattlefield(player1, artifactToken("Clue"));
        harness.addToBattlefield(player2, artifactToken("Treasure"));
        harness.addToBattlefield(player1, new SandsteppeWarRiders());
        Permanent target = harness.addToBattlefieldAndReturn(player1, targetCreature());

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger on an opponent's beginning of combat")
    void doesNotTriggerOnOpponentsCombat() {
        harness.addToBattlefield(player1, artifactToken("Clue"));
        harness.addToBattlefield(player1, artifactToken("Treasure"));
        harness.addToBattlefield(player1, new SandsteppeWarRiders());
        Permanent target = harness.addToBattlefieldAndReturn(player1, targetCreature());

        advanceToBeginningOfCombat(player2);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void noArtifactTokensMeansNoCounters() {
        Permanent riders = harness.addToBattlefieldAndReturn(player1, new SandsteppeWarRiders());

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(riders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void countsArtifactTokensAtResolution() {
        Permanent clue = harness.addToBattlefieldAndReturn(player1, artifactToken("Clue"));
        harness.addToBattlefield(player1, artifactToken("Treasure"));
        harness.addToBattlefield(player1, new SandsteppeWarRiders());
        Permanent falconer = harness.addToBattlefieldAndReturn(player1, new AbzanFalconer());

        advanceToBeginningOfCombat(player1);
        gd.playerBattlefields.get(player1.getId()).remove(clue);
        harness.passBothPriorities();

        assertThat(falconer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void controllerChoosesAmongCreaturesTiedForLeastToughness() {
        harness.addToBattlefield(player1, artifactToken("Clue"));
        Permanent riders = harness.addToBattlefieldAndReturn(player1, new SandsteppeWarRiders());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AbzanFalconer());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AbzanFalconer());

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(riders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void choosesLeastToughnessUsingExistingCounters() {
        harness.addToBattlefield(player1, artifactToken("Treasure"));
        Permanent riders = harness.addToBattlefieldAndReturn(player1, new SandsteppeWarRiders());
        Permanent falconer = harness.addToBattlefieldAndReturn(player1, new AbzanFalconer());
        falconer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        advanceToBeginningOfCombat(player1);
        harness.passBothPriorities();

        assertThat(riders.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(falconer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private Card targetCreature() {
        Card card = new Card();
        card.setName("Target Creature");
        card.setManaCost("");
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Card artifactToken(String name) {
        Card card = artifactPermanent(name);
        card.setToken(true);
        return card;
    }

    private Card artifactPermanent(String name) {
        Card card = new Card();
        card.setName(name);
        card.setManaCost("");
        card.setType(CardType.ARTIFACT);
        return card;
    }

    private void advanceToBeginningOfCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.BEGINNING_OF_COMBAT);
    }
}
