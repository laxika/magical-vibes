package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RiverOfTears.class})
class RiverOfTearsTest extends BaseCardTest {

    @Test
    void producesBlueManaWhenNoLandWasPlayed() {
        Permanent river = addCreatureReady(player1, new RiverOfTears());

        harness.activateAbility(player1, 0, null, null);

        assertThat(river.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void producesBlackManaAfterItsControllerPlaysALand() {
        Permanent river = addCreatureReady(player1, new RiverOfTears());
        harness.setHand(player1, List.of(new RiverOfTears()));
        harness.playLand(player1, 0);

        harness.activateAbility(player1, 0, null, null);

        assertThat(river.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    void ignoresLandsPlayedByOpponent() {
        Permanent river = addCreatureReady(player1, new RiverOfTears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new RiverOfTears()));
        harness.playLand(player2, 0);

        harness.activateAbility(player1, 0, null, null);

        assertThat(river.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    void producesBlackManaOnTheTurnItIsPlayed() {
        harness.setHand(player1, List.of(new RiverOfTears()));
        harness.playLand(player1, 0);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void puttingALandOntoTheBattlefieldDoesNotChangeManaToBlack() {
        Permanent river = harness.enterBattlefieldAndReturn(player1, new RiverOfTears());
        harness.enterBattlefieldAndReturn(player1, new RiverOfTears());

        harness.activateAbility(player1, 0, null, null);

        assertThat(river.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void producesBlueAgainOnTheOpponentsNextTurn() {
        addCreatureReady(player1, new RiverOfTears());
        harness.setHand(player1, List.of(new RiverOfTears()));
        harness.playLand(player1, 0);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

}
