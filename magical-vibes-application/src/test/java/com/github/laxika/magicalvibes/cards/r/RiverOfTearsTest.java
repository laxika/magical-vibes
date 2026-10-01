package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiverOfTears.class, Forest.class})
class RiverOfTearsTest extends BaseCardTest {

    @Test
    void producesBlueManaWhenNoLandWasPlayed() {
        Permanent river = addReadyRiver();

        harness.activateAbility(player1, 0, null, null);

        assertThat(river.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void producesBlackManaAfterItsControllerPlaysALand() {
        Permanent river = addReadyRiver();
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);

        harness.activateAbility(player1, 0, null, null);

        assertThat(river.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void ignoresLandsPlayedByOpponent() {
        Permanent river = addReadyRiver();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);

        harness.activateAbility(player1, 0, null, null);

        assertThat(river.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    private Permanent addReadyRiver() {
        Permanent river = harness.addToBattlefieldAndReturn(player1, new RiverOfTears());
        river.setSummoningSick(false);
        return river;
    }
}
