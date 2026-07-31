package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VeteransVoiceTest extends BaseCardTest {

    private Permanent host;
    private Permanent other;

    private void setupAura() {
        host = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new VeteransVoice());
        aura.setAttachedTo(host.getId());

        other = addCreatureReady(player1, new GrizzlyBears());
    }

    @Test
    @DisplayName("Tapping the enchanted creature gives another target creature +2/+1")
    void boostsOtherCreature() {
        setupAura();

        harness.activateAbility(player1, 1, null, other.getId());
        harness.passBothPriorities();

        assertThat(host.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(3);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        setupAura();

        harness.activateAbility(player1, 1, null, other.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate while the enchanted creature is tapped")
    void cannotActivateWhileHostTapped() {
        setupAura();
        host.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, other.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The enchanted creature itself is an illegal target")
    void rejectsEnchantedCreatureAsTarget() {
        setupAura();

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, host.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(host.isTapped()).isFalse();
    }
}
