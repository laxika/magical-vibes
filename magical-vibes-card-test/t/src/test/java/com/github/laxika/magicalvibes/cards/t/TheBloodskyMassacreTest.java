package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.b.BloodskyBerserker;
import com.github.laxika.magicalvibes.cards.w.WarchanterSkald;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheBloodskyMassacre.class, BloodskyBerserker.class, WarchanterSkald.class})
class TheBloodskyMassacreTest extends BaseCardTest {

    @Test
    void chapterICreatesMenacingDemonBerserker() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBloodskyMassacre());
        saga.setCounterCount(CounterType.LORE, 0);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Demon Berserker");
        assertThat(token.getEffectivePower()).isEqualTo(2);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DEMON, CardSubtype.BERSERKER);
        assertThat(token.getCard().getKeywords()).contains(Keyword.MENACE);
    }

    @Test
    void chapterIIFiresForEachAttackingBerserker() {
        harness.setLibrary(player1, List.of(new Card(), new Card(), new Card()));
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBloodskyMassacre());
        saga.setCounterCount(CounterType.LORE, 1);
        addCreatureReady(player2, creature("Berserker", List.of(CardSubtype.BERSERKER)));
        addCreatureReady(player2, creature("Not a Berserker", List.of()));

        advanceToNextChapter();
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    @Test
    void chapterIIICreatesPersistentRedManaForControlledBerserkers() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBloodskyMassacre());
        saga.setCounterCount(CounterType.LORE, 2);
        addCreatureReady(player1, creature("First Berserker", List.of(CardSubtype.BERSERKER)));
        addCreatureReady(player1, creature("Second Berserker", List.of(CardSubtype.BERSERKER)));

        advanceToNextChapter();
        harness.passBothPriorities();

        var manaPool = gd.playerManaPools.get(player1.getId());
        assertThat(manaPool.get(ManaColor.RED)).isEqualTo(2);
        assertThat(manaPool.getPersistentMana(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    void chapterIIDrawsForEachBerserkerEvenAfterSagaLeaves() {
        harness.setLibrary(player1, List.of(new WarchanterSkald(), new WarchanterSkald(), new WarchanterSkald()));
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBloodskyMassacre());
        saga.setCounterCount(CounterType.LORE, 1);
        addCreatureReady(player1, new BloodskyBerserker());
        addCreatureReady(player1, new BloodskyBerserker());
        addCreatureReady(player1, new WarchanterSkald());

        advanceToNextChapter();
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(saga);
        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 18);
    }

    @Test
    void chapterIIExpiresAtEndOfTurn() {
        harness.setLibrary(player1, List.of(new WarchanterSkald(), new WarchanterSkald(), new WarchanterSkald()));
        harness.setLibrary(player2, List.of(new WarchanterSkald(), new WarchanterSkald()));
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBloodskyMassacre());
        saga.setCounterCount(CounterType.LORE, 1);
        addCreatureReady(player2, new BloodskyBerserker());

        advanceToNextChapter();
        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        declareAttackers(player2, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertLife(player1, 20);
    }

    @Test
    void chapterIIIManaSurvivesPhasesButExpiresAtEndOfTurn() {
        harness.setLibrary(player2, List.of(new WarchanterSkald(), new WarchanterSkald()));
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBloodskyMassacre());
        saga.setCounterCount(CounterType.LORE, 2);
        addCreatureReady(player1, new BloodskyBerserker());
        addCreatureReady(player1, new WarchanterSkald());
        addCreatureReady(player2, new BloodskyBerserker());

        advanceToNextChapter();
        resolveAllTriggers();
        harness.assertInGraveyard(player1, "The Bloodsky Massacre");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);

        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
    }
    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private static Card creature(String name, List<CardSubtype> subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setColor(CardColor.RED);
        card.setSubtypes(subtypes);
        card.setPower(0);
        card.setToughness(1);
        return card;
    }
}
