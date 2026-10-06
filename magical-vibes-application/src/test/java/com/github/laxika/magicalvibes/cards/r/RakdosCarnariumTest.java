package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AzoriusGuildmage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakdosCarnarium.class, RixMaadiDungeonPalace.class, AzoriusGuildmage.class})
class RakdosCarnariumTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and returns a chosen land to its owner's hand")
    void entersTappedAndReturnsChosenLand() {
        Permanent rixMaadi = harness.addToBattlefieldAndReturn(player1, new RixMaadiDungeonPalace());
        harness.setHand(player1, List.of(new RakdosCarnarium()));

        harness.playLand(player1, 0);

        Permanent carnarium = findPermanent(player1, "Rakdos Carnarium");
        assertThat(carnarium.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, rixMaadi.getId());

        harness.assertOnBattlefield(player1, "Rakdos Carnarium");
        harness.assertInHand(player1, "Rix Maadi, Dungeon Palace");
        harness.assertNotOnBattlefield(player1, "Rix Maadi, Dungeon Palace");
    }

    @Test
    @DisplayName("Can return itself when it is the only land")
    void canReturnItself() {
        harness.setHand(player1, List.of(new RakdosCarnarium()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent carnarium = findPermanent(player1, "Rakdos Carnarium");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(carnarium.getId());
        harness.handlePermanentChosen(player1, carnarium.getId());

        harness.assertNotOnBattlefield(player1, "Rakdos Carnarium");
        harness.assertInHand(player1, "Rakdos Carnarium");
    }

    @Test
    @DisplayName("Only offers lands controlled by its controller")
    void onlyOffersControlledLands() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new RixMaadiDungeonPalace());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new RixMaadiDungeonPalace());
        Permanent ownNonland = harness.addToBattlefieldAndReturn(player1, new AzoriusGuildmage());
        harness.setHand(player1, List.of(new RakdosCarnarium()));

        harness.playLand(player1, 0);
        Permanent carnarium = findPermanent(player1, "Rakdos Carnarium");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(ownLand.getId(), carnarium.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(opponentLand.getId(), ownNonland.getId());

        harness.handlePermanentChosen(player1, ownLand.getId());
    }

    @Test
    @DisplayName("Returns a controlled land to its owner rather than its controller")
    void returnsLandToItsOwner() {
        RixMaadiDungeonPalace borrowedLand = new RixMaadiDungeonPalace();
        borrowedLand.setOwnerId(player2.getId());
        Permanent land = harness.addToBattlefieldAndReturn(player1, borrowedLand);
        harness.setHand(player1, List.of(new RakdosCarnarium()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());

        harness.assertInHand(player2, "Rix Maadi, Dungeon Palace");
        harness.assertNotInHand(player1, "Rix Maadi, Dungeon Palace");
        harness.assertNotOnBattlefield(player1, "Rix Maadi, Dungeon Palace");
        harness.assertOnBattlefield(player1, "Rakdos Carnarium");
    }

    @Test
    @DisplayName("Mana resolves immediately while the return-land trigger is on the stack")
    void manaAbilityResolvesWithReturnTriggerPending() {
        Permanent carnarium = harness.enterBattlefieldAndReturn(player1, new RakdosCarnarium());
        carnarium.untap();
        assertThat(gd.stack).hasSize(1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(carnarium.isTapped()).isTrue();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, carnarium.getId());
        harness.assertInHand(player1, "Rakdos Carnarium");
        harness.assertNotOnBattlefield(player1, "Rakdos Carnarium");
    }

    @Test
    @DisplayName("Tapping adds one black and one red mana")
    void manaAbilityAddsBlackAndRed() {
        Permanent carnarium = harness.addToBattlefieldAndReturn(player1, new RakdosCarnarium());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(carnarium.isTapped()).isTrue();
    }
}
