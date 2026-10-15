package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FiremindVessel.class})
class FiremindVesselTest extends BaseCardTest {

    @Test
    @DisplayName("Firemind Vessel enters the battlefield tapped")
    void entersBattlefieldTapped() {
        harness.setHand(player1, List.of(new FiremindVessel()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Firemind Vessel").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Firemind Vessel adds two mana of different colors")
    void addsManaOfDifferentColors() {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new FiremindVessel());
        GameData gameData = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ManaColor.RED.name());
        assertThatThrownBy(() -> harness.handleListChoice(player1, ManaColor.RED.name()))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(vessel.isTapped()).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "WHITE, BLUE", "WHITE, BLACK", "WHITE, RED", "WHITE, GREEN",
            "BLUE, BLACK", "BLUE, RED", "BLUE, GREEN",
            "BLACK, RED", "BLACK, GREEN", "RED, GREEN"
    })
    @DisplayName("Every pair of different colors is available without using the stack")
    void acceptsEveryDifferentColorPair(ManaColor first, ManaColor second) {
        Permanent vessel = harness.addToBattlefieldAndReturn(player1, new FiremindVessel());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).isEmpty();
        assertThatThrownBy(() -> harness.handleListChoice(player1, ManaColor.COLORLESS.name()))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, first.name());
        assertThatThrownBy(() -> harness.handleListChoice(player1, ManaColor.COLORLESS.name()))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleListChoice(player1, second.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(first)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(second)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotalAllMana()).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(vessel.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A Vessel entering tapped cannot activate until it untaps")
    void cannotActivateOnEnteringButCanAfterUntapping() {
        Permanent vessel = harness.enterBattlefieldAndReturn(player1, new FiremindVessel());

        assertThat(vessel.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();

        harness.performUntapStep(player1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(vessel.isTapped()).isTrue();
    }
}
