package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ViashinoBranchrider.class)
class ViashinoBranchriderTest extends BaseCardTest {

    @Test
    void entersWithoutCountersWhenNotKicked() {
        harness.setHand(player1, List.of(new ViashinoBranchrider()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent branchrider = battlefieldBranchrider();
        assertThat(branchrider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void entersWithTwoCountersWhenKicked() {
        harness.setHand(player1, List.of(new ViashinoBranchrider()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent branchrider = battlefieldBranchrider();
        assertThat(branchrider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void activatedAbilityGivesPlusTwoPowerUntilEndOfTurn() {
        Permanent branchrider = addReadyBranchrider();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, branchrider)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, branchrider)).isEqualTo(1);
    }

    @Test
    void activatedAbilityWearsOffAtEndOfTurn() {
        Permanent branchrider = addReadyBranchrider();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, branchrider)).isEqualTo(1);
    }

    private Permanent addReadyBranchrider() {
        Permanent branchrider = new Permanent(new ViashinoBranchrider());
        branchrider.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(branchrider);
        return branchrider;
    }

    private Permanent battlefieldBranchrider() {
        return gd.playerBattlefields.get(player1.getId()).getFirst();
    }
}
