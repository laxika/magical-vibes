package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.p.Plains;
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

@CardUsed({OrzhovBasilica.class, Plains.class})
class OrzhovBasilicaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped and prompts to return a land")
    void entersTappedAndPromptsToReturnLand() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new OrzhovBasilica()));
        harness.playLand(player1, 0);

        Permanent basilica = findPermanent(player1, "Orzhov Basilica");
        assertThat(basilica.isTapped()).isTrue();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(basilica.getId(), plains.getId());
    }

    @Test
    @DisplayName("The ETB ability returns the chosen land to its owner's hand")
    void returnsChosenLandToHand() {
        Permanent plains = harness.addToBattlefieldAndReturn(player1, new Plains());
        harness.setHand(player1, List.of(new OrzhovBasilica()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, plains.getId());

        harness.assertOnBattlefield(player1, "Orzhov Basilica");
        harness.assertNotOnBattlefield(player1, "Plains");
        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Tapping Orzhov Basilica adds white and black mana")
    void tappingAddsWhiteAndBlackMana() {
        Permanent basilica = harness.addToBattlefieldAndReturn(player1, new OrzhovBasilica());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(basilica.isTapped()).isTrue();
    }
}
