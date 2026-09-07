package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HotfootGnome.class, GrizzlyBears.class, Mountain.class})
class HotfootGnomeTest extends BaseCardTest {

    @Test
    @DisplayName("Another target creature gains haste")
    void grantsHasteToAnotherCreature() {
        Permanent gnome = addReadyGnome(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();
        assertThat(gnome.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Haste wears off at end of turn")
    void hasteWearsOffAtEndOfTurn() {
        addReadyGnome(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Cannot target the Gnome itself")
    void cannotTargetItself() {
        Permanent gnome = addReadyGnome(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, gnome.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature");
    }

    @Test
    @DisplayName("Cannot target a noncreature")
    void cannotTargetNonCreature() {
        addReadyGnome(player1);
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another creature");
    }

    private Permanent addReadyGnome(Player player) {
        Permanent gnome = harness.addToBattlefieldAndReturn(player, new HotfootGnome());
        gnome.setSummoningSick(false);
        return gnome;
    }
}
