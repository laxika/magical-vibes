package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(LlanowarCavalry.class)
class LlanowarCavalryTest extends BaseCardTest {

    @Test
    void resolvingAbilityGrantsVigilanceUntilEndOfTurn() {
        Permanent cavalry = addCreatureReady(player1, new LlanowarCavalry());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void vigilanceWearsOffAtEndOfTurn() {
        Permanent cavalry = addCreatureReady(player1, new LlanowarCavalry());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, cavalry, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    void abilityRequiresOneWhiteMana() {
        addCreatureReady(player1, new LlanowarCavalry());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    void abilityDoesNotTapCavalry() {
        Permanent cavalry = addCreatureReady(player1, new LlanowarCavalry());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(cavalry.isTapped()).isFalse();
    }
}
