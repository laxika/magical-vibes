package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConcealedCourtyardTest extends BaseCardTest {

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
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new LlanowarElves()));
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new LlanowarElves()));
        gd.playerBattlefields.get(player1.getId()).add(new Permanent(new LlanowarElves()));

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
        Permanent permanent = new Permanent(new ConcealedCourtyard());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addMountain(Player player) {
        gd.playerBattlefields.get(player.getId()).add(new Permanent(new Mountain()));
    }

    private Permanent findCourtyard(Player player) {
        return findPermanent(player, "Concealed Courtyard");
    }
}
