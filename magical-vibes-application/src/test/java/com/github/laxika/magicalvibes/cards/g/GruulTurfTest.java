package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GruulTurf.class, GruulNodorog.class})
class GruulTurfTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and returns a chosen land to its owner's hand")
    void entersTappedAndReturnsChosenLand() {
        Permanent otherLand = harness.addToBattlefieldAndReturn(player1, new GruulTurf());
        harness.setHand(player1, List.of(new GruulTurf()));

        harness.playLand(player1, 0);

        Permanent turf = findPermanents(player1, "Gruul Turf").getLast();
        assertThat(turf.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, otherLand.getId());

        harness.assertOnBattlefield(player1, "Gruul Turf");
        harness.assertInHand(player1, "Gruul Turf");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(otherLand);
    }

    @Test
    @DisplayName("Can return itself when it is the only land")
    void canReturnItself() {
        harness.setHand(player1, List.of(new GruulTurf()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent turf = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(turf.getId());
        harness.handlePermanentChosen(player1, turf.getId());

        harness.assertNotOnBattlefield(player1, "Gruul Turf");
        harness.assertInHand(player1, "Gruul Turf");
    }

    @Test
    @DisplayName("Returns a controlled land to its owner's hand and excludes nonlands")
    void returnsControlledLandToItsOwnersHand() {
        GruulTurf ownedLandCard = new GruulTurf();
        ownedLandCard.setOwnerId(player2.getId());
        Permanent ownedLand = harness.addToBattlefieldAndReturn(player1, ownedLandCard);
        Permanent nonland = harness.addToBattlefieldAndReturn(player1, new GruulNodorog());
        harness.setHand(player1, List.of(new GruulTurf()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent enteringTurf = findPermanents(player1, "Gruul Turf").getLast();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactlyInAnyOrder(ownedLand.getId(), enteringTurf.getId())
                .doesNotContain(nonland.getId());
        harness.handlePermanentChosen(player1, ownedLand.getId());

        harness.assertInHand(player2, "Gruul Turf");
        harness.assertNotInHand(player1, "Gruul Turf");
        harness.assertOnBattlefield(player1, "Gruul Nodorog");
    }

    @Test
    @DisplayName("An opponent's land cannot be chosen for the return trigger")
    void cannotReturnOpponentsLand() {
        Permanent opponentsLand = harness.addToBattlefieldAndReturn(player2, new GruulTurf());
        harness.setHand(player1, List.of(new GruulTurf()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent turf = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .containsExactly(turf.getId())
                .doesNotContain(opponentsLand.getId());
        harness.handlePermanentChosen(player1, turf.getId());

        harness.assertNotOnBattlefield(player1, "Gruul Turf");
        harness.assertInHand(player1, "Gruul Turf");
        harness.assertOnBattlefield(player2, "Gruul Turf");
        harness.assertNotInHand(player2, "Gruul Turf");
    }

    @Test
    @DisplayName("Tapping adds one red and one green mana")
    void manaAbilityAddsRedAndGreen() {
        Permanent turf = harness.addToBattlefieldAndReturn(player1, new GruulTurf());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(turf.isTapped()).isTrue();
    }
}
