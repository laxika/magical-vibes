package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.DrownedCatacomb;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RadiantSummit.class, Forest.class, Plains.class, DrownedCatacomb.class})
class RadiantSummitTest extends BaseCardTest {

    @Test
    void entersTappedWithoutBasicLands() {
        playRadiantSummit();

        assertThat(findRadiantSummit(player1).isTapped()).isTrue();
    }

    @Test
    void entersTappedWithFewerThanTwoBasicLands() {
        harness.addToBattlefield(player1, new Forest());

        playRadiantSummit();

        assertThat(findRadiantSummit(player1).isTapped()).isTrue();
    }

    @Test
    void entersUntappedWithTwoBasicLands() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());

        playRadiantSummit();

        assertThat(findRadiantSummit(player1).isTapped()).isFalse();
    }

    @Test
    void twoBasicLandsWithTheSameNameCount() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        playRadiantSummit();

        assertThat(findRadiantSummit(player1).isTapped()).isFalse();
    }

    @Test
    void entersUntappedWithMoreThanTwoTappedBasicLands() {
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
        harness.addToBattlefieldAndReturn(player1, new Forest()).tap();
        harness.addToBattlefieldAndReturn(player1, new Plains()).tap();

        playRadiantSummit();

        assertThat(findRadiantSummit(player1).isTapped()).isFalse();
    }

    @Test
    void basicLandTypesDoNotMakeANonbasicLandCount() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new RadiantSummit());

        playRadiantSummit();

        assertThat(findPermanents(player1, "Radiant Summit").get(1).isTapped()).isTrue();
    }

    @Test
    void nonbasicLandsDoNotCount() {
        harness.addToBattlefield(player1, new DrownedCatacomb());
        harness.addToBattlefield(player1, new DrownedCatacomb());

        playRadiantSummit();

        assertThat(findRadiantSummit(player1).isTapped()).isTrue();
    }

    @Test
    void opponentsBasicLandsDoNotCount() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Plains());

        playRadiantSummit();

        assertThat(findRadiantSummit(player1).isTapped()).isTrue();
    }

    @Test
    void basicLandsControlledByDifferentPlayersAreNotCombined() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new Plains());

        playRadiantSummit();

        assertThat(findRadiantSummit(player1).isTapped()).isTrue();
    }

    @Test
    void tappingProducesRedMana() {
        addCreatureReady(player1, new RadiantSummit());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    void tappingProducesWhiteMana() {
        addCreatureReady(player1, new RadiantSummit());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    private void playRadiantSummit() {
        harness.setHand(player1, List.of(new RadiantSummit()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent findRadiantSummit(Player player) {
        return findPermanent(player, "Radiant Summit");
    }
}
