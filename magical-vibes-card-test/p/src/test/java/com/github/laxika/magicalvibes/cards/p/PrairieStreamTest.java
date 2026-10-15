package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DrownedCatacomb;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrairieStream.class, Plains.class, Island.class, DrownedCatacomb.class})
class PrairieStreamTest extends BaseCardTest {

    @Test
    void entersTappedWithFewerThanTwoBasicLands() {
        harness.addToBattlefield(player1, new Plains());

        playPrairieStream();

        assertThat(findPrairieStream(player1).isTapped()).isTrue();
    }

    @Test
    void entersUntappedWithTwoBasicLands() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());

        playPrairieStream();

        assertThat(findPrairieStream(player1).isTapped()).isFalse();
    }

    @Test
    void nonbasicLandsDoNotCount() {
        harness.addToBattlefield(player1, new DrownedCatacomb());

        playPrairieStream();

        assertThat(findPrairieStream(player1).isTapped()).isTrue();
    }

    @Test
    void opponentsBasicLandsDoNotCount() {
        harness.addToBattlefield(player2, new Plains());
        harness.addToBattlefield(player2, new Island());

        playPrairieStream();

        assertThat(findPrairieStream(player1).isTapped()).isTrue();
    }

    @Test
    void tappingProducesWhiteMana() {
        harness.addToBattlefield(player1, new PrairieStream());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void tappingProducesBlueMana() {
        harness.addToBattlefield(player1, new PrairieStream());

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    void entersTappedWithoutBasicLands() {
        playPrairieStream();

        assertThat(findPrairieStream(player1).isTapped()).isTrue();
    }

    @Test
    void twoTappedBasicLandsOfTheSameTypeAreEnough() {
        harness.addToBattlefieldAndReturn(player1, new Plains()).tap();
        harness.addToBattlefieldAndReturn(player1, new Plains()).tap();

        playPrairieStream();

        assertThat(findPrairieStream(player1).isTapped()).isFalse();
    }

    @Test
    void entersUntappedWithMoreThanTwoBasicLands() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Plains());

        playPrairieStream();

        assertThat(findPrairieStream(player1).isTapped()).isFalse();
    }

    @Test
    void nonbasicLandWithBasicLandTypesDoesNotSupplySecondBasicLand() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new PrairieStream());

        playPrairieStream();

        assertThat(findPermanents(player1, "Prairie Stream")).hasSize(2);
        assertThat(findPermanents(player1, "Prairie Stream").get(1).isTapped()).isTrue();
    }

    @Test
    void replacementAppliesWhenPutOntoBattlefieldWithoutBeingPlayed() {
        Permanent permanent = harness.enterBattlefieldAndReturn(player1, new PrairieStream());

        assertThat(permanent.isTapped()).isTrue();
    }

    @Test
    void putOntoBattlefieldUntappedWithTwoBasicLands() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());

        Permanent permanent = harness.enterBattlefieldAndReturn(player1, new PrairieStream());

        assertThat(permanent.isTapped()).isFalse();
    }

    private void playPrairieStream() {
        harness.setHand(player1, List.of(new PrairieStream()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent findPrairieStream(Player player) {
        return findPermanent(player, "Prairie Stream");
    }
}
