package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PlanarNexus.class)
class PlanarNexusTest extends BaseCardTest {

    @Test
    void isEveryNonbasicLandType() {
        Permanent nexus = harness.addToBattlefieldAndReturn(player1, new PlanarNexus());

        assertThat(gqs.effectiveLandTypes(gd, nexus)).containsExactlyInAnyOrder(
                CardSubtype.CAVE,
                CardSubtype.DESERT,
                CardSubtype.GATE,
                CardSubtype.LAIR,
                CardSubtype.LOCUS,
                CardSubtype.MINE,
                CardSubtype.POWER_PLANT,
                CardSubtype.SPHERE,
                CardSubtype.TOWER,
                CardSubtype.URZAS
        );
    }

    @Test
    void tapsForColorlessMana() {
        harness.addToBattlefield(player1, new PlanarNexus());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void paysOneToTapForAnyColor() {
        harness.addToBattlefield(player1, new PlanarNexus());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }
}
