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
    @Test
    @DisplayName("Boosts every attacker and keeps the resolved boost after combat ends")
    void boostsEveryAttackerAndPersistsAfterCombat() {
        harness.addToBattlefield(player1, new RishadanPort());
        Permanent first = addCreatureReady(player2, new HornedTroll());
        Permanent second = addCreatureReady(player2, new HornedTroll());
        first.setAttacking(true);
        second.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player1, new MercadiasDownfall(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(first.getEffectivePower()).isEqualTo(3);
        assertThat(second.getEffectivePower()).isEqualTo(3);
        assertThat(first.getEffectiveToughness()).isEqualTo(2);
        assertThat(second.getEffectiveToughness()).isEqualTo(2);

        first.setAttacking(false);
        second.setAttacking(false);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        assertThat(first.getEffectivePower()).isEqualTo(3);
        assertThat(second.getEffectivePower()).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts lands at resolution and does not recalculate the boost afterward")
    void locksLandCountAtResolution() {
        harness.addToBattlefield(player1, new RishadanPort());
        Permanent attacker = addCreatureReady(player2, new HornedTroll());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.castFromHand(player2, new MercadiasDownfall(), "{2}{R}");
        harness.addToBattlefield(player1, new RishadanPort());
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).clear();
        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        harness.addToBattlefield(player1, new RishadanPort());
        assertThat(attacker.getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Creatures that begin attacking after resolution do not receive the boost")
    void doesNotBoostLaterAttackers() {
        harness.addToBattlefield(player1, new RishadanPort());
        Permanent creature = addCreatureReady(player2, new HornedTroll());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.castFromHand(player2, new MercadiasDownfall(), "{2}{R}");
        harness.passBothPriorities();
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        creature.setAttacking(true);

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }
}
