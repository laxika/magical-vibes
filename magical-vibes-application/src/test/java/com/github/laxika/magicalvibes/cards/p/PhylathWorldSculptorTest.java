package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CrawlingBarrens;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhylathWorldSculptor.class, Forest.class, GrizzlyBears.class, CrawlingBarrens.class})
class PhylathWorldSculptorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates one Plant token for each basic land you control")
    void etbCreatesPlantTokenForEachBasicLandYouControl() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        castPhylath();

        List<Permanent> plants = findPermanents(player1, "Plant");
        assertThat(plants).hasSize(2);
        assertThat(plants).allSatisfy(plant -> {
            assertThat(plant.getEffectivePower()).isZero();
            assertThat(plant.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Landfall puts four +1/+1 counters on a target Plant you control")
    void landfallPutsFourCountersOnTargetPlantYouControl() {
        harness.addToBattlefield(player1, new Forest());
        castPhylath();
        Permanent plant = findPermanent(player1, "Plant");

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, plant.getId());
        harness.passBothPriorities();

        assertThat(plant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(plant.getEffectivePower()).isEqualTo(4);
        assertThat(plant.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Landfall cannot target a non-Plant permanent")
    void landfallCannotTargetNonPlantPermanent() {
        harness.addToBattlefield(player1, new Forest());
        castPhylath();
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).doesNotContain(bear.getId());
    }

    @Test
    void etbDoesNotCountNonbasicLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new CrawlingBarrens());

        castPhylath();

        assertThat(findPermanents(player1, "Plant")).hasSize(1);
    }

    @Test
    void etbCreatesNoPlantsWithoutBasicLands() {
        harness.addToBattlefield(player1, new CrawlingBarrens());

        castPhylath();

        assertThat(findPermanents(player1, "Plant")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void etbCountsBasicLandsAtResolution() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.castFromHand(player1, new PhylathWorldSculptor(), "{4}{R}{G}");
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(forest);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Plant")).hasSize(1);
    }

    @Test
    void nonbasicLandAlsoTriggersLandfall() {
        harness.addToBattlefield(player1, new Forest());
        castPhylath();
        Permanent plant = findPermanent(player1, "Plant");

        harness.setHand(player1, List.of(new CrawlingBarrens()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, plant.getId());
        harness.passBothPriorities();

        assertThat(plant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void landfallCannotTargetOpponentsPlant() {
        harness.addToBattlefield(player2, new Forest());
        harness.enterBattlefieldAndReturn(player2, new PhylathWorldSculptor());
        harness.passBothPriorities();
        Permanent opposingPlant = findPermanent(player2, "Plant");
        harness.addToBattlefield(player1, new Forest());
        castPhylath();
        Permanent ownPlant = findPermanent(player1, "Plant");

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(ownPlant.getId()).doesNotContain(opposingPlant.getId());
        harness.handlePermanentChosen(player1, ownPlant.getId());
        harness.passBothPriorities();
        assertThat(ownPlant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(opposingPlant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsLandDoesNotTriggerLandfall() {
        harness.addToBattlefield(player1, new Forest());
        castPhylath();
        Permanent plant = findPermanent(player1, "Plant");

        harness.enterBattlefieldAndReturn(player2, new Forest());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(plant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void landfallWithNoLegalPlantDoesNotRequestTarget() {
        castPhylath();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Plant")).isEmpty();
    }

    @Test
    void landfallOnlyPutsCountersOnTheChosenPlant() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        castPhylath();
        List<Permanent> plants = findPermanents(player1, "Plant");

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, plants.get(0).getId());
        harness.passBothPriorities();

        assertThat(plants.get(0).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(plants.get(1).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void landfallDoesNotPutCountersOnPlantNowControlledByOpponent() {
        harness.addToBattlefield(player1, new Forest());
        castPhylath();
        Permanent plant = findPermanent(player1, "Plant");
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, plant.getId());

        gd.playerBattlefields.get(player1.getId()).remove(plant);
        gd.playerBattlefields.get(player2.getId()).add(plant);
        harness.passBothPriorities();

        assertThat(plant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void castPhylath() {
        harness.castFromHand(player1, new PhylathWorldSculptor(), "{4}{R}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
