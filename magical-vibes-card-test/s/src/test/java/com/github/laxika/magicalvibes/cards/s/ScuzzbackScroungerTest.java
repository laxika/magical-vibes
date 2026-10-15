package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScuzzbackScrounger.class})
class ScuzzbackScroungerTest extends BaseCardTest {

    @Test
    void acceptingFirstMainPhaseTriggerBlightsAndCreatesTreasure() {
        Permanent scrounger = harness.addToBattlefieldAndReturn(player1, new ScuzzbackScrounger());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(scrounger.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void decliningFirstMainPhaseTriggerDoesNothing() {
        Permanent scrounger = harness.addToBattlefieldAndReturn(player1, new ScuzzbackScrounger());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(scrounger.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void blightCanChooseAnotherControlledCreature() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ScuzzbackScrounger());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ScuzzbackScrounger());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ScuzzbackScrounger());

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, other.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(source.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(other.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    void lethalBlightStillCreatesTreasure() {
        Permanent scrounger = harness.addToBattlefieldAndReturn(player1, new ScuzzbackScrounger());
        scrounger.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        advanceToPrecombatMain(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(scrounger);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(scrounger.getCard());
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(1);
    }

    @Test
    void doesNotTriggerDuringOpponentsFirstMainPhase() {
        Permanent scrounger = harness.addToBattlefieldAndReturn(player1, new ScuzzbackScrounger());

        advanceToPrecombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(scrounger.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    @Test
    void noTreasureWhenNoControlledCreatureRemainsAtResolution() {
        Permanent scrounger = harness.addToBattlefieldAndReturn(player1, new ScuzzbackScrounger());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new ScuzzbackScrounger());

        advanceToPrecombatMain(player1);
        gd.playerBattlefields.get(player1.getId()).remove(scrounger);
        gd.playerGraveyards.get(player1.getId()).add(scrounger.getCard());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(opponent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void doesNotTriggerDuringSecondMainPhase() {
        harness.addToBattlefield(player1, new ScuzzbackScrounger());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(countPermanents(player1, "Treasure")).isZero();
    }

    private void advanceToPrecombatMain(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(player, TurnStep.PRECOMBAT_MAIN);
    }
}
