package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.d.DimirAqueduct;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Terraformer.class, Forest.class, ImprisonedInTheMoon.class,
        DimirAqueduct.class, BloodMoon.class})
class TerraformerTest extends BaseCardTest {

    @Test
    @DisplayName("Each land controlled by the ability's controller becomes the chosen type")
    void allControllerLandsBecomeChosenType() {
        harness.addToBattlefield(player1, new Terraformer());
        Permanent forestA = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent forestB = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");

        assertThat(forestA.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
        assertThat(forestB.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("The ability does not change an opponent's lands")
    void opponentLandsAreUnaffected() {
        harness.addToBattlefield(player1, new Terraformer());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");

        assertThat(opponentForest.getTransientLandTypeOverride()).isNull();
    }

    @Test
    @DisplayName("The chosen type replaces the lands' existing land types")
    void chosenTypeReplacesExistingTypes() {
        harness.addToBattlefield(player1, new Terraformer());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");

        assertThat(forest.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
        assertThat(forest.getTransientSubtypes()).isEmpty();
    }

    @Test
    @DisplayName("A land entering after resolution is not affected")
    void landsEnteringAfterResolutionAreUnaffected() {
        harness.addToBattlefield(player1, new Terraformer());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");

        Permanent laterForest = harness.addToBattlefieldAndReturn(player1, new Forest());

        assertThat(laterForest.getTransientLandTypeOverride()).isNull();
    }

    @Test
    @DisplayName("An overridden land produces mana of the chosen type")
    void overriddenLandProducesChosenMana() {
        harness.addToBattlefield(player1, new Terraformer());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("The chosen type lasts only until end of turn")
    void chosenTypeExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new Terraformer());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(forest.getTransientLandTypeOverride()).isNull();
        assertThat(gqs.hasEffectiveSubtype(gd, forest, CardSubtype.FOREST)).isTrue();
    }

    @Test
    @DisplayName("A permanent that is currently a land is affected even without a printed land type")
    void currentlyLandPermanentIsAffected() {
        harness.addToBattlefield(player1, new Terraformer());
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player1, new Terraformer());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        aura.setAttachedTo(enchantedCreature.getId());
        harness.forceActivePlayer(player1);

        assertThat(gqs.isLand(gd, enchantedCreature)).isTrue();

        activateAndChoose("ISLAND");

        assertThat(enchantedCreature.getTransientLandTypeOverride()).isEqualTo(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("All five basic land types can be chosen with repeated activations from a tapped creature")
    void allBasicTypesCanBeChosenWithoutTappingTerraformer() {
        Permanent terraformer = harness.addToBattlefieldAndReturn(player1, new Terraformer());
        terraformer.tap();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        for (CardSubtype subtype : List.of(CardSubtype.PLAINS, CardSubtype.ISLAND, CardSubtype.SWAMP,
                CardSubtype.MOUNTAIN, CardSubtype.FOREST)) {
            activateAndChoose(subtype.name());
            assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(subtype);
        }
    }

    @Test
    @DisplayName("A nonbasic land loses its printed mana ability and remains nonbasic")
    void nonbasicLandProducesOnlyChosenMana() {
        harness.addToBattlefield(player1, new Terraformer());
        Permanent aqueduct = harness.addToBattlefieldAndReturn(player1, new DimirAqueduct());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");
        assertThat(gqs.hasEffectiveSupertype(gd, aqueduct, CardSupertype.BASIC)).isFalse();
        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(aqueduct));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("A later Blood Moon overrides Terraformer's earlier land type change")
    void laterBloodMoonOverridesChosenType() {
        harness.addToBattlefield(player1, new Terraformer());
        Permanent aqueduct = harness.addToBattlefieldAndReturn(player1, new DimirAqueduct());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");
        harness.setHand(player1, List.of(new BloodMoon()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, aqueduct)).containsExactly(CardSubtype.MOUNTAIN);
    }

    @Test
    @DisplayName("Terraformer overrides an earlier Blood Moon until end of turn")
    void earlierBloodMoonIsOverriddenUntilCleanup() {
        harness.addToBattlefield(player1, new Terraformer());
        Permanent aqueduct = harness.addToBattlefieldAndReturn(player1, new DimirAqueduct());
        harness.addToBattlefield(player1, new BloodMoon());
        harness.forceActivePlayer(player1);

        activateAndChoose("ISLAND");

        assertThat(gqs.effectiveBasicLandTypes(gd, aqueduct)).containsExactly(CardSubtype.ISLAND);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.effectiveBasicLandTypes(gd, aqueduct)).containsExactly(CardSubtype.MOUNTAIN);
    }

    private void activateAndChoose(String subtype) {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype);
    }
}
