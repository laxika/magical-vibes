package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.w.WeldingJar;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrarkClanGrunt.class, WeldingJar.class})
class KrarkClanGruntTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact gives Krark-Clan Grunt +1/+0 and first strike until end of turn")
    void sacrificeBoostsGruntAndGrantsFirstStrike() {
        Permanent grunt = harness.addToBattlefieldAndReturn(player1, new KrarkClanGrunt());
        harness.addToBattlefield(player1, new WeldingJar());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Welding Jar");
        assertThat(grunt.getPowerModifier()).isEqualTo(1);
        assertThat(grunt.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, grunt, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The pump and first strike wear off at cleanup")
    void effectsWearOffAtCleanup() {
        Permanent grunt = harness.addToBattlefieldAndReturn(player1, new KrarkClanGrunt());
        harness.addToBattlefield(player1, new WeldingJar());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(grunt.getPowerModifier()).isZero();
        assertThat(grunt.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, grunt, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The ability requires an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        harness.addToBattlefield(player1, new KrarkClanGrunt());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");
    }

    @Test
    @DisplayName("The ability can be activated repeatedly by sacrificing multiple artifacts")
    void repeatedActivationsAccumulateTheBoost() {
        Permanent grunt = harness.addToBattlefieldAndReturn(player1, new KrarkClanGrunt());
        Permanent firstJar = harness.addToBattlefieldAndReturn(player1, new WeldingJar());
        harness.addToBattlefield(player1, new WeldingJar());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, firstJar.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Welding Jar")).isZero();
        assertThat(grunt.getPowerModifier()).isEqualTo(2);
        assertThat(grunt.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, grunt, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The ability cannot sacrifice an artifact controlled by an opponent")
    void cannotSacrificeOpponentsArtifact() {
        Permanent grunt = harness.addToBattlefieldAndReturn(player1, new KrarkClanGrunt());
        harness.addToBattlefield(player2, new WeldingJar());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");

        harness.assertOnBattlefield(player2, "Welding Jar");
        assertThat(grunt.getPowerModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, grunt, Keyword.FIRST_STRIKE)).isFalse();
    }
}
