package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AvengerOfZendikarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates one Plant token for each land you control")
    void etbCreatesPlantTokenForEachLandYouControl() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Forest());

        castAvenger();

        List<Permanent> plants = findPlants(player1);
        assertThat(plants).hasSize(2);
        assertThat(plants).allSatisfy(plant -> {
            assertThat(plant.getEffectivePower()).isZero();
            assertThat(plant.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Landfall may put a +1/+1 counter on each Plant creature you control")
    void landfallPutsCountersOnPlants() {
        harness.addToBattlefield(player1, new Forest());
        Permanent nonPlant = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAvenger();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPlants(player1)).singleElement()
                .extracting(plant -> plant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(1);
        assertThat(nonPlant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanent(player1, "Avenger of Zendikar")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining landfall does not put counters on Plants")
    void decliningLandfallDoesNotPutCountersOnPlants() {
        harness.addToBattlefield(player1, new Forest());
        castAvenger();

        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPlants(player1)).singleElement()
                .extracting(plant -> plant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(0);
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new Forest());
        castAvenger();
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPlants(player1)).singleElement()
                .extracting(plant -> plant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(0);
    }

    private void castAvenger() {
        harness.setHand(player1, List.of(new AvengerOfZendikar()));
        harness.addMana(player1, ManaColor.GREEN, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Permanent> findPlants(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(com.github.laxika.magicalvibes.model.CardSubtype.PLANT))
                .toList();
    }
}
