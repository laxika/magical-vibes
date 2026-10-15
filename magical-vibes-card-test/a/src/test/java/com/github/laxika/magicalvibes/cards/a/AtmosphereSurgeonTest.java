package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AtmosphereSurgeon.class, GrizzlyBears.class, Shock.class})
class AtmosphereSurgeonTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a noncreature spell puts an oil counter on Atmosphere Surgeon")
    void noncreatureSpellPutsOilCounter() {
        Permanent surgeon = addSurgeonReady(player1);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(surgeon.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a creature spell does not put an oil counter on Atmosphere Surgeon")
    void creatureSpellDoesNotPutOilCounter() {
        Permanent surgeon = addSurgeonReady(player1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        assertThat(surgeon.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Removing an oil counter grants a target creature flying until end of turn")
    void removesOilCounterAndGrantsFlying() {
        Permanent surgeon = addSurgeonReady(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        surgeon.setCounterCount(CounterType.OIL, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(surgeon.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The flying grant wears off at end of turn and activation is sorcery speed")
    void flyingGrantWearsOffAndActivationIsSorcerySpeed() {
        Permanent surgeon = addSurgeonReady(player1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        surgeon.setCounterCount(CounterType.OIL, 1);

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isFalse();
    }

    @Test
    void opponentsNoncreatureSpellDoesNotAddOil() {
        Permanent surgeon = addSurgeonReady(player1);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(surgeon.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    void cannotActivateWithoutOilEvenWithAnotherCounterType() {
        Permanent surgeon = addSurgeonReady(player1);
        surgeon.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, surgeon.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(surgeon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, surgeon, Keyword.FLYING)).isFalse();
    }

    @Test
    void canTargetItselfWhileTappedAndSummoningSickAndPaysOilImmediately() {
        Permanent surgeon = harness.addToBattlefieldAndReturn(player1, new AtmosphereSurgeon());
        surgeon.setSummoningSick(true);
        surgeon.tap();
        surgeon.setCounterCount(CounterType.OIL, 2);

        harness.activateAbility(player1, 0, null, surgeon.getId());

        assertThat(surgeon.getCounterCount(CounterType.OIL)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, surgeon, Keyword.FLYING)).isFalse();
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, surgeon, Keyword.FLYING)).isTrue();
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        Permanent surgeon = addSurgeonReady(player1);
        surgeon.setCounterCount(CounterType.OIL, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, surgeon.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(surgeon.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    void cannotActivateWithSpellAndCastTriggerOnStack() {
        Permanent surgeon = addSurgeonReady(player1);
        surgeon.setCounterCount(CounterType.OIL, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, surgeon.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(surgeon.getCounterCount(CounterType.OIL)).isEqualTo(1);

        resolveAllTriggers();
        assertThat(surgeon.getCounterCount(CounterType.OIL)).isEqualTo(2);
    }

    private Permanent addSurgeonReady(Player player) {
        return addCreatureReady(player, new AtmosphereSurgeon());
    }
}
