package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.y.YotianSoldier;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CopperhoofVorrac.class, YotianSoldier.class, Mountain.class})
class CopperhoofVorracTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each untapped permanent an opponent controls")
    void countsUntappedOpponentPermanents() {
        Permanent vorrac = addCreatureReady(player1, new CopperhoofVorrac());
        addCreatureReady(player2, new YotianSoldier());
        addCreatureReady(player2, new Mountain());

        assertThat(gqs.getEffectivePower(gd, vorrac)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, vorrac)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not count tapped or controller's permanents")
    void ignoresTappedAndOwnPermanents() {
        Permanent vorrac = addCreatureReady(player1, new CopperhoofVorrac());
        addCreatureReady(player1, new YotianSoldier());
        addCreatureReady(player2, new YotianSoldier());
        Permanent tappedMountain = addCreatureReady(player2, new Mountain());
        tappedMountain.tap();

        assertThat(gqs.getEffectivePower(gd, vorrac)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vorrac)).isEqualTo(3);
    }

    @Test
    @DisplayName("Updates as an opponent's permanent becomes tapped or untapped")
    void updatesWithTapStateChanges() {
        Permanent vorrac = addCreatureReady(player1, new CopperhoofVorrac());
        Permanent opponentPermanent = addCreatureReady(player2, new YotianSoldier());

        assertThat(gqs.getEffectivePower(gd, vorrac)).isEqualTo(3);

        opponentPermanent.tap();
        assertThat(gqs.getEffectivePower(gd, vorrac)).isEqualTo(2);

        opponentPermanent.untap();
        assertThat(gqs.getEffectivePower(gd, vorrac)).isEqualTo(3);
    }
}
