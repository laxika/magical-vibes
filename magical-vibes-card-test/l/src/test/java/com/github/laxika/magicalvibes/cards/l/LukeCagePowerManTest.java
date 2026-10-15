package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LukeCagePowerMan.class, GrizzlyBears.class})
class LukeCagePowerManTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking alone gives Luke +2/+0 and indestructible until end of turn")
    void attackingAloneGrantsBoostAndIndestructible() {
        Permanent luke = addCreatureReady(player1, new LukeCagePowerMan());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, luke)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, luke)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, luke, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, luke)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, luke)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, luke, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Attacking with another creature does not trigger Luke's ability")
    void doesNotTriggerWhenAttackingWithAnotherCreature() {
        Permanent luke = addCreatureReady(player1, new LukeCagePowerMan());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0, 1));

        assertThat(gqs.getEffectivePower(gd, luke)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, luke)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, luke, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Luke's bonus waits for the attack trigger to resolve")
    void bonusRequiresTriggerResolution() {
        Permanent luke = addCreatureReady(player1, new LukeCagePowerMan());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, luke)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, luke, Keyword.INDESTRUCTIBLE)).isFalse();

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, luke)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, luke, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    @DisplayName("Other creatures staying home do not prevent Luke from attacking alone")
    void nonattackingAllyDoesNotPreventBonus() {
        Permanent luke = addCreatureReady(player1, new LukeCagePowerMan());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, luke)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, luke, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    @DisplayName("Luke survives lethal combat damage after attacking alone")
    void indestructiblePreventsLethalCombatDamage() {
        Permanent luke = addCreatureReady(player1, new LukeCagePowerMan());
        Permanent blocker = addCreatureReady(player2, new LukeCagePowerMan());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            resolveAllTriggers();
        });
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(luke);
        assertThat(luke.getMarkedDamage()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, luke, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
        assertThat(blocker.getMarkedDamage()).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, blocker, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
