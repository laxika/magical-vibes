package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DevilthornFox;
import com.github.laxika.magicalvibes.cards.t.ThrabenInspector;
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

@CardUsed({VeteranCathar.class, ThrabenInspector.class, DevilthornFox.class})
class VeteranCatharTest extends BaseCardTest {

    @Test
    @DisplayName("The ability grants double strike to a target Human")
    void grantsDoubleStrikeToTargetHuman() {
        harness.addToBattlefield(player1, new VeteranCathar());
        Permanent human = harness.addToBattlefieldAndReturn(player2, new ThrabenInspector());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, human.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, human, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The granted double strike wears off at end of turn")
    void doubleStrikeWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new VeteranCathar());
        Permanent human = harness.addToBattlefieldAndReturn(player1, new ThrabenInspector());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, human.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, human, Keyword.DOUBLE_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, human, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot target a non-Human creature")
    void cannotTargetNonHumanCreature() {
        harness.addToBattlefield(player1, new VeteranCathar());
        Permanent fox = harness.addToBattlefieldAndReturn(player1, new DevilthornFox());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, fox.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a Human");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Veteran Cathar can target itself")
    void canTargetItselfWhileTappedAndSummoningSick() {
        Permanent cathar = harness.addToBattlefieldAndReturn(player1, new VeteranCathar());
        cathar.setSummoningSick(true);
        cathar.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, cathar.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cathar, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(cathar.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The activation requires white mana")
    void cannotActivateWithoutWhiteMana() {
        Permanent cathar = harness.addToBattlefieldAndReturn(player1, new VeteranCathar());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, cathar.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, cathar, Keyword.DOUBLE_STRIKE)).isFalse();
    }
}
