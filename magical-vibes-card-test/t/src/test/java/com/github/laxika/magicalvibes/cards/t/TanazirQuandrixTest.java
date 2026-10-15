package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.ArcaneSubtraction;
import com.github.laxika.magicalvibes.cards.b.BuryInBooks;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({TanazirQuandrix.class, AirElemental.class, GrizzlyBears.class,
        ArcaneSubtraction.class, BuryInBooks.class})
class TanazirQuandrixTest extends BaseCardTest {

    @Test
    @DisplayName("ETB doubles the +1/+1 counters on a creature you control")
    void etbDoublesCountersOnOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.setHand(player1, List.of(new TanazirQuandrix()));
        addManaToCastTanazir();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void etbCannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new TanazirQuandrix()));
        addManaToCastTanazir();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Attacking optionally sets other creatures' base power and toughness to Tanazir's actual stats")
    void attackSetsOtherCreaturesBaseStats() {
        Permanent tanazir = addCreatureReady(player1, new TanazirQuandrix());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        tanazir.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(tanazir.getEffectivePower()).isEqualTo(5);
        assertThat(tanazir.getEffectiveToughness()).isEqualTo(5);
        assertThat(other.getEffectivePower()).isEqualTo(5);
        assertThat(other.getEffectiveToughness()).isEqualTo(5);
        assertThat(opponent.getEffectivePower()).isEqualTo(2);
        assertThat(opponent.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the attack trigger leaves other creatures unchanged")
    void decliningAttackTriggerDoesNothing() {
        addCreatureReady(player1, new TanazirQuandrix());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(other.getEffectivePower()).isEqualTo(2);
        assertThat(other.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Doubling zero counters does not add counters")
    void etbWithNoCountersDoesNothing() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TanazirQuandrix()));
        addManaToCastTanazir();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("ETB doubles the counter count at resolution, not when it triggers")
    void etbUsesCurrentCounterCount() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new TanazirQuandrix()));
        addManaToCastTanazir();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Attack trigger uses current stats and preserves counters on other creatures")
    void attackUsesResolutionStatsAndLeavesOtherCountersApplied() {
        Permanent tanazir = addCreatureReady(player1, new TanazirQuandrix());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        other.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        declareAttackers(player1, List.of(0));
        tanazir.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(9);
        tanazir.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(9);

        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, lateCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, lateCreature)).isEqualTo(2);

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attack trigger sets negative base power without clamping it to zero")
    void attackPreservesNegativeSourcePower() {
        Permanent tanazir = addCreatureReady(player1, new TanazirQuandrix());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        for (int i = 0; i < 2; i++) {
            harness.setHand(player1, List.of(new ArcaneSubtraction()));
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.castAndResolveInstant(player1, 0, tanazir.getId());
        }
        assertThat(gqs.getEffectivePower(gd, tanazir)).isEqualTo(-4);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(-4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(4);
    }

    @Test
    @DisplayName("Attack trigger uses last known stats if Tanazir leaves before resolution")
    void attackUsesLastKnownStatsAfterSourceLeaves() {
        Permanent tanazir = addCreatureReady(player1, new TanazirQuandrix());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        tanazir.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player2, List.of(new BuryInBooks()));
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);

        declareAttackers(player1, List.of(0));
        harness.castAndResolveInstant(player2, 0, tanazir.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(tanazir);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(other);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(6);
    }

    private void addManaToCastTanazir() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
