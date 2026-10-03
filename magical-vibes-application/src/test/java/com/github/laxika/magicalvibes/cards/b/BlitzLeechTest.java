package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlitzLeech.class, AirElemental.class})
class BlitzLeechTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gives an opponent's creature -2/-2 and removes all counters from it")
    void etbWeakensOpponentCreatureAndRemovesCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        target.setCounterCount(CounterType.CHARGE, 3);

        castBlitzLeech(target);
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isEqualTo(-2);
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("ETB debuff wears off at end of turn")
    void debuffWearsOffAtEndOfTurn() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        castBlitzLeech(target);
        resolveAllTriggers();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("ETB cannot target a creature you control")
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.setHand(player1, List.of(new BlitzLeech()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature an opponent controls");
    }

    @Test
    @DisplayName("Removing +1/+1 counters can make the weakened creature die")
    void counterRemovalCanKillTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BlitzLeech());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        castBlitzLeech(target);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Blitz Leech");
        harness.assertInGraveyard(player2, "Blitz Leech");
        harness.assertOnBattlefield(player1, "Blitz Leech");
    }

    @Test
    @DisplayName("Counter removal finishes before checking whether the weakened creature dies")
    void removingNegativeCountersSavesTargetDuringResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        castBlitzLeech(target);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Air Elemental");
        harness.assertNotInGraveyard(player2, "Air Elemental");
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(target.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("A creature that leaves before the trigger resolves is not affected")
    void missingTargetMakesTriggerFizzle() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        target.setCounterCount(CounterType.CHARGE, 3);

        castBlitzLeech(target);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(target);
        resolveAllTriggers();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Blitz Leech");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's turn with no legal ETB target")
    void flashWithNoOpponentCreature() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.setHand(player1, List.of(new BlitzLeech()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Blitz Leech");
        assertThat(gd.stack).isEmpty();
    }

    private void castBlitzLeech(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new BlitzLeech()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castCreature(player1, 0, 0, target.getId());
    }

}
