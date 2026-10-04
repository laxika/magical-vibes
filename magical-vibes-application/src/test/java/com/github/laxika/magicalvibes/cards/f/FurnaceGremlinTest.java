package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.StokeTheFlames;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FurnaceGremlin.class, StokeTheFlames.class})
class FurnaceGremlinTest extends BaseCardTest {

    @Test
    @DisplayName("Its ability gives it +1/+0 until end of turn")
    void activatedAbilityBoostsPower() {
        Permanent gremlin = harness.addToBattlefieldAndReturn(player1, new FurnaceGremlin());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, gremlin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, gremlin)).isEqualTo(2);

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gqs.getEffectivePower(gd, gremlin)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, gremlin)).isEqualTo(2);
    }

    @Test
    @DisplayName("When it dies, it incubates X where X is its power")
    void deathIncubatesItsPower() {
        Permanent gremlin = harness.addToBattlefieldAndReturn(player1, new FurnaceGremlin());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThat(gqs.getEffectivePower(gd, gremlin)).isEqualTo(2);
        harness.setHand(player2, java.util.List.of(new StokeTheFlames()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castAndResolveInstant(player2, 0, gremlin.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Furnace Gremlin");
        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Repeated activations accumulate and its Incubator transforms with those counters")
    void repeatedBoostsAndTokenTransformation() {
        Permanent gremlin = harness.addToBattlefieldAndReturn(player1, new FurnaceGremlin());
        harness.addMana(player1, ManaColor.RED, 4);
        for (int i = 0; i < 2; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }
        assertThat(gqs.getEffectivePower(gd, gremlin)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, gremlin)).isEqualTo(2);

        gremlin.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(gqs.isCreature(gd, incubator)).isFalse();
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(incubator.isTransformed()).isTrue();
        assertThat(gqs.isCreature(gd, incubator)).isTrue();
        assertThat(gqs.isArtifact(gd, incubator)).isTrue();
        assertThat(gqs.getEffectivePower(gd, incubator)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, incubator)).isEqualTo(3);
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Zero power still creates an Incubator that dies when transformed")
    void zeroPowerIncubatorDiesAfterTransformation() {
        Permanent gremlin = harness.addToBattlefieldAndReturn(player1, new FurnaceGremlin());
        gremlin.setPersistentPowerModifier(-1);
        gremlin.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        Permanent incubator = findPermanent(player1, "Incubator");
        assertThat(incubator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, incubator)).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }
}
