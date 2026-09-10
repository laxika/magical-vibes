package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
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

@CardUsed({SwarmShambler.class, Shock.class, GrizzlyBears.class, ProdigalPyromancer.class})
class SwarmShamblerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter")
    void entersWithCounter() {
        Permanent shambler = harness.addToBattlefieldAndReturn(player1, new SwarmShambler());

        assertThat(shambler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates an Insect when an opponent targets a creature with a +1/+1 counter")
    void createsInsectWhenOpponentTargetsCounteredCreature() {
        Permanent shambler = harness.addToBattlefieldAndReturn(player1, new SwarmShambler());
        castOpponentShock(shambler);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(Permanent::isToken).hasSize(1);
    }

    @Test
    @DisplayName("Does not create an Insect when the targeted creature has no +1/+1 counter")
    void doesNotCreateInsectForCreatureWithoutCounter() {
        harness.addToBattlefield(player1, new SwarmShambler());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castOpponentShock(bears);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not create an Insect when its controller casts the targeting spell")
    void doesNotCreateInsectForOwnSpell() {
        Permanent shambler = harness.addToBattlefieldAndReturn(player1, new SwarmShambler());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, shambler.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Does not create an Insect when an opponent's ability targets it")
    void doesNotCreateInsectForOpponentAbility() {
        Permanent shambler = harness.addToBattlefieldAndReturn(player1, new SwarmShambler());
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);

        harness.activateAbility(player2, 0, null, shambler.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when its ability resolves")
    void activatedAbilityPutsCounterOnItself() {
        Permanent shambler = harness.addToBattlefieldAndReturn(player1, new SwarmShambler());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(shambler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(shambler.isTapped()).isTrue();
    }

    private void castOpponentShock(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, target.getId());
    }
}
