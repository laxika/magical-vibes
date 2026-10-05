package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GodlessShrine;
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

@CardUsed({OrzhovBasilica.class, GodlessShrine.class, OrzhovSignet.class})
class OrzhovBasilicaTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped and prompts to return a land")
    void entersTappedAndPromptsToReturnLand() {
        Permanent godlessShrine = harness.addToBattlefieldAndReturn(player1, new GodlessShrine());
        harness.setHand(player1, List.of(new OrzhovBasilica()));
        harness.playLand(player1, 0);

        Permanent basilica = findPermanent(player1, "Orzhov Basilica");
        assertThat(basilica.isTapped()).isTrue();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(basilica.getId(), godlessShrine.getId());
    }

    @Test
    @DisplayName("The ETB ability returns the chosen land to its owner's hand")
    void returnsChosenLandToHand() {
        Permanent godlessShrine = harness.addToBattlefieldAndReturn(player1, new GodlessShrine());
        harness.setHand(player1, List.of(new OrzhovBasilica()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, godlessShrine.getId());

        harness.assertOnBattlefield(player1, "Orzhov Basilica");
        harness.assertNotOnBattlefield(player1, "Godless Shrine");
        harness.assertInHand(player1, "Godless Shrine");
    }

    @Test
    @DisplayName("The ETB ability only offers lands controlled by its controller")
    void onlyOffersControlledLands() {
        harness.addToBattlefield(player1, new OrzhovSignet());
        harness.setHand(player1, List.of(new OrzhovBasilica()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent basilica = findPermanent(player1, "Orzhov Basilica");
        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(basilica.getId());

        harness.handlePermanentChosen(player1, basilica.getId());

        harness.assertOnBattlefield(player1, "Orzhov Signet");
        harness.assertNotOnBattlefield(player1, "Orzhov Basilica");
        harness.assertInHand(player1, "Orzhov Basilica");
    }

    @Test
    @DisplayName("The ETB ability does not offer an opponent's land")
    void onlyOffersLandsControlledByItsController() {
        Permanent opponentShrine = harness.addToBattlefieldAndReturn(player2, new GodlessShrine());
        harness.setHand(player1, List.of(new OrzhovBasilica()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent basilica = findPermanent(player1, "Orzhov Basilica");
        GameData gd = harness.getGameData();
        PendingInteraction.PermanentChoice choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(basilica.getId());

        harness.handlePermanentChosen(player1, basilica.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(opponentShrine);
        harness.assertInHand(player1, "Orzhov Basilica");
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

    @Test
    @DisplayName("A controlled land owned by the opponent returns to the opponent's hand")
    void returnsBorrowedLandToItsOwner() {
        GodlessShrine shrine = new GodlessShrine();
        shrine.setOwnerId(player2.getId());
        Permanent borrowedLand = harness.addToBattlefieldAndReturn(player1, shrine);
        harness.setHand(player1, List.of(new OrzhovBasilica()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, borrowedLand.getId());

        harness.assertNotOnBattlefield(player1, "Godless Shrine");
        harness.assertInHand(player2, "Godless Shrine");
        harness.assertNotInHand(player1, "Godless Shrine");
        harness.assertOnBattlefield(player1, "Orzhov Basilica");
    }

    @Test
    @DisplayName("The return trigger still resolves after Basilica leaves the battlefield")
    void returnsLandAfterSourceLeavesBattlefield() {
        Permanent shrine = harness.addToBattlefieldAndReturn(player1, new GodlessShrine());
        harness.setHand(player1, List.of(new OrzhovBasilica()));
        harness.playLand(player1, 0);
        Permanent basilica = findPermanent(player1, "Orzhov Basilica");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, basilica));

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, shrine.getId());

        harness.assertNotOnBattlefield(player1, "Godless Shrine");
        harness.assertInHand(player1, "Godless Shrine");
        harness.assertInHand(player1, "Orzhov Basilica");
    }

    @Test
    @DisplayName("The return trigger resolves without a choice if no lands remain")
    void resolvesWhenNoControlledLandsRemain() {
        harness.setHand(player1, List.of(new OrzhovBasilica()));
        harness.playLand(player1, 0);
        Permanent basilica = findPermanent(player1, "Orzhov Basilica");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, basilica));

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertInHand(player1, "Orzhov Basilica");
    }
}
