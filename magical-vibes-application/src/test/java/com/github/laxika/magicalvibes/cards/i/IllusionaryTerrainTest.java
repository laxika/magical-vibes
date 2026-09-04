package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredMountain;
import com.github.laxika.magicalvibes.cards.v.VolcanicIsland;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
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

@CardUsed({IllusionaryTerrain.class, Mountain.class, Plains.class, SnowCoveredMountain.class})
class IllusionaryTerrainTest extends BaseCardTest {

    private Permanent terrainWithTypes(CardSubtype from, CardSubtype to) {
        Permanent terrain = harness.addToBattlefieldAndReturn(player1, new IllusionaryTerrain());
        terrain.setChosenSubtype(from);
        terrain.setSecondChosenSubtype(to);
        return terrain;
    }

    @Test
    @DisplayName("Resolving Illusionary Terrain awaits two basic land type choices")
    void resolvingTriggersTwoBasicLandTypeChoices() {
        harness.setHand(player1, List.of(new IllusionaryTerrain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "MOUNTAIN");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "ISLAND");

        Permanent terrain = findPermanent(player1, "Illusionary Terrain");
        assertThat(terrain.getChosenSubtype()).isEqualTo(CardSubtype.MOUNTAIN);
        assertThat(terrain.getSecondChosenSubtype()).isEqualTo(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("The two chosen basic land types may be the same")
    void mayChooseSameTypeTwice() {
        harness.setHand(player1, List.of(new IllusionaryTerrain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "MOUNTAIN");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "MOUNTAIN");

        Permanent terrain = findPermanent(player1, "Illusionary Terrain");
        assertThat(terrain.getChosenSubtype()).isEqualTo(CardSubtype.MOUNTAIN);
        assertThat(terrain.getSecondChosenSubtype()).isEqualTo(CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("A basic Mountain taps for blue when Mountain→Island is chosen")
    void basicMountainProducesChosenSecondTypeMana() {
        harness.addToBattlefield(player1, new Mountain());
        terrainWithTypes(CardSubtype.MOUNTAIN, CardSubtype.ISLAND);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    @DisplayName("A basic Plains is unaffected when Mountain→Island is chosen")
    void otherBasicTypeUnaffected() {
        harness.addToBattlefield(player1, new Plains());
        terrainWithTypes(CardSubtype.MOUNTAIN, CardSubtype.ISLAND);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Also converts a basic Mountain the opponent controls")
    void convertsOpponentBasicMountain() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        terrainWithTypes(CardSubtype.MOUNTAIN, CardSubtype.PLAINS);

        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.PLAINS);
    }

    @Test
    @CardUsed(VolcanicIsland.class)
    @DisplayName("A nonbasic dual land with the chosen type is unaffected")
    void nonbasicUnaffected() {
        Permanent volcanicIsland = harness.addToBattlefieldAndReturn(player1, new VolcanicIsland());
        terrainWithTypes(CardSubtype.MOUNTAIN, CardSubtype.ISLAND);

        assertThat(gqs.effectiveBasicLandTypes(gd, volcanicIsland))
                .containsExactlyInAnyOrder(CardSubtype.ISLAND, CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("A basic Mountain entering later is converted")
    void convertsBasicMountainEnteringLater() {
        terrainWithTypes(CardSubtype.MOUNTAIN, CardSubtype.ISLAND);
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
        assertThat(gqs.effectiveBasicLandTypes(gd, mountain)).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("Changing a snow-covered Mountain preserves its snow supertype")
    void preservesSnowSupertype() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new SnowCoveredMountain());
        terrainWithTypes(CardSubtype.MOUNTAIN, CardSubtype.ISLAND);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
        assertThat(gqs.hasEffectiveSupertype(gd, mountain, CardSupertype.SNOW)).isTrue();
    }

    @Test
    @DisplayName("A Mountain taps for red again once Illusionary Terrain leaves")
    void redResumesWhenTerrainLeaves() {
        harness.addToBattlefield(player1, new Mountain());
        Permanent terrain = terrainWithTypes(CardSubtype.MOUNTAIN, CardSubtype.ISLAND);

        gd.playerBattlefields.get(player1.getId()).remove(terrain);
        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Illusionary Terrain")
    void paysCumulativeUpkeep() {
        Permanent terrain = harness.addToBattlefieldAndReturn(player1, new IllusionaryTerrain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(terrain.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(terrain);
    }

    @Test
    @DisplayName("Cumulative upkeep increases with each age counter")
    void paysIncreasingCumulativeUpkeep() {
        Permanent terrain = harness.addToBattlefieldAndReturn(player1, new IllusionaryTerrain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(terrain.getCounterCount(CounterType.AGE)).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(terrain);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Illusionary Terrain")
    void declineSacrifices() {
        Permanent terrain = harness.addToBattlefieldAndReturn(player1, new IllusionaryTerrain());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(terrain);
        harness.assertInGraveyard(player1, "Illusionary Terrain");
    }

    @Test
    @DisplayName("Cumulative upkeep triggers only during Illusionary Terrain's controller's upkeep")
    void cumulativeUpkeepOnlyTriggersDuringControllersUpkeep() {
        Permanent terrain = harness.addToBattlefieldAndReturn(player1, new IllusionaryTerrain());

        advanceToUpkeep(player2);

        assertThat(terrain.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
