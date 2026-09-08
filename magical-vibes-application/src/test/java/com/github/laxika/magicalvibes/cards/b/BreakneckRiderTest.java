package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BreakneckRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms into Neck Breaker when no spells were cast last turn")
    void transformsWhenNoSpellsCastLastTurn() {
        harness.addToBattlefield(player1, new BreakneckRider());
        Permanent rider = findPermanent(player1, "Breakneck Rider");
        gd.spellsCastLastTurn.clear();

        advanceToUpkeepFor(player1);
        harness.passBothPriorities();

        assertThat(rider.isTransformed()).isTrue();
        assertThat(rider.getCard().getName()).isEqualTo("Neck Breaker");
        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not transform when a spell was cast last turn")
    void doesNotTransformWhenSpellCastLastTurn() {
        harness.addToBattlefield(player1, new BreakneckRider());
        Permanent rider = findPermanent(player1, "Breakneck Rider");
        gd.spellsCastLastTurn.put(player1.getId(), 1);

        advanceToUpkeepFor(player1);

        assertThat(rider.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Neck Breaker transforms back when a player cast two or more spells last turn")
    void transformsBackWhenTwoOrMoreSpellsWereCastLastTurn() {
        Permanent rider = addRiderTransformed();
        gd.spellsCastLastTurn.clear();
        gd.spellsCastLastTurn.put(player2.getId(), 2);

        advanceToUpkeepFor(player2);
        harness.passBothPriorities();

        assertThat(rider.isTransformed()).isFalse();
        assertThat(rider.getCard().getName()).isEqualTo("Breakneck Rider");
    }

    @Test
    @DisplayName("Neck Breaker boosts and grants trample to attacking creatures you control")
    void attackingCreaturesYouControlAreBoostedAndGainTrample() {
        Permanent rider = addRiderTransformed();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());

        markAttacking(player1, List.of(0, 1));
        markAttacking(player2, List.of(0));

        assertThat(gqs.getEffectivePower(gd, rider)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, rider)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, rider, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Neck Breaker does not affect nonattacking creatures you control")
    void nonattackingCreaturesAreNotAffected() {
        addRiderTransformed();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addRiderTransformed() {
        harness.addToBattlefield(player1, new BreakneckRider());
        Permanent rider = findPermanent(player1, "Breakneck Rider");
        gd.spellsCastLastTurn.clear();
        advanceToUpkeepFor(player1);
        harness.passBothPriorities();
        return rider;
    }

    private void advanceToUpkeepFor(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private void markAttacking(Player player, List<Integer> attackerIndices) {
        List<Permanent> battlefield = gd.playerBattlefields.get(player.getId());
        for (int index : attackerIndices) {
            battlefield.get(index).setAttacking(true);
        }
    }
}
