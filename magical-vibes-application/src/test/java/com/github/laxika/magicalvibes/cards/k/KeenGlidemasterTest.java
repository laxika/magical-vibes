package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AlpineWatchdog;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeenGlidemaster.class, AlpineWatchdog.class, Mountain.class})
class KeenGlidemasterTest extends BaseCardTest {

    private void addKeenGlidemasterReady() {
        addCreatureReady(player1, new KeenGlidemaster());
    }

    @Test
    @DisplayName("Ability grants flying to target creature")
    void grantsFlying() {
        addKeenGlidemasterReady();
        harness.addMana(player1, ManaColor.BLUE, 3);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying wears off at end of turn")
    void flyingWearsOff() {
        addKeenGlidemasterReady();
        harness.addMana(player1, ManaColor.BLUE, 3);
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new AlpineWatchdog());

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Ability can only target creatures")
    void cannotTargetNonCreature() {
        addKeenGlidemasterReady();
        harness.addMana(player1, ManaColor.BLUE, 3);
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped summoning-sick Glidemaster can grant itself flying")
    void canTargetItselfWhileTappedAndSummoningSick() {
        Permanent glidemaster = harness.addToBattlefieldAndReturn(player1, new KeenGlidemaster());
        glidemaster.setSummoningSick(true);
        glidemaster.tap();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, 0, null, glidemaster.getId());
        assertThat(gqs.hasKeyword(gd, glidemaster, Keyword.FLYING)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, glidemaster, Keyword.FLYING)).isTrue();
        assertThat(glidemaster.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Ability can grant flying to an opponent's creature repeatedly")
    void canTargetOpponentAndActivateRepeatedly() {
        Permanent glidemaster = harness.addToBattlefieldAndReturn(player1, new KeenGlidemaster());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AlpineWatchdog());
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, glidemaster, Keyword.FLYING)).isFalse();
        assertThat(glidemaster.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Ability requires three mana including blue")
    void cannotActivateWithoutBlueMana() {
        Permanent glidemaster = harness.addToBattlefieldAndReturn(player1, new KeenGlidemaster());
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, glidemaster.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.hasKeyword(gd, glidemaster, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Two mana cannot pay the activation cost")
    void cannotActivateWithOnlyTwoMana() {
        Permanent glidemaster = harness.addToBattlefieldAndReturn(player1, new KeenGlidemaster());
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, glidemaster.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gqs.hasKeyword(gd, glidemaster, Keyword.FLYING)).isFalse();
    }
}
