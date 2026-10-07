package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.d.DarksteelCitadel;
import com.github.laxika.magicalvibes.cards.e.EchoingTruth;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.v.VolcanicIsland;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunderingTitan.class, Island.class, Mountain.class, Shatter.class,
        DarksteelCitadel.class, EchoingTruth.class, VolcanicIsland.class})
class SunderingTitanTest extends BaseCardTest {

    @Test
    @DisplayName("ETB lets its controller choose a land from any battlefield and destroys it")
    void etbControllerChoosesLandFromAnyBattlefield() {
        Permanent playerOneMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent playerTwoMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.castFromHand(player1, new SunderingTitan(), "{8}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                playerOneMountain.getId(), playerTwoMountain.getId());
        assertThat(choice.context()).isInstanceOf(
                MultiPermanentChoiceContext.ChooseLandOfEachBasicTypeThenDestroyChoice.class);

        harness.handleMultiplePermanentsChosen(player1, List.of(playerTwoMountain.getId()));

        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertOnBattlefield(player1, "Sundering Titan");
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(playerTwoMountain.getCard());
    }

    @Test
    @DisplayName("ETB chooses and destroys one land for each available basic land type")
    void etbChoosesOneLandOfEachAvailableBasicLandType() {
        Permanent playerOneIsland = harness.addToBattlefieldAndReturn(player1, new Island());
        Permanent playerTwoIsland = harness.addToBattlefieldAndReturn(player2, new Island());
        Permanent playerOneMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent playerTwoMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.castFromHand(player1, new SunderingTitan(), "{8}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice islandChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(islandChoice).isNotNull();
        assertThat(islandChoice.validIds()).containsExactlyInAnyOrder(
                playerOneIsland.getId(), playerTwoIsland.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(playerTwoIsland.getId()));

        PendingInteraction.MultiPermanentChoice mountainChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(mountainChoice).isNotNull();
        assertThat(mountainChoice.validIds()).containsExactlyInAnyOrder(
                playerOneMountain.getId(), playerTwoMountain.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(playerOneMountain.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getId)
                .contains(playerOneIsland.getId())
                .doesNotContain(playerOneMountain.getId());
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getId)
                .contains(playerTwoMountain.getId())
                .doesNotContain(playerTwoIsland.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(playerOneMountain.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(playerTwoIsland.getCard());
    }

    @Test
    @DisplayName("When it leaves, its controller chooses lands and the selected lands are destroyed")
    void leavesBattlefieldTriggersTheSameEffect() {
        Permanent titan = harness.addToBattlefieldAndReturn(player1, new SunderingTitan());
        Permanent playerOneMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent playerTwoMountain = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, titan.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(
                playerOneMountain.getId(), playerTwoMountain.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(playerOneMountain.getId()));

        harness.assertNotOnBattlefield(player1, "Sundering Titan");
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player1, "Sundering Titan");
    }

    @Test
    void resolvesWithoutLands() {
        harness.castFromHand(player1, new SunderingTitan(), "{8}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Sundering Titan");
    }

    @Test
    void ignoresLandsWithoutBasicLandTypes() {
        harness.addToBattlefield(player2, new DarksteelCitadel());
        harness.addToBattlefield(player1, new Mountain());

        harness.castFromHand(player1, new SunderingTitan(), "{8}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player2, "Darksteel Citadel");
        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    void canChooseTheSameNonbasicLandForBothItsTypes() {
        Permanent dual = harness.addToBattlefieldAndReturn(player2, new VolcanicIsland());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Mountain());

        harness.castFromHand(player1, new SunderingTitan(), "{8}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(dual.getId()));

        harness.assertOnBattlefield(player2, "Volcanic Island");
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).contains(dual.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(dual.getId()));

        harness.assertOnBattlefield(player1, "Island");
        harness.assertOnBattlefield(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Volcanic Island");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(dual.getCard());
    }

    @Test
    void choosingADualLandForOneTypeDoesNotCoverItsOtherType() {
        Permanent dual = harness.addToBattlefieldAndReturn(player2, new VolcanicIsland());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        harness.castFromHand(player1, new SunderingTitan(), "{8}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Volcanic Island");
        harness.assertOnBattlefield(player1, "Mountain");
        harness.handleMultiplePermanentsChosen(player1, List.of(mountain.getId()));

        harness.assertNotOnBattlefield(player2, "Volcanic Island");
        harness.assertNotOnBattlefield(player1, "Mountain");
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(dual.getCard());
        harness.assertInGraveyard(player1, "Mountain");
    }

    @Test
    void returningTitanToHandAlsoTriggersLandDestruction() {
        Permanent titan = harness.addToBattlefieldAndReturn(player1, new SunderingTitan());
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player1, List.of(new EchoingTruth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, titan.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Sundering Titan");
        harness.assertOnBattlefield(player2, "Mountain");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mountain");
        harness.assertInGraveyard(player2, "Mountain");
    }
}
