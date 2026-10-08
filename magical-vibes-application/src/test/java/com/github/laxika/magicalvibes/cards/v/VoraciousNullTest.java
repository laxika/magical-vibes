package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CullingDrone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoraciousNull.class, CullingDrone.class})
class VoraciousNullTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature puts two +1/+1 counters on Voracious Null")
    void sacrificingAnotherCreaturePutsTwoCountersOnItself() {
        Permanent nullCreature = addCreatureReady(player1, new VoraciousNull());
        harness.addToBattlefield(player1, new CullingDrone());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(nullCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Culling Drone");
        harness.assertOnBattlefield(player1, "Voracious Null");
    }

    @Test
    @DisplayName("The ability cannot sacrifice Voracious Null itself")
    void abilityRequiresAnotherCreature() {
        addCreatureReady(player1, new VoraciousNull());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability can only be activated as a sorcery")
    void abilityRequiresSorcerySpeed() {
        addCreatureReady(player1, new VoraciousNull());
        harness.addToBattlefield(player1, new CullingDrone());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    void sacrificeIsPaidBeforeCountersResolve() {
        Permanent nullCreature = addCreatureReady(player1, new VoraciousNull());
        harness.addToBattlefield(player1, new CullingDrone());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Culling Drone");
        harness.assertNotOnBattlefield(player1, "Culling Drone");
        assertThat(nullCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(nullCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void cannotSacrificeOpponentsCreature() {
        addCreatureReady(player1, new VoraciousNull());
        harness.addToBattlefield(player2, new CullingDrone());
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Culling Drone");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateDuringOpponentsMainPhase() {
        addCreatureReady(player1, new VoraciousNull());
        harness.addToBattlefield(player1, new CullingDrone());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        harness.assertOnBattlefield(player1, "Culling Drone");
    }

    @Test
    void cannotActivateWhileStackIsNotEmpty() {
        addCreatureReady(player1, new VoraciousNull());
        harness.addToBattlefield(player1, new CullingDrone());
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.activateAbility(player1, 0, null, null);
        harness.addToBattlefield(player1, new CullingDrone());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.assertOnBattlefield(player1, "Culling Drone");
        harness.passBothPriorities();
    }

    @Test
    void tappedSummoningSickCreatureCanActivateRepeatedly() {
        Permanent nullCreature = harness.addToBattlefieldAndReturn(player1, new VoraciousNull());
        nullCreature.setSummoningSick(true);
        nullCreature.tap();
        harness.addMana(player1, ManaColor.BLACK, 4);

        for (int activation = 0; activation < 2; activation++) {
            harness.addToBattlefield(player1, new CullingDrone());
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(nullCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(nullCreature.isTapped()).isTrue();
    }
}
