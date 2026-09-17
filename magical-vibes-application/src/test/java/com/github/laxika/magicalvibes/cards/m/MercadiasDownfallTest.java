package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HornedTroll;
import com.github.laxika.magicalvibes.cards.r.RishadanPort;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MercadiasDownfall.class, RishadanPort.class, Forest.class, HornedTroll.class})
class MercadiasDownfallTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts attacking creatures by the defending player's nonbasic land count")
    void boostsAttackingCreaturesByDefendingNonbasicLands() {
        harness.addToBattlefield(player1, new RishadanPort());
        harness.addToBattlefield(player1, new RishadanPort());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new RishadanPort());

        Permanent attacker = addCreatureReady(player2, new HornedTroll());
        attacker.setAttacking(true);
        Permanent nonAttacker = addCreatureReady(player2, new HornedTroll());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player1, new MercadiasDownfall(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(nonAttacker.getEffectivePower()).isEqualTo(2);
        assertThat(nonAttacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("The attacking creature boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent attacker = addCreatureReady(player2, new HornedTroll());
        attacker.setAttacking(true);
        harness.addToBattlefield(player1, new RishadanPort());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player1, new MercadiasDownfall(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not boost attacking creatures when the defending player controls no nonbasic lands")
    void doesNotBoostWithNoDefendingNonbasicLands() {
        harness.addToBattlefield(player1, new Forest());
        Permanent attacker = addCreatureReady(player2, new HornedTroll());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player1, new MercadiasDownfall(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Counts nonbasic lands for the defending player rather than the spell controller")
    void countsDefendingPlayersNonbasicLands() {
        harness.addToBattlefield(player1, new RishadanPort());
        harness.addToBattlefield(player1, new RishadanPort());
        harness.addToBattlefield(player2, new RishadanPort());
        Permanent attacker = addCreatureReady(player2, new HornedTroll());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player2, new MercadiasDownfall(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }
}
