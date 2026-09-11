package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KnightErrant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValorousCharge.class, KnightErrant.class, GrizzlyBears.class})
class ValorousChargeTest extends BaseCardTest {

    @Test
    @DisplayName("White creatures get +2/+0, non-white creatures are unaffected")
    void boostsOnlyWhiteCreatures() {
        Permanent whiteCreature = addCreatureReady(player1, new KnightErrant()); // 2/2 White
        Permanent greenCreature = addCreatureReady(player1, new GrizzlyBears());  // 2/2 Green

        castValorousCharge();

        assertThat(whiteCreature.getEffectivePower()).isEqualTo(4);
        assertThat(whiteCreature.getEffectiveToughness()).isEqualTo(2);

        assertThat(greenCreature.getEffectivePower()).isEqualTo(2);
        assertThat(greenCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's white creatures also get +2/+0")
    void boostsAllPlayersWhiteCreatures() {
        Permanent ownWhite = addCreatureReady(player1, new KnightErrant());
        Permanent opponentWhite = addCreatureReady(player2, new KnightErrant());

        castValorousCharge();

        assertThat(ownWhite.getEffectivePower()).isEqualTo(4);
        assertThat(opponentWhite.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent whiteCreature = addCreatureReady(player1, new KnightErrant());

        castValorousCharge();

        assertThat(whiteCreature.getEffectivePower()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(whiteCreature.getEffectivePower()).isEqualTo(2);
        assertThat(whiteCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("White creatures entering after resolution are not boosted")
    void doesNotBoostWhiteCreaturesEnteringAfterResolution() {
        castValorousCharge();

        Permanent whiteCreature = addCreatureReady(player1, new KnightErrant());

        assertThat(whiteCreature.getEffectivePower()).isEqualTo(2);
        assertThat(whiteCreature.getEffectiveToughness()).isEqualTo(2);
    }

    private void castValorousCharge() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new ValorousCharge(), "{1}{W}{W}");
        harness.passBothPriorities();
    }
}
