package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(WarriorsOfTiamat.class)
class WarriorsOfTiamatTest extends BaseCardTest {
    @Test
    void canAttackTheTurnItEnters() {
        harness.castFromHand(player1, new WarriorsOfTiamat(), "{4}{R}");
        resolveAllTriggers();

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertInHand(player1, "Warriors of Tiamat");
    }

    @Test
    void attackingAgainDoesNotConjureAnotherDuplicate() {
        addCreatureReady(player1, new WarriorsOfTiamat());
        harness.setHand(player1, List.of());

        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.performUntapStep(player1);
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void conjuredDuplicateCanAttackImmediatelyButDoesNotConjureAgain() {
        addCreatureReady(player1, new WarriorsOfTiamat());
        harness.setHand(player1, List.of());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        Card duplicate = gd.playerHands.get(player1.getId()).getFirst();
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, duplicate, "{4}{R}");
        resolveAllTriggers();
        int lifeBeforeAttack = gd.getLife(player2.getId());
        declareAttackers(List.of(0));
        resolveAllTriggers();
        resolveCombat();

        harness.assertLife(player2, lifeBeforeAttack - 4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void doubleTeamConjuresADuplicateAndRemovesDoubleTeamFromBothCards() {
        Permanent warriors = addCreatureReady(player1, new WarriorsOfTiamat());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, warriors, Keyword.DOUBLE_TEAM)).isFalse();
        Card copy = gd.playerHands.get(player1.getId()).stream()
                .filter(WarriorsOfTiamat.class::isInstance)
                .findFirst()
                .orElseThrow();
        assertThat(copy.getKeywords()).doesNotContain(Keyword.DOUBLE_TEAM);
    }
}
