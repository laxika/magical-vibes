package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GatewayPlaza;
import com.github.laxika.magicalvibes.cards.p.PrimordialWurm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CourageInCrisis.class, PrimordialWurm.class, GatewayPlaza.class})
class CourageInCrisisTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on target creature, then proliferates")
    void putsCounterThenProliferates() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        other.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        cast(target.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId(), other.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(other.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Puts the counter even when proliferate chooses none")
    void putsCounterWhenProliferateChoosesNone() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        other.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        cast(target.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GatewayPlaza());
        harness.setHand(player1, List.of(new CourageInCrisis()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void proliferatesPlayersAndEveryCounterKindOnSelectedNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new GatewayPlaza());
        land.setCounterCount(CounterType.CHARGE, 3);
        land.setCounterCount(CounterType.STUN, 1);
        gd.playerPoisonCounters.put(player1.getId(), 2);
        gd.playerPoisonCounters.put(player2.getId(), 4);
        gd.playerEnergyCounters.put(player2.getId(), 3);

        cast(target.getId());
        harness.handleMultiplePermanentsChosen(player1,
                List.of(land.getId(), player1.getId(), player2.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(land.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(land.getCounterCount(CounterType.STUN)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.get(player1.getId())).isEqualTo(3);
        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(5);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    void proliferatesBothOpposingCounterKindsBeforeTheyCancel() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        cast(target.getId());
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.handleMultiplePermanentsChosen(player1, List.of(target.getId()));

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Primordial Wurm");
    }

    @Test
    void doesNotProliferateWhenOnlyTargetLeavesBattlefield() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PrimordialWurm());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new PrimordialWurm());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new CourageInCrisis()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Courage in Crisis");
    }

    private void cast(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new CourageInCrisis()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }
}
