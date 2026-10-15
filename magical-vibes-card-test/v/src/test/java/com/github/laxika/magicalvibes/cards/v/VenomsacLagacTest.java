package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VenomsacLagac.class, GrizzlyBears.class})
class VenomsacLagacTest extends BaseCardTest {

    @Test
    @DisplayName("Saddle 2 taps another creature and saddles Venomsac Lagac")
    void saddleTapsAnotherCreature() {
        Permanent lagac = addCreatureReady(player1, new VenomsacLagac());
        Permanent helper = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(lagac.isSaddled()).isTrue();
        assertThat(helper.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking while saddled gives Venomsac Lagac +0/+3 until end of turn")
    void attacksWhileSaddled() {
        Permanent lagac = addCreatureReady(player1, new VenomsacLagac());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lagac)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lagac)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(lagac.isSaddled()).isFalse();
        assertThat(gqs.getEffectivePower(gd, lagac)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lagac)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking while not saddled does not trigger")
    void doesNotTriggerWhenNotSaddled() {
        Permanent lagac = addCreatureReady(player1, new VenomsacLagac());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lagac)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lagac)).isEqualTo(1);
    }

    @Test
    @DisplayName("Saddle cannot tap the Mount itself or an opponent's creature")
    void cannotSaddleWithoutOtherControlledCreatures() {
        Permanent lagac = addCreatureReady(player1, new VenomsacLagac());
        Permanent opponent = addCreatureReady(player2, new VenomsacLagac());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lagac.isTapped()).isFalse();
        assertThat(lagac.isSaddled()).isFalse();
        assertThat(opponent.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A summoning-sick creature can pay the saddle cost")
    void summoningSickCreatureCanSaddle() {
        Permanent lagac = addCreatureReady(player1, new VenomsacLagac());
        Permanent helper = addCreatureReady(player1, new VenomsacLagac());
        helper.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        assertThat(helper.isTapped()).isTrue();
        assertThat(lagac.isSaddled()).isFalse();

        harness.passBothPriorities();

        assertThat(lagac.isSaddled()).isTrue();
        assertThat(lagac.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Tapped creatures cannot pay the saddle cost")
    void tappedCreatureCannotSaddle() {
        Permanent lagac = addCreatureReady(player1, new VenomsacLagac());
        Permanent helper = addCreatureReady(player1, new VenomsacLagac());
        helper.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(lagac.isSaddled()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Saddle can only be activated during a main phase")
    void cannotSaddleDuringCombat() {
        Permanent lagac = addCreatureReady(player1, new VenomsacLagac());
        Permanent helper = addCreatureReady(player1, new VenomsacLagac());
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helper.isTapped()).isFalse();
        assertThat(lagac.isSaddled()).isFalse();
    }

    @Test
    @DisplayName("Saddle cannot be activated with another ability on the stack")
    void cannotSaddleWithNonemptyStack() {
        Permanent lagac = addCreatureReady(player1, new VenomsacLagac());
        addCreatureReady(player1, new VenomsacLagac());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.passBothPriorities();
        assertThat(lagac.isSaddled()).isTrue();
    }

    @Test
    @DisplayName("Saddle requires at least two total power from other creatures")
    void insufficientPowerCannotSaddle() {
        Permanent lagac = addCreatureReady(player1, new VenomsacLagac());
        Permanent helper = addCreatureReady(player1, new VenomsacLagac());
        helper.setPowerModifier(-1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(helper.isTapped()).isFalse();
        assertThat(lagac.isSaddled()).isFalse();
    }

    @Test
    @DisplayName("Two one-power creatures can combine to pay Saddle 2")
    void multipleCreaturesCanSaddle() {
        Permanent lagac = addCreatureReady(player1, new VenomsacLagac());
        Permanent first = addCreatureReady(player1, new VenomsacLagac());
        Permanent second = addCreatureReady(player1, new VenomsacLagac());
        first.setPowerModifier(-1);
        second.setPowerModifier(-1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(lagac.isTapped()).isFalse();
        assertThat(lagac.isSaddled()).isTrue();
    }

    @Test
    @DisplayName("Deathtouch destroys a blocker even when combat damage is below its toughness")
    void deathtouchKillsLargerBlocker() {
        Permanent lagac = addCreatureReady(player1, new VenomsacLagac());
        Permanent blocker = addCreatureReady(player2, new VenomsacLagac());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(lagac.getCard());
        harness.assertLife(player2, 20);
    }
}
