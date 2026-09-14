package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonsparkReactor.class, GrizzlyBears.class, Spellbook.class})
class DragonsparkReactorTest extends BaseCardTest {

    @Test
    void entersWithAChargeCounter() {
        harness.setHand(player1, List.of(new DragonsparkReactor()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent reactor = findPermanent(player1, "Dragonspark Reactor");
        assertThat(reactor.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void anotherArtifactEnteringAddsAChargeCounter() {
        Permanent reactor = harness.addToBattlefieldAndReturn(player1, new DragonsparkReactor());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(reactor.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void abilityDealsChargeCountersDamageToPlayerAndOptionalCreature() {
        Permanent reactor = harness.addToBattlefieldAndReturn(player1, new DragonsparkReactor());
        reactor.setCounterCount(CounterType.CHARGE, 1);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int reactorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reactor);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1, reactorIndex, 0,
                List.of(player2.getId(), bear.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(bear.getMarkedDamage()).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Dragonspark Reactor");
    }

    @Test
    void abilityCanOmitCreatureTarget() {
        Permanent reactor = harness.addToBattlefieldAndReturn(player1, new DragonsparkReactor());
        reactor.setCounterCount(CounterType.CHARGE, 3);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int reactorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reactor);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1, reactorIndex, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(bear.getMarkedDamage()).isZero();
    }

    @Test
    void abilityUsesChargeCounterCountAfterSacrificeCost() {
        Permanent reactor = harness.addToBattlefieldAndReturn(player1, new DragonsparkReactor());
        reactor.setCounterCount(CounterType.CHARGE, 5);
        int reactorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reactor);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbilityWithMultiTargets(player1, reactorIndex, 0, List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    void firstTargetMustBeAPlayer() {
        Permanent reactor = harness.addToBattlefieldAndReturn(player1, new DragonsparkReactor());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        int reactorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(reactor);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, reactorIndex, 0, List.of(bear.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
