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
        int kirriBasePower = gqs.getEffectivePower(gd, kirri);

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

    private Permanent addKirri(Player player) {
        return harness.addToBattlefieldAndReturn(player, new KirriTalentedSprout());
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
