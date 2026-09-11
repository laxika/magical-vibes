package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhylathWorldSculptor.class, Forest.class, GrizzlyBears.class})
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

    private void castPhylath() {
        harness.setHand(player1, List.of(new PhylathWorldSculptor()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
