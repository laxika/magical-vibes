package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RavenousIntruderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact gives Ravenous Intruder +2/+2 until end of turn")
    void sacrificeBoostsRavenousIntruder() {
        harness.addToBattlefield(player1, new RavenousIntruder());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent intruder = findPermanent(player1, "Ravenous Intruder");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(intruder.getPowerModifier()).isEqualTo(2);
        assertThat(intruder.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new RavenousIntruder());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent intruder = findPermanent(player1, "Ravenous Intruder");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(intruder.getPowerModifier()).isEqualTo(0);
        assertThat(intruder.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate ability without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        harness.addToBattlefield(player1, new RavenousIntruder());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }
}
