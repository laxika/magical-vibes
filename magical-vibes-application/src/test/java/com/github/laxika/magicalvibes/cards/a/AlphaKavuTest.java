package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlphaKavu.class, AmphibiousKavu.class, AuroraGriffin.class})
class AlphaKavuTest extends BaseCardTest {

    private void addReadyAlphaKavu() {
        addCreatureReady(player1, new AlphaKavu());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    @Test
    @DisplayName("Ability gives target Kavu -1/+1 until end of turn")
    void weakensAndToughensTargetKavu() {
        addReadyAlphaKavu();
        harness.addToBattlefield(player1, new AmphibiousKavu());

        UUID targetId = harness.getPermanentId(player1, "Amphibious Kavu");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent kavu = findPermanent(player1, "Amphibious Kavu");
        assertThat(kavu.getEffectivePower()).isEqualTo(1);
        assertThat(kavu.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addReadyAlphaKavu();
        harness.addToBattlefield(player1, new AmphibiousKavu());

        UUID targetId = harness.getPermanentId(player1, "Amphibious Kavu");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        Permanent kavu = findPermanent(player1, "Amphibious Kavu");
        assertThat(kavu.getEffectivePower()).isEqualTo(2);
        assertThat(kavu.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability cannot target a non-Kavu creature")
    void rejectsNonKavuTarget() {
        addReadyAlphaKavu();
        harness.addToBattlefield(player1, new AuroraGriffin());

        UUID targetId = harness.getPermanentId(player1, "Aurora Griffin");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, targetId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability can target an opponent's Kavu")
    void targetsOpponentControlledKavu() {
        addReadyAlphaKavu();
        harness.addToBattlefield(player2, new AmphibiousKavu());

        UUID targetId = harness.getPermanentId(player2, "Amphibious Kavu");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent kavu = findPermanent(player2, "Amphibious Kavu");
        assertThat(kavu.getEffectivePower()).isEqualTo(1);
        assertThat(kavu.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability does not tap Alpha Kavu")
    void abilityDoesNotTapSource() {
        addReadyAlphaKavu();
        UUID targetId = harness.getPermanentId(player1, "Alpha Kavu");

        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Alpha Kavu").isTapped()).isFalse();
    }
}
