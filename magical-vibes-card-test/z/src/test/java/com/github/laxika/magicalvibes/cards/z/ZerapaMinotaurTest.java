package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(ZerapaMinotaur.class)
class ZerapaMinotaurTest extends BaseCardTest {

    @Test
    void anyPlayerMayPayToRemoveFirstStrike() {
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new ZerapaMinotaur());
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void lostFirstStrikeReturnsAtEndOfTurn() {
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new ZerapaMinotaur());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.FIRST_STRIKE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void firstStrikeIsRemovedOnlyWhenAbilityResolves() {
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new ZerapaMinotaur());
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.FIRST_STRIKE)).isTrue();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    void activatingPlayerMustPayBothManaEvenIfControllerHasMana() {
        Permanent minotaur = harness.addToBattlefieldAndReturn(player1, new ZerapaMinotaur());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, minotaur, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void abilityRemovesFirstStrikeOnlyFromItsSource() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new ZerapaMinotaur());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new ZerapaMinotaur());
        source.tap();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(source.isTapped()).isTrue();
    }
}
