package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CloudheathDrake.class})
class CloudheathDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("{1}{W} grants vigilance until end of turn")
    void grantsVigilance() {
        Permanent drake = addCreatureReady(player1, new CloudheathDrake());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, drake, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Vigilance wears off at end of turn cleanup")
    void vigilanceResetsAtEndOfTurn() {
        Permanent drake = addCreatureReady(player1, new CloudheathDrake());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, drake, Keyword.VIGILANCE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, drake, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Granted vigilance: does not tap when attacking")
    void grantedVigilanceDoesNotTapWhenAttacking() {
        Permanent drake = addCreatureReady(player1, new CloudheathDrake());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, drake, Keyword.VIGILANCE)).isTrue();

        declareAttackers(List.of(0));

        assertThat(drake.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Only the activating Drake gains vigilance, and only after resolution")
    void grantsVigilanceOnlyToSourceOnResolution() {
        Permanent source = addCreatureReady(player1, new CloudheathDrake());
        Permanent other = addCreatureReady(player1, new CloudheathDrake());
        Permanent opponent = addCreatureReady(player2, new CloudheathDrake());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gqs.hasKeyword(gd, source, Keyword.VIGILANCE)).isFalse();
        assertThat(source.isTapped()).isFalse();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, source, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, other, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("A tapped, summoning-sick Drake can activate without untapping")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new CloudheathDrake());
        drake.setSummoningSick(true);
        drake.setTapped(true);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, drake, Keyword.VIGILANCE)).isTrue();
        assertThat(drake.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Two colorless mana cannot pay the white activation cost")
    void activationRequiresWhiteMana() {
        Permanent drake = addCreatureReady(player1, new CloudheathDrake());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, drake, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("One white mana cannot pay the entire activation cost")
    void activationRequiresGenericManaToo() {
        Permanent drake = addCreatureReady(player1, new CloudheathDrake());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, drake, Keyword.VIGILANCE)).isFalse();
    }
}
