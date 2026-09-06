package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.Crusade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Meekstone.class, GrizzlyBears.class, HillGiant.class, Crusade.class, WhiteKnight.class})
class MeekstoneTest extends BaseCardTest {

    @Test
    @DisplayName("Tapped creature with power 3+ does not untap while Meekstone is out")
    void power3CreatureStaysTapped() {
        harness.addToBattlefield(player1, new Meekstone());
        Permanent giant = addCreatureReady(player1, new HillGiant()); // 3/3
        giant.tap();

        advanceToNextTurn(player2);

        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tapped creature with power under 3 untaps normally")
    void power2CreatureUntaps() {
        harness.addToBattlefield(player1, new Meekstone());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears()); // 2/2
        bears.tap();

        advanceToNextTurn(player2);

        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Affects opponents' creatures during their untap step")
    void affectsOpponentCreatures() {
        harness.addToBattlefield(player1, new Meekstone());
        Permanent opponentGiant = addCreatureReady(player2, new HillGiant()); // 3/3
        opponentGiant.tap();

        // player2's untap step
        advanceToNextTurn(player1);

        assertThat(opponentGiant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Meekstone remains effective while tapped")
    void remainsEffectiveWhileTapped() {
        Permanent meekstone = harness.addToBattlefieldAndReturn(player1, new Meekstone());
        meekstone.tap();
        Permanent giant = addCreatureReady(player1, new HillGiant()); // 3/3
        giant.tap();

        advanceToNextTurn(player2);

        assertThat(giant.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A creature boosted to power 3 is kept tapped")
    void affectsCreatureWithPowerBoost() {
        harness.addToBattlefield(player1, new Meekstone());
        harness.addToBattlefield(player1, new Crusade());
        Permanent knight = addCreatureReady(player1, new WhiteKnight()); // 2/2, 3/3 with Crusade
        knight.tap();

        advanceToNextTurn(player2);

        assertThat(knight.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Once Meekstone leaves, power 3+ creatures untap again")
    void untapsAfterMeekstoneLeaves() {
        Permanent meekstone = harness.addToBattlefieldAndReturn(player1, new Meekstone());
        Permanent giant = addCreatureReady(player1, new HillGiant()); // 3/3
        giant.tap();

        gd.playerBattlefields.get(player1.getId()).remove(meekstone);

        advanceToNextTurn(player2);

        assertThat(giant.isTapped()).isFalse();
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        Player nextActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(nextActivePlayer, TurnStep.UNTAP);
    }
}
