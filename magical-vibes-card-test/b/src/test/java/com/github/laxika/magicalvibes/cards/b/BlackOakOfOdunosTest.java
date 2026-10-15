package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlackOakOfOdunos.class})
class BlackOakOfOdunosTest extends BaseCardTest {

    @Test
    @DisplayName("Pays by tapping another creature and gets +1/+1 until end of turn")
    void tapsAnotherCreatureToBoostSelf() {
        Permanent oak = addCreatureReady(player1, new BlackOakOfOdunos());
        Permanent helper = addCreatureReady(player1, new BlackOakOfOdunos());
        harness.addMana(player1, ManaColor.BLACK, 1);

        int oakIndex = gd.playerBattlefields.get(player1.getId()).indexOf(oak);
        harness.activateAbility(player1, oakIndex, null, null);
        harness.passBothPriorities();

        assertThat(helper.isTapped()).isTrue();
        assertThat(oak.isTapped()).isFalse();
        assertThat(oak.getEffectivePower()).isEqualTo(1);
        assertThat(oak.getEffectiveToughness()).isEqualTo(6);
    }

    @Test
    @DisplayName("Cannot activate without another untapped creature")
    void cannotActivateWithoutAnotherUntappedCreature() {
        Permanent oak = addCreatureReady(player1, new BlackOakOfOdunos());
        harness.addMana(player1, ManaColor.BLACK, 1);

        int oakIndex = gd.playerBattlefields.get(player1.getId()).indexOf(oak);
        assertThatThrownBy(() -> harness.activateAbility(player1, oakIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent oak = addCreatureReady(player1, new BlackOakOfOdunos());
        addCreatureReady(player1, new BlackOakOfOdunos());
        harness.addMana(player1, ManaColor.BLACK, 1);

        int oakIndex = gd.playerBattlefields.get(player1.getId()).indexOf(oak);
        harness.activateAbility(player1, oakIndex, null, null);
        harness.passBothPriorities();
        assertThat(oak.getEffectivePower()).isEqualTo(1);
        assertThat(oak.getEffectiveToughness()).isEqualTo(6);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(oak.getEffectivePower()).isEqualTo(0);
        assertThat(oak.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    void canActivateWhileTappedAndSummoningSickUsingAnotherSummoningSickCreature() {
        Permanent oak = harness.addToBattlefieldAndReturn(player1, new BlackOakOfOdunos());
        oak.setSummoningSick(true);
        oak.tap();
        Permanent helper = harness.addToBattlefieldAndReturn(player1, new BlackOakOfOdunos());
        helper.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(helper.isTapped()).isTrue();
        assertThat(oak.getEffectivePower()).isZero();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(oak.isTapped()).isTrue();
        assertThat(oak.getEffectivePower()).isEqualTo(1);
        assertThat(oak.getEffectiveToughness()).isEqualTo(6);
        assertThat(helper.getEffectivePower()).isZero();
    }

    @Test
    void cannotPayWithTappedCreature() {
        addCreatureReady(player1, new BlackOakOfOdunos());
        Permanent helper = addCreatureReady(player1, new BlackOakOfOdunos());
        helper.tap();
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayWithOpponentsCreature() {
        addCreatureReady(player1, new BlackOakOfOdunos());
        Permanent opponent = addCreatureReady(player2, new BlackOakOfOdunos());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponent.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWithoutBlackMana() {
        addCreatureReady(player1, new BlackOakOfOdunos());
        Permanent helper = addCreatureReady(player1, new BlackOakOfOdunos());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(helper.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void repeatedActivationsStackAndCannotReuseTappedCreature() {
        Permanent oak = addCreatureReady(player1, new BlackOakOfOdunos());
        Permanent first = addCreatureReady(player1, new BlackOakOfOdunos());
        Permanent second = addCreatureReady(player1, new BlackOakOfOdunos());
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, first.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(oak.isTapped()).isFalse();
        assertThat(oak.getEffectivePower()).isEqualTo(2);
        assertThat(oak.getEffectiveToughness()).isEqualTo(7);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
