package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.f.FeralShadow;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NocturnalRaid.class, FeralShadow.class, BayFalcon.class})
class NocturnalRaidTest extends BaseCardTest {

    @Test
    @DisplayName("Black creatures get +2/+0, non-black creatures are unaffected")
    void boostsOnlyBlackCreatures() {
        Permanent blackCreature = addCreatureReady(player1, new FeralShadow());
        Permanent nonBlackCreature = addCreatureReady(player1, new BayFalcon());

        castNocturnalRaid();

        assertThat(blackCreature.getEffectivePower()).isEqualTo(4);
        assertThat(blackCreature.getEffectiveToughness()).isEqualTo(1);

        assertThat(nonBlackCreature.getEffectivePower()).isEqualTo(1);
        assertThat(nonBlackCreature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Opponent's black creatures also get +2/+0")
    void boostsAllPlayersBlackCreatures() {
        Permanent ownBlack = addCreatureReady(player1, new FeralShadow());
        Permanent opponentBlack = addCreatureReady(player2, new FeralShadow());

        castNocturnalRaid();

        assertThat(ownBlack.getEffectivePower()).isEqualTo(4);
        assertThat(opponentBlack.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Black creatures entering after resolution are not boosted")
    void doesNotBoostBlackCreaturesEnteringAfterResolution() {
        castNocturnalRaid();

        Permanent laterBlackCreature = addCreatureReady(player1, new FeralShadow());

        assertThat(laterBlackCreature.getEffectivePower()).isEqualTo(2);
        assertThat(laterBlackCreature.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent blackCreature = addCreatureReady(player1, new FeralShadow());

        castNocturnalRaid();

        assertThat(blackCreature.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blackCreature.getEffectivePower()).isEqualTo(2);
        assertThat(blackCreature.getEffectiveToughness()).isEqualTo(1);
    }

    private void castNocturnalRaid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new NocturnalRaid(), "{2}{B}{B}");
        harness.passBothPriorities();
    }
}
