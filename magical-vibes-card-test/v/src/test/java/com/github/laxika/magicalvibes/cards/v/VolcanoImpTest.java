package com.github.laxika.magicalvibes.cards.v;

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

@CardUsed({VolcanoImp.class})
class VolcanoImpTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{R} ability grants first strike until end of turn")
    void grantsFirstStrikeUntilEndOfTurn() {
        Permanent imp = addCreatureReady(player1, new VolcanoImp());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, imp, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, imp, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Non-tap ability can be activated while summoning sick")
    void canActivateWhileSummoningSick() {
        Permanent imp = harness.addToBattlefieldAndReturn(player1, new VolcanoImp());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(imp.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, imp, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Ability cannot be activated without red mana")
    void cannotActivateWithoutRedMana() {
        addCreatureReady(player1, new VolcanoImp());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("First strike is granted only to the source and only when the ability resolves")
    void grantsFirstStrikeOnlyToSourceOnResolution() {
        Permanent imp = addCreatureReady(player1, new VolcanoImp());
        Permanent otherImp = addCreatureReady(player1, new VolcanoImp());
        Permanent opposingImp = addCreatureReady(player2, new VolcanoImp());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.hasKeyword(gd, imp, Keyword.FIRST_STRIKE)).isFalse();

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, imp, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, otherImp, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opposingImp, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("A tapped Volcano Imp can activate its ability")
    void canActivateWhileTapped() {
        Permanent imp = addCreatureReady(player1, new VolcanoImp());
        imp.tap();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(imp.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, imp, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("One red mana alone cannot pay the generic part of the activation cost")
    void cannotActivateWithoutGenericMana() {
        Permanent imp = addCreatureReady(player1, new VolcanoImp());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        assertThat(gqs.hasKeyword(gd, imp, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
