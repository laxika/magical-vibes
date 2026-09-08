package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SvyelunitePriest.class, GrizzlyBears.class, Forest.class})
class SvyelunitePriestTest extends BaseCardTest {

    @Test
    @DisplayName("{U}{U}, {T} during your upkeep grants shroud to target creature")
    void grantsShroudDuringUpkeep() {
        Permanent priest = addCreatureReady(player1, new SvyelunitePriest());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        prepareUpkeep();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.SHROUD)).isTrue();
        assertThat(priest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability can target an opponent's creature")
    void grantsShroudToOpponentsCreature() {
        addCreatureReady(player1, new SvyelunitePriest());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        prepareUpkeep();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.SHROUD)).isTrue();
    }

    @Test
    @DisplayName("Granted shroud wears off at end of turn")
    void shroudWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new SvyelunitePriest());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        prepareUpkeep();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, target, Keyword.SHROUD)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot be activated outside your upkeep")
    void cannotActivateOutsideUpkeep() {
        addCreatureReady(player1, new SvyelunitePriest());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("The ability cannot be activated during an opponent's upkeep")
    void cannotActivateDuringOpponentsUpkeep() {
        addCreatureReady(player1, new SvyelunitePriest());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }

    @Test
    @DisplayName("The ability requires two blue mana")
    void requiresTwoBlueMana() {
        addCreatureReady(player1, new SvyelunitePriest());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot be activated while the priest is tapped")
    void cannotActivateWhileTapped() {
        Permanent priest = addCreatureReady(player1, new SvyelunitePriest());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        priest.tap();
        prepareUpkeep();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability cannot target a non-creature")
    void cannotTargetNonCreature() {
        addCreatureReady(player1, new SvyelunitePriest());
        Permanent forest = addCreatureReady(player1, new Forest());
        prepareUpkeep();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        addAbilityMana();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
    }

}
