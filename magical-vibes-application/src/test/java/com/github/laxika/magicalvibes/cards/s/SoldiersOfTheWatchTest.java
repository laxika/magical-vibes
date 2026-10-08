package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoldiersOfTheWatch.class})
class SoldiersOfTheWatchTest extends BaseCardTest {

    @Test
    void attackConjuresDistinctDuplicateAndBothLoseDoubleTeam() {
        Permanent soldiers = addCreatureReady(player1, new SoldiersOfTheWatch());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        assertThat(duplicate).isInstanceOf(SoldiersOfTheWatch.class);
        assertThat(duplicate.getId()).isNotEqualTo(soldiers.getCard().getId());
        assertThat(duplicate.hasKeyword(Keyword.DOUBLE_TEAM)).isFalse();
        assertThat(gqs.hasKeyword(gd, soldiers, Keyword.DOUBLE_TEAM)).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void originalAndCastDuplicateCannotConjureOnLaterAttack() {
        Permanent original = addCreatureReady(player1, new SoldiersOfTheWatch());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, duplicate, "{1}{W}");
        harness.passBothPriorities();
        Permanent conjured = findPermanents(player1, "Soldiers of the Watch").get(1);
        harness.performUntapStep(player1);
        conjured.setSummoningSick(false);

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.hasKeyword(gd, original, Keyword.DOUBLE_TEAM)).isFalse();
        assertThat(gqs.hasKeyword(gd, conjured, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void eachAttackerConjuresItsOwnDuplicate() {
        Permanent first = addCreatureReady(player1, new SoldiersOfTheWatch());
        Permanent second = addCreatureReady(player1, new SoldiersOfTheWatch());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2)
                .allSatisfy(card -> {
                    assertThat(card).isInstanceOf(SoldiersOfTheWatch.class);
                    assertThat(card.hasKeyword(Keyword.DOUBLE_TEAM)).isFalse();
                    assertThat(card.getId()).isNotEqualTo(first.getCard().getId())
                            .isNotEqualTo(second.getCard().getId());
                });
        assertThat(gd.playerHands.get(player1.getId()).get(0).getId())
                .isNotEqualTo(gd.playerHands.get(player1.getId()).get(1).getId());
        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_TEAM)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_TEAM)).isFalse();
    }

    @Test
    void opponentsAttackConjuresIntoTheirOwnHand() {
        Permanent soldiers = addCreatureReady(player2, new SoldiersOfTheWatch());

        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1)
                .allSatisfy(card -> {
                    assertThat(card).isInstanceOf(SoldiersOfTheWatch.class);
                    assertThat(card.hasKeyword(Keyword.DOUBLE_TEAM)).isFalse();
                });
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gqs.hasKeyword(gd, soldiers, Keyword.DOUBLE_TEAM)).isFalse();
    }
}
