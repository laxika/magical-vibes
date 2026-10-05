package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GruulTurf;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IzzetBoilerworks.class, GruulTurf.class, IzzetGuildmage.class})
class IzzetBoilerworksTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and returns a chosen land to its owner's hand")
    void entersTappedAndReturnsChosenLand() {
        Permanent otherLand = harness.addToBattlefieldAndReturn(player1, new GruulTurf());
        harness.setHand(player1, List.of(new IzzetBoilerworks()));

        harness.playLand(player1, 0);

        Permanent boilerworks = findPermanent(player1, "Izzet Boilerworks");
        assertThat(boilerworks.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, otherLand.getId());

        harness.assertOnBattlefield(player1, "Izzet Boilerworks");
        harness.assertInHand(player1, "Gruul Turf");
        harness.assertNotOnBattlefield(player1, "Gruul Turf");
    }

    @Test
    @DisplayName("Can return itself when it is the only land")
    void canReturnItself() {
        harness.setHand(player1, List.of(new IzzetBoilerworks()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent boilerworks = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(boilerworks.getId());
        harness.handlePermanentChosen(player1, boilerworks.getId());

        harness.assertNotOnBattlefield(player1, "Izzet Boilerworks");
        harness.assertInHand(player1, "Izzet Boilerworks");
    }

    @Test
    @DisplayName("Tapping adds one blue and one red mana")
    void manaAbilityAddsBlueAndRed() {
        Permanent boilerworks = harness.addToBattlefieldAndReturn(player1, new IzzetBoilerworks());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(boilerworks.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An opponent's land cannot satisfy the return trigger")
    void cannotReturnOpponentsLand() {
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new GruulTurf());
        harness.setHand(player1, List.of(new IzzetBoilerworks()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent boilerworks = findPermanent(player1, "Izzet Boilerworks");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(boilerworks.getId())
                .doesNotContain(opposingLand.getId());
        harness.handlePermanentChosen(player1, boilerworks.getId());

        harness.assertInHand(player1, "Izzet Boilerworks");
        harness.assertNotOnBattlefield(player1, "Izzet Boilerworks");
        harness.assertOnBattlefield(player2, "Gruul Turf");
        harness.assertNotInHand(player2, "Gruul Turf");
    }

    @Test
    @DisplayName("Only offers controlled lands and returns the chosen land to its owner's hand")
    void onlyOffersControlledLandsAndReturnsChosenLandToItsOwner() {
        IzzetBoilerworks borrowedLandCard = new IzzetBoilerworks();
        borrowedLandCard.setOwnerId(player2.getId());
        Permanent borrowedLand = harness.addToBattlefieldAndReturn(player1, borrowedLandCard);
        Permanent nonland = harness.addToBattlefieldAndReturn(player1, new IzzetGuildmage());
        harness.setHand(player1, List.of(new IzzetBoilerworks()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent enteringBoilerworks = findPermanents(player1, "Izzet Boilerworks").getLast();
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .containsExactlyInAnyOrder(borrowedLand.getId(), enteringBoilerworks.getId())
                .doesNotContain(nonland.getId());

        harness.handlePermanentChosen(player1, borrowedLand.getId());

        harness.assertInHand(player2, "Izzet Boilerworks");
        harness.assertNotInHand(player1, "Izzet Boilerworks");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .contains(enteringBoilerworks, nonland)
                .doesNotContain(borrowedLand);
    }
}
