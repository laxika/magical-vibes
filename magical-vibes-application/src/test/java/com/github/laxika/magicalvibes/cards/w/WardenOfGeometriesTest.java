package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WardenOfGeometries.class)
class WardenOfGeometriesTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Warden of Geometries produces one colorless mana")
    void tappingProducesColorlessMana() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new WardenOfGeometries());
        warden.setSummoningSick(false);

        harness.tapPermanent(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(warden.isTapped()).isTrue();
    }
}
