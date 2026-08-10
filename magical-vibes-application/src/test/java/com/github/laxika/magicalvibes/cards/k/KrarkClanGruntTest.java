package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KrarkClanGruntTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact gives Krark-Clan Grunt +1/+0 and first strike until end of turn")
    void sacrificeBoostsGruntAndGrantsFirstStrike() {
        harness.addToBattlefield(player1, new KrarkClanGrunt());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent grunt = findPermanent(player1, "Krark-Clan Grunt");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        assertThat(grunt.getPowerModifier()).isEqualTo(1);
        assertThat(grunt.getToughnessModifier()).isZero();
        assertThat(gqs.hasKeyword(gd, grunt, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The pump and first strike wear off at cleanup")
    void effectsWearOffAtCleanup() {
        harness.addToBattlefield(player1, new KrarkClanGrunt());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent grunt = findPermanent(player1, "Krark-Clan Grunt");

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
}
