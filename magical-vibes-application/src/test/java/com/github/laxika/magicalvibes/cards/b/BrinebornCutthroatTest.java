package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BrinebornCutthroat.class, Shock.class})
class BrinebornCutthroatTest extends BaseCardTest {

    @Test
    @DisplayName("Controller casting a spell during an opponent's turn puts a +1/+1 counter on it")
    void controllerSpellDuringOpponentsTurnAddsCounter() {
        harness.addToBattlefield(player1, new BrinebornCutthroat());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent cutthroat = findPermanent(player1, "Brineborn Cutthroat");
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(cutthroat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(harness.getGameQueryService().getEffectivePower(gd, cutthroat)).isEqualTo(3);
        assertThat(harness.getGameQueryService().getEffectiveToughness(gd, cutthroat)).isEqualTo(2);
    }

    @Test
    @DisplayName("Controller casting a spell during their own turn does not trigger it")
    void controllerSpellDuringOwnTurnDoesNotAddCounter() {
        harness.addToBattlefield(player1, new BrinebornCutthroat());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        Permanent cutthroat = findPermanent(player1, "Brineborn Cutthroat");
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(cutthroat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent casting a spell does not trigger it")
    void opponentSpellDoesNotAddCounter() {
        harness.addToBattlefield(player1, new BrinebornCutthroat());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        Permanent cutthroat = findPermanent(player1, "Brineborn Cutthroat");
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(cutthroat.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canBeCastDuringOpponentsTurnWithoutTriggeringItself() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BrinebornCutthroat()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Brineborn Cutthroat");
        assertThat(findPermanent(player1, "Brineborn Cutthroat")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void creatureSpellTriggersExistingCutthroatBeforeEntering() {
        harness.addToBattlefield(player1, new BrinebornCutthroat());
        Permanent original = findPermanent(player1, "Brineborn Cutthroat");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BrinebornCutthroat()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();

        assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> !p.getId().equals(original.getId()))
                .allSatisfy(p -> assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void eachSpellAddsOneCounterToEachCutthroat() {
        harness.addToBattlefield(player1, new BrinebornCutthroat());
        harness.addToBattlefield(player1, new BrinebornCutthroat());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        for (int i = 0; i < 2; i++) {
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.passBothPriorities();
            int expectedCounters = i + 1;
            assertThat(gd.playerBattlefields.get(player1.getId()))
                    .allSatisfy(p -> assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                            .isEqualTo(expectedCounters));
        }
    }
}
