package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SabotageStrategist.class, GrizzlyBears.class})
@DisplayName("Sabotage Strategist")
class SabotageStrategistTest extends BaseCardTest {

    @Test
    @DisplayName("Shrinks creatures attacking its controller")
    void shrinksAttackingCreatures() {
        harness.addToBattlefield(player1, new SabotageStrategist());
        Permanent attacker1 = addReadyAttacker();
        Permanent attacker2 = addReadyAttacker();

        declareAttackers(player2, List.of(0, 1));
        assertThat(gd.stack).hasSize(1);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gqs.getEffectivePower(gd, attacker1)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, attacker2)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exhaust puts three +1/+1 counters on it and can be used only once")
    void exhaustAbility() {
        Permanent strategist = addReadyStrategist();
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(strategist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        addExhaustMana();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");
    }

    @Test
    @DisplayName("The original attacker is weakened even after being removed from combat")
    void weakensOriginalAttackerRemovedFromCombat() {
        harness.addToBattlefield(player1, new SabotageStrategist());
        Permanent attacker = addReadyAttacker();

        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).hasSize(1);
        attacker.setAttacking(false);
        attacker.setAttackTarget(null);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature put onto the battlefield attacking after the trigger is not weakened")
    void excludesCreatureEnteringAttackingAfterTrigger() {
        harness.addToBattlefield(player1, new SabotageStrategist());
        Permanent originalAttacker = addReadyAttacker();

        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).hasSize(1);
        Permanent laterAttacker = addReadyAttacker();
        laterAttacker.setAttacking(true);
        laterAttacker.setAttackTarget(player1.getId());

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gqs.getEffectivePower(gd, originalAttacker)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, laterAttacker)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack trigger resolves after its source leaves the battlefield")
    void attackTriggerSurvivesSourceLeaving() {
        harness.addToBattlefield(player1, new SabotageStrategist());
        Permanent attacker = addReadyAttacker();

        declareAttackers(player2, List.of(0));
        assertThat(gd.stack).hasSize(1);
        Permanent strategist = gd.playerBattlefields.get(player1.getId()).removeFirst();
        gd.playerGraveyards.get(player1.getId()).add(strategist.getCard());

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exhaust is usable while summoning sick and cannot be activated again while pending")
    void exhaustWorksWhileSummoningSickAndIsSpentOnActivation() {
        Permanent strategist = harness.addToBattlefieldAndReturn(player1, new SabotageStrategist());
        addExhaustMana();

        harness.activateAbility(player1, 0, 0, null, null);
        addExhaustMana();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once");

        harness.passBothPriorities();

        assertThat(strategist.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private Permanent addReadyAttacker() {
        return addCreatureReady(player2, new GrizzlyBears());
    }

    private Permanent addReadyStrategist() {
        return addCreatureReady(player1, new SabotageStrategist());
    }

    private void addExhaustMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
