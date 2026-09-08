package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JaceThePerfectedMindTest extends BaseCardTest {

    @Test
    @DisplayName("+1 gives up to one target creature -3/-0 until your next turn")
    void plusOneShrinksTargetUntilYourNextTurn() {
        Permanent jace = addReadyJace(player1, 4);
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(bear.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("+1 may be activated without a target")
    void plusOneAllowsNoTarget() {
        Permanent jace = addReadyJace(player1, 4);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    @DisplayName("-2 mills three, then draws one below the graveyard threshold")
    void minusTwoMillsAndDrawsOneBelowThreshold() {
        Permanent jace = addReadyJace(player1, 4);
        stockLibrary(player1, 30);
        stockLibrary(player2, 30);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(27);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-2 draws three when milling makes a graveyard reach twenty cards")
    void minusTwoDrawsThreeAtThreshold() {
        Permanent jace = addReadyJace(player1, 4);
        stockLibrary(player1, 30);
        stockLibrary(player2, 30);
        harness.setGraveyard(player2, filler(19));

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(22);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-X mills three times X cards")
    void minusXMillsThreeTimesX() {
        Permanent jace = addReadyJace(player1, 6);
        stockLibrary(player2, 30);

        harness.activateAbility(player1, 0, 2, 2, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(24);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
        assertThat(jace.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    private List<Card> filler(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }

    private void stockLibrary(Player player, int count) {
        harness.setLibrary(player, filler(count));
        harness.setHand(player, List.of());
    }

    private Permanent addReadyJace(Player player, int loyalty) {
        Permanent perm = new Permanent(new JaceThePerfectedMind());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
