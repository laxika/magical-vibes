package com.github.laxika.magicalvibes.cards.i;

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

@CardUsed(IgneousGolem.class)
class IgneousGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability grants trample")
    void abilityGrantsTrample() {
        Permanent golem = addCreatureReady(player1, new IgneousGolem());
        harness.addMana(player1, ManaColor.RED, 2);

        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Ability grants trample only to the source creature")
    void abilityGrantsTrampleOnlyToSource() {
        Permanent golem = addCreatureReady(player1, new IgneousGolem());
        Permanent otherGolem = addCreatureReady(player1, new IgneousGolem());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherGolem, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Trample wears off at end of turn")
    void trampleWearsOff() {
        Permanent golem = addCreatureReady(player1, new IgneousGolem());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Ability can be activated while the Golem is tapped")
    void canActivateWhileTapped() {
        Permanent golem = addCreatureReady(player1, new IgneousGolem());
        golem.tap();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(golem.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Cannot activate the ability without enough mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new IgneousGolem());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
