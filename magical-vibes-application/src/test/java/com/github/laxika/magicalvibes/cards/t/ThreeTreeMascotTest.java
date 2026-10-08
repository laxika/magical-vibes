package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.p.PatchworkBanner;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThreeTreeMascot.class, PatchworkBanner.class})
class ThreeTreeMascotTest extends BaseCardTest {

    @Test
    @DisplayName("Pays one generic mana and adds one mana of the chosen color")
    void addsManaOfChosenColor() {
        Permanent mascot = addReadyMascot();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(mascot.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can only be activated once each turn")
    void canOnlyBeActivatedOnceEachTurn() {
        addReadyMascot();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void tappedSummoningSickMascotProducesAnyColorWithoutUsingStack(String color) {
        Permanent mascot = harness.addToBattlefieldAndReturn(player1, new ThreeTreeMascot());
        mascot.setSummoningSick(true);
        mascot.tap();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, color);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.valueOf(color))).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(mascot.isTapped()).isTrue();
    }

    @Test
    void failedPaymentDoesNotConsumeActivation() {
        addReadyMascot();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void separateMascotsHaveIndependentActivationLimits() {
        addReadyMascot();
        addReadyMascot();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");
        harness.activateAbility(player1, 1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void canActivateAgainOnOpponentsTurn() {
        addReadyMascot();
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"FROG", "RABBIT", "SQUIRREL"})
    void changelingReceivesChosenCreatureTypeBonus(String creatureType) {
        Permanent mascot = addReadyMascot();
        Permanent opposingMascot = addCreatureReady(player2, new ThreeTreeMascot());
        int ownPower = gqs.getEffectivePower(gd, mascot);
        int ownToughness = gqs.getEffectiveToughness(gd, mascot);
        int opposingPower = gqs.getEffectivePower(gd, opposingMascot);
        harness.setHand(player1, List.of(new PatchworkBanner()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, creatureType);

        assertThat(gqs.getEffectivePower(gd, mascot)).isEqualTo(ownPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, mascot)).isEqualTo(ownToughness + 1);
        assertThat(gqs.getEffectivePower(gd, opposingMascot)).isEqualTo(opposingPower);
    }

    private Permanent addReadyMascot() {
        return addCreatureReady(player1, new ThreeTreeMascot());
    }
}
