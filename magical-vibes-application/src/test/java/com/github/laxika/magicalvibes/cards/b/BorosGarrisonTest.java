package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({BorosGarrison.class, Mountain.class})
class BorosGarrisonTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped and prompts to return a land")
    void entersTappedAndPromptsToReturnLand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new BorosGarrison()));
        harness.playLand(player1, 0);

        Permanent garrison = findPermanent(player1, "Boros Garrison");
        assertThat(garrison.isTapped()).isTrue();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(garrison.getId(), mountain.getId());
    }

    @Test
    @DisplayName("The ETB ability returns the chosen land to its owner's hand")
    void returnsChosenLandToHand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new BorosGarrison()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, mountain.getId());

        harness.assertOnBattlefield(player1, "Boros Garrison");
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInHand(player1, "Mountain");
    }

    @Test
    @DisplayName("Tapping Boros Garrison adds red and white mana")
    void tappingAddsRedAndWhiteMana() {
        Permanent garrison = harness.addToBattlefieldAndReturn(player1, new BorosGarrison());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(garrison.isTapped()).isTrue();
    }
}
