package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Scuttlegator.class})
class ScuttlegatorTest extends BaseCardTest {

    @Test
    @DisplayName("Adapt 3 puts three +1/+1 counters on Scuttlegator")
    void adaptPutsThreeCountersOnScuttlegator() {
        Permanent scuttlegator = addScuttlegator();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(scuttlegator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot attack while Scuttlegator has no +1/+1 counters")
    void cannotAttackWithoutCounter() {
        Permanent scuttlegator = addScuttlegator();
        prepareAttack();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(scuttlegator))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Can attack while Scuttlegator has a +1/+1 counter")
    void canAttackWithCounter() {
        Permanent scuttlegator = addScuttlegator();
        scuttlegator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        prepareAttack();

        gs.declareAttackers(gd, player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(scuttlegator)));

        assertThat(scuttlegator.isAttacking()).isTrue();
    }

    @Test
    void adaptDoesNothingWhenCounterAlreadyPresent() {
        Permanent scuttlegator = addScuttlegator();
        scuttlegator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(scuttlegator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void adaptChecksCountersAtResolution() {
        Permanent scuttlegator = addScuttlegator();
        addAdaptMana();
        harness.activateAbility(player1, 0, null, null);

        scuttlegator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(scuttlegator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void adaptWorksIfLastCounterIsRemovedBeforeResolution() {
        Permanent scuttlegator = addScuttlegator();
        scuttlegator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addAdaptMana();
        harness.activateAbility(player1, 0, null, null);

        scuttlegator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        assertThat(scuttlegator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void twoPendingAdaptAbilitiesAddCountersOnlyOnce() {
        Permanent scuttlegator = addScuttlegator();
        addAdaptMana();
        addAdaptMana();

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(scuttlegator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void losingLastCounterRestoresDefenderRestriction() {
        Permanent scuttlegator = addScuttlegator();
        scuttlegator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        scuttlegator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        prepareAttack();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(scuttlegator))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    private Permanent addScuttlegator() {
        Permanent scuttlegator = harness.addToBattlefieldAndReturn(player1, new Scuttlegator());
        scuttlegator.setSummoningSick(false);
        return scuttlegator;
    }

    private void addAdaptMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);
    }

    private void prepareAttack() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));
    }
}
