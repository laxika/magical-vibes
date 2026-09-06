package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheLastRonin.class, GrizzlyBears.class})
class TheLastRoninTest extends BaseCardTest {

    @Test
    void chapterIDestroysAllCreatures() {
        addSaga(0);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.CREATURE));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getCard().hasType(CardType.CREATURE));
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void chapterIIMillsFourThenReturnsTargetCreatureFromGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature, new Card(), new Card(), new Card()));
        addSaga(1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getId()));
    }

    @Test
    void chapterIIIBuffsAControlledCreatureThatAttacksAlone() {
        Permanent saga = addSaga(2);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        advanceToNextChapter();
        harness.passBothPriorities();

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @Test
    void chapterIIIDoesNotBuffWhenMultipleCreaturesAttack() {
        addSaga(2);
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        advanceToNextChapter();
        harness.passBothPriorities();

        List<Integer> attackerIndices = List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(first),
                gd.playerBattlefields.get(player1.getId()).indexOf(second));
        declareAttackers(player1, attackerIndices);
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, first, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.LIFELINK)).isFalse();
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheLastRonin());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
