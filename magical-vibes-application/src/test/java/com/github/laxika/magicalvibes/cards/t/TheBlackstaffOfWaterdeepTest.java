package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheBlackstaffOfWaterdeep.class, Millstone.class})
class TheBlackstaffOfWaterdeepTest extends BaseCardTest {

    @Test
    @DisplayName("Animates another nontoken artifact into a 4/4 artifact creature")
    void animatesAnotherArtifact() {
        Permanent staff = addReadyStaff(player1);
        Permanent millstone = addReadyMillstone(player1);
        addMana();

        harness.activateAbility(player1, 0, null, millstone.getId());
        harness.passBothPriorities();

        assertThat(staff.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, millstone)).isTrue();
        assertThat(gqs.isArtifact(gd, millstone)).isTrue();
        assertThat(gqs.getEffectivePower(gd, millstone)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, millstone)).isEqualTo(4);
    }

    @Test
    @DisplayName("Animation lasts while The Blackstaff remains tapped")
    void animationLastsWhileTapped() {
        Permanent staff = addReadyStaff(player1);
        Permanent millstone = addReadyMillstone(player1);
        addMana();

        harness.activateAbility(player1, 0, null, millstone.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, millstone)).isTrue();
    }

    @Test
    @DisplayName("Animation ends when The Blackstaff becomes untapped")
    void animationEndsWhenUntapped() {
        Permanent staff = addReadyStaff(player1);
        Permanent millstone = addReadyMillstone(player1);
        addMana();

        harness.activateAbility(player1, 0, null, millstone.getId());
        harness.passBothPriorities();
        advanceToNextTurnWithMayChoice(player2, true);

        assertThat(staff.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, millstone)).isFalse();
    }

    @Test
    @DisplayName("Cannot target The Blackstaff itself")
    void cannotTargetItself() {
        addReadyStaff(player1);
        addMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null,
                findPermanent(player1, "The Blackstaff of Waterdeep").getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another nontoken artifact you control");
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void sorcerySpeedOnly() {
        addReadyStaff(player1);
        Permanent millstone = addReadyMillstone(player1);
        addMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, millstone.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReadyStaff(Player player) {
        Permanent staff = harness.addToBattlefieldAndReturn(player, new TheBlackstaffOfWaterdeep());
        staff.setSummoningSick(false);
        return staff;
    }

    private Permanent addReadyMillstone(Player player) {
        Permanent millstone = harness.addToBattlefieldAndReturn(player, new Millstone());
        millstone.setSummoningSick(false);
        return millstone;
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }

    private void advanceToNextTurnWithMayChoice(Player currentActivePlayer, boolean acceptUntap) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Player newActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.handleMayAbilityChosen(newActivePlayer, acceptUntap);
    }
}
