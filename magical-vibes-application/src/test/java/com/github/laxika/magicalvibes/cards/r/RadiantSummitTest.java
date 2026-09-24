package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrownedCatacomb;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RadiantSummit.class, DrownedCatacomb.class, Forest.class, Plains.class})
class RadiantSummitTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped without two basic lands")
    void entersTappedWithoutTwoBasicLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new DrownedCatacomb());

        harness.setHand(player1, List.of(new RadiantSummit()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Radiant Summit").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enters untapped with two basic lands")
    void entersUntappedWithTwoBasicLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());

        harness.setHand(player1, List.of(new RadiantSummit()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Radiant Summit").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opponent's basic lands do not satisfy the condition")
    void opponentBasicLandsDoNotCount() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Plains());

        harness.setHand(player1, List.of(new RadiantSummit()));
        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Radiant Summit").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Taps for red mana")
    void tapsForRedMana() {
        Permanent summit = addReadySummit();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(summit.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Taps for white mana")
    void tapsForWhiteMana() {
        Permanent summit = addReadySummit();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(summit.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    private Permanent addReadySummit() {
        Permanent summit = new Permanent(new RadiantSummit());
        summit.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(summit);
        return summit;
    }
}
