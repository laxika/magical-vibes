package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AssassinInitiate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OriginOfTheHiddenOnes.class, AssassinInitiate.class, GrizzlyBears.class})
class OriginOfTheHiddenOnesTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I deals 4 damage to the chosen target")
    void chapterIDealsDamageToTarget() {
        addSagaWithLore(0);
        int lifeBefore = gd.getLife(player2.getId());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 4);
    }

    @Test
    @DisplayName("Chapter II creates two Assassin tokens")
    void chapterIICreatesAssassinTokens() {
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(assassinTokens()).hasSize(2);
    }

    @Test
    @DisplayName("Chapter III creates a tapped attacking token for each attacking Assassin")
    void chapterIIICreatesTokenForEachAttackingAssassin() {
        addSagaWithLore(2);
        Permanent assassin = addCreatureReady(player1, new AssassinInitiate());
        Permanent nonAssassin = addCreatureReady(player1, new GrizzlyBears());

        advanceToNextChapter();
        harness.passBothPriorities();

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(assassin),
                gd.playerBattlefields.get(player1.getId()).indexOf(nonAssassin)));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(assassinTokens()).hasSize(1)
                .allSatisfy(token -> {
                    assertThat(token.isTapped()).isTrue();
                    assertThat(token.isAttackedThisTurn()).isTrue();
                });
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new OriginOfTheHiddenOnes());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private List<Permanent> assassinTokens() {
        return findPermanents(player1, "Assassin").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
    }
}
