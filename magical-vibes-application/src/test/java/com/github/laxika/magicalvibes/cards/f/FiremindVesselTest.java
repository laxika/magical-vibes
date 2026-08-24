package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
}
