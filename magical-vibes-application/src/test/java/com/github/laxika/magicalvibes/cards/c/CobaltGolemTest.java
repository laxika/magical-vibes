package com.github.laxika.magicalvibes.cards.c;

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

@CardUsed(CobaltGolem.class)
class CobaltGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {1}{U} grants flying until end of turn")
    void payingManaGrantsFlying() {
        Permanent golem = addCreatureReady(player1, new CobaltGolem());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying wears off at end of turn")
    void flyingWearsOffAtEndOfTurn() {
        Permanent golem = addCreatureReady(player1, new CobaltGolem());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, golem, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("The ability requires one blue and one generic mana")
    void requiresBlueAndGenericMana() {
        addCreatureReady(player1, new CobaltGolem());
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The ability cannot be paid with only generic mana")
    void requiresBlueMana() {
        addCreatureReady(player1, new CobaltGolem());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Only the activated Cobalt Golem gains flying")
    void grantsFlyingOnlyToSource() {
        Permanent source = addCreatureReady(player1, new CobaltGolem());
        Permanent otherGolem = addCreatureReady(player1, new CobaltGolem());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherGolem, Keyword.FLYING)).isFalse();
    }
}
