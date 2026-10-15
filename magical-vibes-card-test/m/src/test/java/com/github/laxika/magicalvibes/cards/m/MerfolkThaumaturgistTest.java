package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CitanulWoodreaders;
import com.github.laxika.magicalvibes.cards.u.UrborgTombOfYawgmoth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MerfolkThaumaturgist.class, CitanulWoodreaders.class, UrborgTombOfYawgmoth.class})
class MerfolkThaumaturgistTest extends BaseCardTest {

    private Permanent addThaumaturgistReady() {
        return addCreatureReady(player1, new MerfolkThaumaturgist());
    }

    @Test
    @DisplayName("Switches target creature's power and toughness")
    void switchesTargetPowerAndToughness() {
        Permanent source = addThaumaturgistReady();
        Permanent woodreaders = harness.addToBattlefieldAndReturn(player2, new CitanulWoodreaders());

        harness.activateAbility(player1, 0, null, woodreaders.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, woodreaders)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, woodreaders)).isEqualTo(1);
    }

    @Test
    @DisplayName("Switch wears off at end of turn")
    void switchWearsOff() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addThaumaturgistReady();
        Permanent woodreaders = harness.addToBattlefieldAndReturn(player1, new CitanulWoodreaders());

        harness.activateAbility(player1, 0, null, woodreaders.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, woodreaders)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, woodreaders)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, woodreaders)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, woodreaders)).isEqualTo(4);
    }

    @Test
    @DisplayName("Can target itself")
    void canTargetItself() {
        Permanent source = addThaumaturgistReady();

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, source)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addThaumaturgistReady();
        Permanent land = harness.addToBattlefieldAndReturn(player2, new UrborgTombOfYawgmoth());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be a creature");
    }

    @Test
    @DisplayName("Two switches restore the original power and toughness")
    void twoSwitchesRestoreOriginalPowerAndToughness() {
        addThaumaturgistReady();
        addThaumaturgistReady();
        Permanent woodreaders = harness.addToBattlefieldAndReturn(player2, new CitanulWoodreaders());

        harness.activateAbility(player1, 0, null, woodreaders.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, woodreaders)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, woodreaders)).isEqualTo(1);

        harness.activateAbility(player1, 1, null, woodreaders.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, woodreaders)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, woodreaders)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new MerfolkThaumaturgist());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(source.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot activate again while tapped")
    void cannotActivateAgainWhileTapped() {
        Permanent source = addThaumaturgistReady();
        Permanent woodreaders = harness.addToBattlefieldAndReturn(player2, new CitanulWoodreaders());

        harness.activateAbility(player1, 0, null, woodreaders.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, woodreaders.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(source.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, woodreaders)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, woodreaders)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
