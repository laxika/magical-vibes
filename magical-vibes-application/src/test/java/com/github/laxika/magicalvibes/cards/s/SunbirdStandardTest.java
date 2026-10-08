package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.d.DeepfathomEcho;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PlunderingPirate;
import com.github.laxika.magicalvibes.cards.w.WaterwindScout;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SunbirdStandard.class, SunbirdEffigy.class, AirElemental.class, GrizzlyBears.class,
        WaterwindScout.class, PlunderingPirate.class, DeepfathomEcho.class})
class SunbirdStandardTest extends BaseCardTest {

    @Test
    @DisplayName("Craft returns Sunbird Effigy with power and toughness equal to the crafted colors")
    void craftCountsDistinctColorsAmongMaterials() {
        Permanent standard = harness.addToBattlefieldAndReturn(player1, new SunbirdStandard());
        Permanent blueMaterial = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent greenMaterial = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleMultipleCardsChosen(player1,
                List.of(blueMaterial.getCard().getId(), greenMaterial.getCard().getId()));
        harness.passBothPriorities();

        Permanent effigy = findEffigy();
        assertThat(gqs.getEffectivePower(gd, effigy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, effigy)).isEqualTo(2);
        assertThat(gd.findExiledCard(blueMaterial.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(greenMaterial.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(standard, blueMaterial, greenMaterial);
    }

    @Test
    @DisplayName("Sunbird Effigy adds one mana of each crafted color")
    void addsManaOfEachCraftedColor() {
        Permanent effigy = craftWithBlueAndGreen();
        int effigyIndex = gd.playerBattlefields.get(player1.getId()).indexOf(effigy);

        harness.activateAbility(player1, effigyIndex, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLUE", "BLACK", "RED", "GREEN"})
    void standardProducesChosenColorImmediately(ManaColor color) {
        Permanent standard = harness.addToBattlefieldAndReturn(player1, new SunbirdStandard());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(standard.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void craftCanUseOneGraveyardCardAndReturnsUntappedWithHaste() {
        Permanent standard = harness.addToBattlefieldAndReturn(player1, new SunbirdStandard());
        standard.tap();
        WaterwindScout material = new WaterwindScout();
        harness.setGraveyard(player1, List.of(material));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(standard);
        assertThat(gd.findExiledCard(standard.getCard().getId())).isNotNull();
        assertThat(gd.findExiledCard(material.getId())).isNotNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        Permanent effigy = findEffigy();
        assertThat(effigy.getId()).isNotEqualTo(standard.getId());
        assertThat(effigy.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, effigy)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, effigy)).isEqualTo(1);
        assertThat(gd.findExiledCard(standard.getCard().getId())).isNull();

        harness.activateAbility(player1, 0, null, null);

        assertThat(effigy.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void repeatedColorsAndColorlessMaterialsDoNotIncreasePowerOrMana() {
        harness.addToBattlefield(player1, new SunbirdStandard());
        Permanent blueMaterial = harness.addToBattlefieldAndReturn(player1, new WaterwindScout());
        WaterwindScout secondBlue = new WaterwindScout();
        PlunderingPirate redMaterial = new PlunderingPirate();
        SunbirdStandard colorlessMaterial = new SunbirdStandard();
        harness.setGraveyard(player1, List.of(secondBlue, redMaterial, colorlessMaterial));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleMultipleCardsChosen(player1, List.of(blueMaterial.getCard().getId(),
                secondBlue.getId(), redMaterial.getId(), colorlessMaterial.getId()));
        harness.passBothPriorities();

        Permanent effigy = findEffigy();
        assertThat(gqs.getEffectivePower(gd, effigy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, effigy)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void craftingWithOnlyColorlessMaterialDiesAsZeroZero() {
        harness.addToBattlefield(player1, new SunbirdStandard());
        SunbirdStandard material = new SunbirdStandard();
        harness.setGraveyard(player1, List.of(material));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(material.getId())).isNotNull();
        harness.assertInGraveyard(player1, "Sunbird Standard");
    }

    @Test
    void craftCannotUseItsOwnSourceOrOpponentsMaterials() {
        Permanent standard = harness.addToBattlefieldAndReturn(player1, new SunbirdStandard());
        Permanent opposingMaterial = harness.addToBattlefieldAndReturn(player2, new SunbirdStandard());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(standard);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingMaterial);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void craftRequiresFiveManaWithoutExilingMaterialsOnFailure() {
        Permanent standard = harness.addToBattlefieldAndReturn(player1, new SunbirdStandard());
        WaterwindScout material = new WaterwindScout();
        harness.setGraveyard(player1, List.of(material));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(standard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(material);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void craftCannotBeActivatedOutsideMainPhase() {
        Permanent standard = harness.addToBattlefieldAndReturn(player1, new SunbirdStandard());
        WaterwindScout material = new WaterwindScout();
        harness.setGraveyard(player1, List.of(material));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("sorcery speed");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(standard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(material);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void singleMulticolorMaterialContributesEveryColor() {
        harness.addToBattlefield(player1, new SunbirdStandard());
        DeepfathomEcho material = new DeepfathomEcho();
        harness.setGraveyard(player1, List.of(material));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent effigy = findEffigy();
        assertThat(gqs.getEffectivePower(gd, effigy)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, effigy)).isEqualTo(2);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    void craftCannotBeActivatedDuringOpponentsTurn() {
        Permanent standard = harness.addToBattlefieldAndReturn(player1, new SunbirdStandard());
        WaterwindScout material = new WaterwindScout();
        harness.setGraveyard(player1, List.of(material));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("sorcery speed");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(standard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(material);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void craftCannotBeActivatedWhileSpellIsOnStack() {
        Permanent standard = harness.addToBattlefieldAndReturn(player1, new SunbirdStandard());
        WaterwindScout material = new WaterwindScout();
        harness.setGraveyard(player1, List.of(material));
        harness.castFromHand(player1, new SunbirdStandard(), "{3}");
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("stack is empty");

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(standard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(material);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).hasSize(1);
    }

    private Permanent findEffigy() {
        Permanent effigy = findPermanent(player1, "Sunbird Effigy");
        assertThat(effigy.isTransformed()).isTrue();
        assertThat(effigy.getCard()).isInstanceOf(SunbirdEffigy.class);
        return effigy;
    }

    private Permanent craftWithBlueAndGreen() {
        harness.addToBattlefieldAndReturn(player1, new SunbirdStandard());
        Permanent blueMaterial = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent greenMaterial = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleMultipleCardsChosen(player1,
                List.of(blueMaterial.getCard().getId(), greenMaterial.getCard().getId()));
        harness.passBothPriorities();

        return findEffigy();
    }
}
