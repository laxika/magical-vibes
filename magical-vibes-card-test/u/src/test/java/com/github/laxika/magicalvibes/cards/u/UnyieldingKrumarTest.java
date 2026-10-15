package com.github.laxika.magicalvibes.cards.u;

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

@CardUsed({UnyieldingKrumar.class})
class UnyieldingKrumarTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants first strike until end of turn")
    void resolvingAbilityGrantsFirstStrike() {
        Permanent krumar = addCreatureReady(player1, new UnyieldingKrumar());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, krumar, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Granted first strike wears off at end of turn")
    void firstStrikeWearsOff() {
        Permanent krumar = addCreatureReady(player1, new UnyieldingKrumar());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, krumar, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Ability requires one generic and one white mana")
    void requiresOneGenericAndOneWhiteMana() {
        addCreatureReady(player1, new UnyieldingKrumar());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability uses the stack and grants first strike only to its source")
    void grantsFirstStrikeOnlyAfterResolutionAndOnlyToSource() {
        Permanent krumar = addCreatureReady(player1, new UnyieldingKrumar());
        Permanent other = addCreatureReady(player1, new UnyieldingKrumar());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.hasKeyword(gd, krumar, Keyword.FIRST_STRIKE)).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, krumar, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent krumar = harness.addToBattlefieldAndReturn(player1, new UnyieldingKrumar());
        krumar.setSummoningSick(true);
        krumar.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, krumar, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(krumar.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Generic mana cannot replace the white mana requirement")
    void requiresWhiteMana() {
        addCreatureReady(player1, new UnyieldingKrumar());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }
}
