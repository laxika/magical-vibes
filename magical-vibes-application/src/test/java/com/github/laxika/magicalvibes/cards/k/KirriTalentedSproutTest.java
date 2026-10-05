package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BattlewandOak;
import com.github.laxika.magicalvibes.cards.c.CarnivorousPlant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KirriTalentedSprout.class, CarnivorousPlant.class, BattlewandOak.class, GrizzlyBears.class,
        Forest.class})
class KirriTalentedSproutTest extends BaseCardTest {

    @Test
    @DisplayName("Other Plants and Treefolk you control get +2/+0")
    void boostsOtherPlantsAndTreefolk() {
        Permanent plant = harness.addToBattlefieldAndReturn(player1, new CarnivorousPlant());
        Permanent treefolk = harness.addToBattlefieldAndReturn(player1, new BattlewandOak());
        Permanent nonMatching = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentPlant = harness.addToBattlefieldAndReturn(player2, new CarnivorousPlant());

        int plantBasePower = gqs.getEffectivePower(gd, plant);
        int treefolkBasePower = gqs.getEffectivePower(gd, treefolk);
        int nonMatchingBasePower = gqs.getEffectivePower(gd, nonMatching);
        int opponentPlantBasePower = gqs.getEffectivePower(gd, opponentPlant);
        Permanent kirri = addKirri(player1);
        int kirriBasePower = kirri.getCard().getPower();

        assertThat(gqs.getEffectivePower(gd, kirri)).isEqualTo(kirriBasePower);
        assertThat(gqs.getEffectivePower(gd, plant)).isEqualTo(plantBasePower + 2);
        assertThat(gqs.getEffectivePower(gd, treefolk)).isEqualTo(treefolkBasePower + 2);
        assertThat(gqs.getEffectivePower(gd, nonMatching)).isEqualTo(nonMatchingBasePower);
        assertThat(gqs.getEffectivePower(gd, opponentPlant)).isEqualTo(opponentPlantBasePower);
    }

    @Test
    @DisplayName("Postcombat main trigger returns a target Plant, Treefolk, or land card")
    void returnsMatchingCardFromGraveyardToHand() {
        addKirri(player1);
        Card plant = new CarnivorousPlant();
        Card treefolk = new BattlewandOak();
        Card land = new Forest();
        Card nonMatching = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(plant, treefolk, land, nonMatching));

        advanceToPostcombatMain(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(plant.getId(), treefolk.getId(), land.getId());

        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void returnsPlantToHand() {
        assertReturnsToHand(new CarnivorousPlant());
    }

    @Test
    void returnsTreefolkToHand() {
        assertReturnsToHand(new BattlewandOak());
    }

    @Test
    void doesNotTriggerDuringPrecombatMain() {
        addKirri(player1);
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        harness.forceStep(TurnStep.DRAW);

        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
    }

    @Test
    void triggersAtEachPostcombatMainPhase() {
        addKirri(player1);
        Card plant = new CarnivorousPlant();
        Card treefolk = new BattlewandOak();
        harness.setGraveyard(player1, List.of(plant, treefolk));

        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of(plant.getId()));
        harness.passBothPriorities();

        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of(treefolk.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(plant, treefolk);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsPostcombatMain() {
        addKirri(player1);
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));

        advanceToPostcombatMain(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
    }

    @Test
    void doesNotOfferOpponentsGraveyardCards() {
        addKirri(player1);
        Card ownLand = new Forest();
        Card opposingPlant = new CarnivorousPlant();
        harness.setGraveyard(player1, List.of(ownLand));
        harness.setGraveyard(player2, List.of(opposingPlant));

        advanceToPostcombatMain(player1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(ownLand.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownLand.getId()));
        harness.passBothPriorities();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingPlant);
    }

    @Test
    void hasNoTargetWhenOnlyNonmatchingCardsAreInGraveyard() {
        addKirri(player1);
        Card bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));

        advanceToPostcombatMain(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bear);
    }

    @Test
    void doesNotReturnTargetThatLeavesGraveyardBeforeResolution() {
        addKirri(player1);
        Card land = new Forest();
        harness.setGraveyard(player1, List.of(land));
        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.stack).isEmpty();
    }

    private void assertReturnsToHand(Card card) {
        addKirri(player1);
        harness.setGraveyard(player1, List.of(card));
        advanceToPostcombatMain(player1);
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(card);
    }

    private Permanent addKirri(Player player) {
        return harness.addToBattlefieldAndReturn(player, new KirriTalentedSprout());
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.passUntil(activePlayer, TurnStep.POSTCOMBAT_MAIN);
    }
}
