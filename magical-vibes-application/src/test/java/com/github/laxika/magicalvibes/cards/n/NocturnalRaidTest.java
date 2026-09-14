package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.l.LoomingShade;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NocturnalRaid.class, LoomingShade.class, HornedTurtle.class})
class NocturnalRaidTest extends BaseCardTest {

    @Test
    @DisplayName("Black creatures get +2/+0, non-black creatures are unaffected")
    void boostsOnlyBlackCreatures() {
        Permanent blackCreature = addCreatureReady(player1, new LoomingShade());
        Permanent nonBlackCreature = addCreatureReady(player1, new HornedTurtle());

        castNocturnalRaid();

        assertThat(blackCreature.getEffectivePower()).isEqualTo(3);
        assertThat(blackCreature.getEffectiveToughness()).isEqualTo(1);

        assertThat(nonBlackCreature.getEffectivePower()).isEqualTo(1);
        assertThat(nonBlackCreature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's black creatures also get +2/+0")
    void boostsAllPlayersBlackCreatures() {
        Permanent ownBlack = addCreatureReady(player1, new LoomingShade());
        Permanent opponentBlack = addCreatureReady(player2, new LoomingShade());

        castNocturnalRaid();

        assertThat(ownBlack.getEffectivePower()).isEqualTo(3);
        assertThat(opponentBlack.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Black creatures entering after resolution are not boosted")
    void doesNotBoostBlackCreaturesEnteringAfterResolution() {
        castNocturnalRaid();

        Permanent laterBlackCreature = addCreatureReady(player1, new LoomingShade());

        assertThat(laterBlackCreature.getEffectivePower()).isEqualTo(1);
        assertThat(laterBlackCreature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent blackCreature = addCreatureReady(player1, new LoomingShade());

        castNocturnalRaid();

        assertThat(blackCreature.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blackCreature.getEffectivePower()).isEqualTo(1);
        assertThat(blackCreature.getEffectiveToughness()).isEqualTo(1);
    }

    private void castNocturnalRaid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new NocturnalRaid(), "{2}{B}{B}");
        harness.passBothPriorities();
    }
}
