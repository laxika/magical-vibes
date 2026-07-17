package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AtogTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact gives Atog +2/+2 until end of turn")
    void sacrificeBoostsAtog() {
        harness.addToBattlefield(player1, new Atog());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent atog = findPermanent(player1, "Atog");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Artifact was sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spellbook"));
        assertThat(atog.getPowerModifier()).isEqualTo(2);
        assertThat(atog.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at cleanup step")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new Atog());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent atog = findPermanent(player1, "Atog");

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(atog.getPowerModifier()).isEqualTo(0);
        assertThat(atog.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate ability without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        harness.addToBattlefield(player1, new Atog());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No artifact to sacrifice");
    }
}
