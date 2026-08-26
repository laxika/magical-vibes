package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({DimirAqueduct.class, Island.class})
class DimirAqueductTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped and prompts to return a land")
    void entersTappedAndPromptsToReturnLand() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new DimirAqueduct()));
        harness.playLand(player1, 0);

        Permanent aqueduct = findPermanent(player1, "Dimir Aqueduct");
        assertThat(aqueduct.isTapped()).isTrue();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(aqueduct.getId(), island.getId());
    }

    @Test
    @DisplayName("The ETB ability returns the chosen land to its owner's hand")
    void returnsChosenLandToHand() {
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new DimirAqueduct()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, island.getId());

        harness.assertOnBattlefield(player1, "Dimir Aqueduct");
        harness.assertNotOnBattlefield(player1, "Island");
        harness.assertInHand(player1, "Island");
    }

    @Test
    @DisplayName("Tapping Dimir Aqueduct adds blue and black mana")
    void tappingAddsBlueAndBlackMana() {
        Permanent aqueduct = harness.addToBattlefieldAndReturn(player1, new DimirAqueduct());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(aqueduct.isTapped()).isTrue();
    }
}
