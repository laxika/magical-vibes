package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunbirdStandard.class, SunbirdEffigy.class, AirElemental.class, GrizzlyBears.class})
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

        Permanent effigy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isTransformed() && permanent.getCard() instanceof SunbirdEffigy)
                .findFirst()
                .orElseThrow();
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

    private Permanent craftWithBlueAndGreen() {
        harness.addToBattlefieldAndReturn(player1, new SunbirdStandard());
        Permanent blueMaterial = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent greenMaterial = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleMultipleCardsChosen(player1,
                List.of(blueMaterial.getCard().getId(), greenMaterial.getCard().getId()));
        harness.passBothPriorities();

        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isTransformed() && permanent.getCard() instanceof SunbirdEffigy)
                .findFirst()
                .orElseThrow();
    }
}
