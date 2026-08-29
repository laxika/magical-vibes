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

@CardUsed(IcatianInfantry.class)
class IcatianInfantryTest extends BaseCardTest {

    @Test
    @DisplayName("The first ability grants first strike until end of turn")
    void grantsFirstStrikeUntilEndOfTurn() {
        Permanent infantry = addCreatureReady(player1, new IcatianInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, infantry, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, infantry, Keyword.BANDING)).isFalse();
        assertThat(infantry.isTapped()).isFalse();

        expireTemporaryAbilities();

        assertThat(gqs.hasKeyword(gd, infantry, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The second ability grants banding until end of turn")
    void grantsBandingUntilEndOfTurn() {
        Permanent infantry = addCreatureReady(player1, new IcatianInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, infantry, Keyword.BANDING)).isTrue();
        assertThat(gqs.hasKeyword(gd, infantry, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(infantry.isTapped()).isFalse();

        expireTemporaryAbilities();

        assertThat(gqs.hasKeyword(gd, infantry, Keyword.BANDING)).isFalse();
    }

    @Test
    @DisplayName("Both abilities can be active on the same creature")
    void canGainBothKeywordsAtOnce() {
        Permanent infantry = addCreatureReady(player1, new IcatianInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, infantry, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, infantry, Keyword.BANDING)).isTrue();
        assertThat(infantry.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each ability affects only the Icatian Infantry that activated it")
    void abilitiesAffectOnlyTheirSource() {
        Permanent firstInfantry = addCreatureReady(player1, new IcatianInfantry());
        Permanent secondInfantry = addCreatureReady(player1, new IcatianInfantry());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, firstInfantry, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, firstInfantry, Keyword.BANDING)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondInfantry, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, secondInfantry, Keyword.BANDING)).isTrue();
    }

    @Test
    @DisplayName("Both abilities require one generic mana")
    void abilitiesRequireGenericMana() {
        addCreatureReady(player1, new IcatianInfantry());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void expireTemporaryAbilities() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
