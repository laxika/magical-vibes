package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RaphaelTheNightwatcher.class, GrizzlyBears.class})
class RaphaelTheNightwatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your attacking creatures double strike, including itself")
    void givesYourAttackingCreaturesDoubleStrike() {
        Permanent raphael = harness.addToBattlefieldAndReturn(player1, new RaphaelTheNightwatcher());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        markAttacking(player1, List.of(0, 1));

        assertThat(gqs.hasKeyword(gd, raphael, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Does not give double strike to an opponent's attacker")
    void doesNotGiveDoubleStrikeToOpponentsAttacker() {
        harness.addToBattlefield(player1, new RaphaelTheNightwatcher());
        Permanent opponentAttacker = addCreatureReady(player2, new GrizzlyBears());
        opponentAttacker.setAttacking(true);

        assertThat(gqs.hasKeyword(gd, opponentAttacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Sneak returns an unblocked attacker and enters tapped and attacking")
    void sneakSwapsTheUnblockedAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new RaphaelTheNightwatcher()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
            harness.assertInHand(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.passBothPriorities();
        });

        harness.assertInHand(player1, "Grizzly Bears");
        Permanent raphael = findPermanent(player1, "Raphael, the Nightwatcher");

        assertThat(raphael.isTapped()).isTrue();
        assertThat(raphael.isAttacking()).isTrue();
        assertThat(raphael.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gqs.hasKeyword(gd, raphael, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Double strike updates when creatures stop attacking or Raphael leaves")
    void doubleStrikeTracksAttackingStateAndSourcePresence() {
        Permanent raphael = harness.addToBattlefieldAndReturn(player1, new RaphaelTheNightwatcher());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, raphael, Keyword.DOUBLE_STRIKE)).isFalse();
        attacker.setAttacking(true);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isTrue();
        attacker.setAttacking(false);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isFalse();
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).remove(raphael);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Sneak cannot be used outside the declare blockers step")
    void cannotSneakDuringMainPhase() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new RaphaelTheNightwatcher()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Raphael, the Nightwatcher");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private void markAttacking(Player player, List<Integer> attackerIndices) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int index : attackerIndices) {
            battlefield.get(index).setAttacking(true);
        }
    }
}
