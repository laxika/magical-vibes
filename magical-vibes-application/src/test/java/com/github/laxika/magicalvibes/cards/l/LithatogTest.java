package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LithatogTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing an artifact gives Lithatog +1/+1 until end of turn")
    void sacrificeArtifactBoostsLithatog() {
        harness.addToBattlefield(player1, new Lithatog());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Forest());

        Permanent lithatog = findPermanent(player1, "Lithatog");

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Spellbook");
        harness.assertOnBattlefield(player1, "Forest");
        assertThat(lithatog.getPowerModifier()).isEqualTo(1);
        assertThat(lithatog.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a land gives Lithatog +1/+1 until end of turn")
    void sacrificeLandBoostsLithatog() {
        harness.addToBattlefield(player1, new Lithatog());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new Forest());

        Permanent lithatog = findPermanent(player1, "Lithatog");

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spellbook");
        harness.assertInGraveyard(player1, "Forest");
        assertThat(lithatog.getPowerModifier()).isEqualTo(1);
        assertThat(lithatog.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new Lithatog());
        harness.addToBattlefield(player1, new Spellbook());

        Permanent lithatog = findPermanent(player1, "Lithatog");

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(lithatog.getPowerModifier()).isEqualTo(0);
        assertThat(lithatog.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("An ability cannot be activated without a matching permanent to sacrifice")
    void cannotActivateWithoutMatchingPermanent() {
        harness.addToBattlefield(player1, new Lithatog());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: an artifact");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No permanent to sacrifice matching: a land");
    }
}
