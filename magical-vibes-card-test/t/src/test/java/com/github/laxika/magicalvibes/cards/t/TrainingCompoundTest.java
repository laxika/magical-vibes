package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrainingCompound.class, Forest.class})
class TrainingCompoundTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Training Compound produces colorless mana")
    void tappingProducesColorlessMana() {
        Permanent compound = addReadyCompound();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(compound.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Red or green mana requires a newly entered Training Compound or a basic land")
    void coloredManaRequiresCondition() {
        Permanent compound = addReadyCompound();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered this turn or if you control a basic land");
        assertThat(compound.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A Training Compound that entered this turn can produce red or green mana")
    void newlyEnteredCompoundProducesChosenMana() {
        Permanent compound = harness.enterBattlefieldAndReturn(player1, new TrainingCompound());

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(compound.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A basic land enables Training Compound to produce red or green mana")
    void basicLandEnablesChosenMana() {
        Permanent compound = addReadyCompound();
        harness.addToBattlefield(player1, new Forest());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(compound.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's basic land does not enable colored mana")
    void opponentsBasicLandDoesNotEnableColoredMana() {
        Permanent compound = addReadyCompound();
        harness.addToBattlefield(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered this turn or if you control a basic land");
        assertThat(compound.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Another Training Compound entering does not enable an older one")
    void anotherLandEnteringDoesNotEnableColoredMana() {
        Permanent compound = addReadyCompound();
        harness.enterBattlefieldAndReturn(player1, new TrainingCompound());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered this turn or if you control a basic land");
        assertThat(compound.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Colored mana stops being available when the basic land leaves")
    void losingBasicLandDisablesColoredMana() {
        Permanent compound = addReadyCompound();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, "RED");
        gd.playerBattlefields.get(player1.getId()).remove(forest);
        compound.untap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered this turn or if you control a basic land");
        assertThat(compound.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("The entered-this-turn permission expires on a later turn")
    void enteredThisTurnPermissionExpires() {
        Permanent compound = harness.enterBattlefieldAndReturn(player1, new TrainingCompound());
        gd.permanentsEnteredBattlefieldThisTurn.clear();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("entered this turn or if you control a basic land");
        assertThat(compound.isTapped()).isFalse();
    }

    private Permanent addReadyCompound() {
        return harness.addToBattlefieldAndReturn(player1, new TrainingCompound());
    }
}
