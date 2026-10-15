package com.github.laxika.magicalvibes.cards.s;

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

@CardUsed({SunBlessedPeak.class})
class SunBlessedPeakTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersBattlefieldTapped() {
        playPeak();

        Permanent peak = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(peak.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Adds red mana")
    void addsRedMana() {
        Permanent peak = addReadyPeak();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(peak.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Adds white mana")
    void addsWhiteMana() {
        Permanent peak = addReadyPeak();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(peak.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sacrificing it draws a card")
    void sacrificingDrawsCard() {
        addReadyPeak();
        harness.setLibrary(player1, List.of(new SunBlessedPeak()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof SunBlessedPeak);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Enters tapped when put directly onto the battlefield")
    void entersTappedWithoutBeingPlayed() {
        Permanent peak = harness.enterBattlefieldAndReturn(player1, new SunBlessedPeak());

        assertThat(peak.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A newly controlled noncreature land can produce mana")
    void newlyControlledLandCanProduceMana() {
        Permanent peak = harness.addToBattlefieldAndReturn(player1, new SunBlessedPeak());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(peak.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Neither ability can be activated while tapped")
    void tappedLandCannotActivateEitherAbility() {
        playPeak();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Insufficient mana does not sacrifice or tap the land")
    void insufficientManaDoesNotPayOtherCosts() {
        Permanent peak = addReadyPeak();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(peak.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(peak);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    private void playPeak() {
        harness.setHand(player1, List.of(new SunBlessedPeak()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player1, 0);
    }

    private Permanent addReadyPeak() {
        Permanent peak = harness.addToBattlefieldAndReturn(player1, new SunBlessedPeak());
        peak.setSummoningSick(false);
        return peak;
    }
}
