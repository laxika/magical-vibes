package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DreadStatuary;
import com.github.laxika.magicalvibes.cards.g.GnarlidPack;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvengerOfZendikar.class, DreadStatuary.class, GnarlidPack.class})
class AvengerOfZendikarTest extends BaseCardTest {

    @Test
    @DisplayName("ETB creates one Plant token for each land you control")
    void etbCreatesPlantTokenForEachLandYouControl() {
        harness.addToBattlefield(player1, new DreadStatuary());
        harness.addToBattlefield(player1, new DreadStatuary());
        harness.addToBattlefield(player2, new DreadStatuary());

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
        harness.addToBattlefield(player1, new DreadStatuary());
        Permanent nonPlant = harness.addToBattlefieldAndReturn(player1, new GnarlidPack());
        castAvenger();

        harness.setHand(player1, List.of(new DreadStatuary()));
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
        harness.addToBattlefield(player1, new DreadStatuary());
        castAvenger();

        harness.setHand(player1, List.of(new DreadStatuary()));
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
        harness.addToBattlefield(player1, new DreadStatuary());
        castAvenger();
        harness.setHand(player2, List.of(new DreadStatuary()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(findPlants(player1)).singleElement()
                .extracting(plant -> plant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(0);
    }

    @Test
    @DisplayName("ETB creates no Plants when you control no lands")
    void noLandsCreatesNoPlants() {
        harness.addToBattlefield(player2, new DreadStatuary());

        castAvenger();

        assertThat(findPlants(player1)).isEmpty();
        harness.assertOnBattlefield(player1, "Avenger of Zendikar");
    }

    @Test
    @DisplayName("ETB counts lands when the trigger resolves")
    void etbCountsLandsAtResolution() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new DreadStatuary());
        harness.addToBattlefield(player1, new DreadStatuary());
        harness.castFromHand(player1, new AvengerOfZendikar(), "{5}{G}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerGraveyards.get(player1.getId()).add(land.getCard());
        resolveAllTriggers();

        assertThat(findPlants(player1)).hasSize(1);
    }

    @Test
    @DisplayName("Landfall puts counters on every friendly Plant and no opposing Plants")
    void landfallAffectsAllFriendlyPlantsOnly() {
        harness.addToBattlefield(player1, new DreadStatuary());
        harness.addToBattlefield(player1, new DreadStatuary());
        harness.addToBattlefield(player2, new DreadStatuary());
        harness.enterBattlefieldAndReturn(player2, new AvengerOfZendikar());
        resolveAllTriggers();
        assertThat(findPlants(player2)).hasSize(1);
        castAvenger();

        harness.setHand(player1, List.of(new DreadStatuary()));
        harness.playLand(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPlants(player1)).hasSize(2).allSatisfy(plant ->
                assertThat(plant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
        assertThat(findPlants(player2)).singleElement().satisfies(plant ->
                assertThat(plant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    @DisplayName("Landfall still resolves after Avenger leaves the battlefield")
    void landfallResolvesWithoutAvenger() {
        harness.addToBattlefield(player1, new DreadStatuary());
        castAvenger();
        Permanent avenger = findPermanent(player1, "Avenger of Zendikar");
        harness.setHand(player1, List.of(new DreadStatuary()));
        harness.playLand(player1, 0);

        gd.playerBattlefields.get(player1.getId()).remove(avenger);
        gd.playerGraveyards.get(player1.getId()).add(avenger.getCard());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPlants(player1)).singleElement().satisfies(plant ->
                assertThat(plant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1));
    }

    private void castAvenger() {
        harness.castFromHand(player1, new AvengerOfZendikar(), "{5}{G}{G}");
        resolveAllTriggers();
    }

    private List<Permanent> findPlants(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(com.github.laxika.magicalvibes.model.CardSubtype.PLANT))
                .toList();
    }
}
