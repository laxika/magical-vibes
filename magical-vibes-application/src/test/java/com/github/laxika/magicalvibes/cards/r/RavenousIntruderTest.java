package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RavenousIntruder.class, Spellbook.class, Ornithopter.class})
class RavenousIntruderTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact gives Ravenous Intruder +2/+2 until end of turn")
    void sacrificeBoostsRavenousIntruder() {
        Permanent intruder = harness.addToBattlefieldAndReturn(player1, new RavenousIntruder());
        harness.addToBattlefield(player1, new Spellbook());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(intruder.getPowerModifier()).isEqualTo(2);
        assertThat(intruder.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        Permanent intruder = harness.addToBattlefieldAndReturn(player1, new RavenousIntruder());
        harness.addToBattlefield(player1, new Spellbook());

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

    @Test
    @DisplayName("An artifact creature is sacrificed as a cost before the boost resolves")
    void artifactCreatureIsPaidBeforeResolution() {
        Permanent intruder = harness.addToBattlefieldAndReturn(player1, new RavenousIntruder());
        harness.addToBattlefield(player1, new Ornithopter());

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Ornithopter");
        harness.assertInGraveyard(player1, "Ornithopter");
        assertThat(gd.stack).hasSize(1);
        assertThat(intruder.getPowerModifier()).isZero();
        assertThat(intruder.getToughnessModifier()).isZero();

        harness.passBothPriorities();

        assertThat(intruder.getPowerModifier()).isEqualTo(2);
        assertThat(intruder.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("A tapped newly entered Intruder can activate repeatedly and its boosts accumulate")
    void repeatedActivationsAccumulateWithoutTappingCost() {
        Permanent intruder = harness.addToBattlefieldAndReturn(player1, new RavenousIntruder());
        intruder.setTapped(true);
        intruder.setSummoningSick(true);
        harness.addToBattlefield(player1, new Ornithopter());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new Ornithopter());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(intruder.getPowerModifier()).isEqualTo(4);
        assertThat(intruder.getToughnessModifier()).isEqualTo(4);
        assertThat(intruder.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's artifact cannot pay the sacrifice cost")
    void cannotSacrificeOpponentsArtifact() {
        harness.addToBattlefield(player1, new RavenousIntruder());
        harness.addToBattlefield(player2, new Ornithopter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");

        harness.assertOnBattlefield(player2, "Ornithopter");
        assertThat(gd.stack).isEmpty();
    }
}
