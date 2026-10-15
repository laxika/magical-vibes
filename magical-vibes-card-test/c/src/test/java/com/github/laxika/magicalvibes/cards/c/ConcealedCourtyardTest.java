package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LongtuskCub;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConcealedCourtyard.class, Mountain.class, LongtuskCub.class})
class ConcealedCourtyardTest extends BaseCardTest {

    @Test
    void entersUntappedWithNoOtherLands() {
        castConcealedCourtyard();

        assertThat(findCourtyard(player1).isTapped()).isFalse();
    }

    @Test
    void entersUntappedWithTwoOtherLands() {
        addMountain(player1);
        addMountain(player1);

        castConcealedCourtyard();

        assertThat(findCourtyard(player1).isTapped()).isFalse();
    }

    @Test
    void entersTappedWithThreeOtherLands() {
        addMountain(player1);
        addMountain(player1);
        addMountain(player1);

        castConcealedCourtyard();

        assertThat(findCourtyard(player1).isTapped()).isTrue();
    }

    @Test
    void nonLandPermanentsDoNotCount() {
        harness.addToBattlefield(player1, new LongtuskCub());
        harness.addToBattlefield(player1, new LongtuskCub());
        harness.addToBattlefield(player1, new LongtuskCub());

        castConcealedCourtyard();

        assertThat(findCourtyard(player1).isTapped()).isFalse();
    }

    @Test
    void opponentsLandsDoNotCount() {
        addMountain(player2);
        addMountain(player2);
        addMountain(player2);

        castConcealedCourtyard();

        assertThat(findCourtyard(player1).isTapped()).isFalse();
    }

    @Test
    void tappingProducesWhiteMana() {
        addReadyCourtyard(player1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
    }

    @Test
    void freshlyPlayedCourtyardCanProduceManaAndPaysTapCost() {
        castConcealedCourtyard();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(findCourtyard(player1).isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappingProducesBlackMana() {
        addReadyCourtyard(player1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
    }

    private void castConcealedCourtyard() {
        harness.setHand(player1, List.of(new ConcealedCourtyard()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.playLand(player1, 0);
    }

    private Permanent addReadyCourtyard(Player player) {
        return addCreatureReady(player, new ConcealedCourtyard());
    }

    private void addMountain(Player player) {
        harness.addToBattlefield(player, new Mountain());
    }

    private Permanent findCourtyard(Player player) {
        return findPermanent(player, "Concealed Courtyard");
    }
}
