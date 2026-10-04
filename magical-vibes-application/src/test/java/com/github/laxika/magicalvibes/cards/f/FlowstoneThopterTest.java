package com.github.laxika.magicalvibes.cards.f;

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

@CardUsed(FlowstoneThopter.class)
class FlowstoneThopterTest extends BaseCardTest {

    @Test
    @DisplayName("Activating gives +1/-1 and flying until end of turn")
    void boostsAndGrantsFlying() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new FlowstoneThopter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Each activation adds another +1/-1 boost")
    void repeatedActivationsStack() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new FlowstoneThopter());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The ability requires one generic mana")
    void abilityRequiresMana() {
        harness.addToBattlefieldAndReturn(player1, new FlowstoneThopter());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The boost and flying wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new FlowstoneThopter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The boost and flying apply only when the ability resolves and only to its source")
    void affectsOnlyItsSourceOnResolution() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new FlowstoneThopter());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new FlowstoneThopter());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new FlowstoneThopter());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        for (Permanent unaffected : new Permanent[]{other, opposing}) {
            assertThat(gqs.getEffectivePower(gd, unaffected)).isEqualTo(4);
            assertThat(gqs.getEffectiveToughness(gd, unaffected)).isEqualTo(4);
            assertThat(gqs.hasKeyword(gd, unaffected, Keyword.FLYING)).isFalse();
        }
    }

    @Test
    @DisplayName("A tapped, summoning-sick Thopter can activate using colored mana")
    void canActivateWhileTappedAndSummoningSickWithColoredMana() {
        Permanent thopter = harness.addToBattlefieldAndReturn(player1, new FlowstoneThopter());
        thopter.tap();
        thopter.setSummoningSick(true);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, thopter)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, thopter)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, thopter, Keyword.FLYING)).isTrue();
        assertThat(thopter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Four activations put the Thopter into its owner's graveyard at zero toughness")
    void diesWhenRepeatedActivationsReduceToughnessToZero() {
        harness.addToBattlefield(player1, new FlowstoneThopter());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        for (int activation = 0; activation < 4; activation++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        harness.assertNotOnBattlefield(player1, "Flowstone Thopter");
        harness.assertInGraveyard(player1, "Flowstone Thopter");
    }
}
