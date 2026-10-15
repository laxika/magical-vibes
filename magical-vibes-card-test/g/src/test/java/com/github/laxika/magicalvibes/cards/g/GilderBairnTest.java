package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GilderBairn.class, GrizzlyBears.class})
class GilderBairnTest extends BaseCardTest {

    @Test
    @DisplayName("Doubles +1/+1 counters on the target permanent (3 becomes 6) and untaps the source")
    void doublesPlusOnePlusOneCounters() {
        Permanent bairn = addReadyBairn(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        prepareTurn();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        // Grizzly Bears 2/2 base + six +1/+1 counters.
        assertThat(bears.getEffectivePower()).isEqualTo(8);
        assertThat(bears.getEffectiveToughness()).isEqualTo(8);
        // Paying {Q} untapped the source.
        assertThat(bairn.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Doubles every kind of counter present on the target")
    void doublesEachKindOfCounter() {
        addReadyBairn(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bears.setCounterCount(CounterType.CHARGE, 3);
        prepareTurn();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(bears.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Does nothing when the target permanent has no counters")
    void noOpWhenTargetHasNoCounters() {
        addReadyBairn(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // no counters
        prepareTurn();

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(0);
        assertThat(bears.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate while the source is untapped ({Q} requires it tapped)")
    void cannotActivateWhileUntapped() {
        addCreatureReady(player1, new GilderBairn());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareTurn();

        assertThatThrownBy(() ->
                harness.activateAbility(player1, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can pay the hybrid cost with blue and target itself")
    void paysBlueAndTargetsItself() {
        Permanent bairn = addReadyBairn(player1);
        bairn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, bairn.getId());

        assertThat(bairn.isTapped()).isFalse();
        assertThat(bairn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(bairn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("Doubles counters on an opponent's permanent using the count at resolution")
    void usesOpponentCountersAtResolution() {
        addReadyBairn(player1);
        Permanent target = addCreatureReady(player2, new GilderBairn());
        target.setCounterCount(CounterType.CHARGE, 1);
        prepareTurn();

        harness.activateAbility(player1, 0, null, target.getId());
        target.setCounterCount(CounterType.CHARGE, 3);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot pay the untap cost while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent bairn = addReadyBairn(player1);
        bairn.setSummoningSick(true);
        prepareTurn();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bairn.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(bairn.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Doubles minus counters and puts a lethally reduced creature into the graveyard")
    void doublesMinusCounters() {
        addReadyBairn(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        prepareTurn();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("An ability still resolves after its source leaves the battlefield")
    void resolvesWithoutSource() {
        Permanent bairn = addReadyBairn(player1);
        Permanent target = addCreatureReady(player2, new GilderBairn());
        target.setCounterCount(CounterType.CHARGE, 2);
        prepareTurn();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(bairn);
        gd.playerGraveyards.get(player1.getId()).add(bairn.getCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
    }

    private void prepareTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 3);
    }

    private Permanent addReadyBairn(Player player) {
        Permanent perm = addCreatureReady(player, new GilderBairn());
        perm.tap(); // {Q} requires the source to be tapped
        return perm;
    }
}
