package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LlanowarAugur.class, GrizzlyBears.class, Forest.class})
class LlanowarAugurTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself and gives a target creature +3/+3 and trample")
    void sacrificesItselfAndBoostsTarget() {
        addAugur();
        Permanent target = addCreature(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        beginUpkeep();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
        assertThat(target.getGrantedKeywords()).contains(Keyword.TRAMPLE);
        harness.assertNotOnBattlefield(player1, "Llanowar Augur");
        harness.assertInGraveyard(player1, "Llanowar Augur");
    }

    @Test
    @DisplayName("The boost and trample wear off at end of turn")
    void boostAndTrampleWearOffAtEndOfTurn() {
        addAugur();
        Permanent target = addCreature(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        beginUpkeep();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        addAugur();
        Permanent target = addCreature(player2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        beginUpkeep();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    @DisplayName("Cannot activate the ability outside your upkeep")
    void cannotActivateOutsideYourUpkeep() {
        addAugur();
        Permanent target = addCreature(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreaturePermanent() {
        addAugur();
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);
        beginUpkeep();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    private Permanent addAugur() {
        return harness.addToBattlefieldAndReturn(player1, new LlanowarAugur());
    }

    private Permanent addCreature(Player player) {
        return harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
    }

    private void beginUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
    }
}
