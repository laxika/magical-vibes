package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AmaranthineWall;
import com.github.laxika.magicalvibes.cards.s.SkitterEel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BiomancersFamiliar.class, AmaranthineWall.class, SkitterEel.class})
class BiomancersFamiliarTest extends BaseCardTest {

    @Test
    void keepsOneManaMinimumForGenericOnlyAbility() {
        harness.addToBattlefield(player1, new BiomancersFamiliar());
        harness.addToBattlefield(player1, new AmaranthineWall());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void reducesCreatureActivatedAbilityCost() {
        addCreatureReady(player1, new BiomancersFamiliar());
        Permanent eel = addCreatureReady(player1, new SkitterEel());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(eel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void nextAdaptIgnoresCountersOnce() {
        Permanent familiar = addCreatureReady(player1, new BiomancersFamiliar());
        Permanent eel = addCreatureReady(player1, new SkitterEel());
        eel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, eel.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(eel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(eel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(familiar.isTapped()).isTrue();
    }

    @Test
    void doesNotReduceOpponentsCreatureActivatedAbilityCost() {
        addCreatureReady(player1, new BiomancersFamiliar());
        addCreatureReady(player2, new SkitterEel());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void overlappingPermissionsApplyToTheSameNextAdapt() {
        addCreatureReady(player1, new BiomancersFamiliar());
        addCreatureReady(player1, new BiomancersFamiliar());
        Permanent eel = addCreatureReady(player1, new SkitterEel());
        eel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, eel.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, eel.getId());
        harness.passBothPriorities();
        assertThat(eel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();
        assertThat(eel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();
        assertThat(eel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void canAllowAnOpponentsCreatureToAdapt() {
        addCreatureReady(player1, new BiomancersFamiliar());
        Permanent eel = addCreatureReady(player2, new SkitterEel());
        eel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, eel.getId());
        harness.passBothPriorities();
        assertThat(eel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        assertThat(eel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void unusedAdaptPermissionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new BiomancersFamiliar());
        Permanent eel = addCreatureReady(player1, new SkitterEel());
        eel.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 0, null, eel.getId());
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        assertThat(eel.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void multipleFamiliarsStillRequireOneManaForGenericOnlyAbility() {
        harness.addToBattlefield(player1, new BiomancersFamiliar());
        harness.addToBattlefield(player1, new BiomancersFamiliar());
        harness.addToBattlefield(player1, new AmaranthineWall());

        assertThatThrownBy(() -> harness.activateAbility(player1, 2, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 2, null, null);
        harness.passBothPriorities();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
