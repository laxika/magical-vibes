package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TheBloodskyMassacreTest extends BaseCardTest {

    @Test
    void chapterICreatesMenacingDemonBerserker() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheBloodskyMassacre());
        saga.setCounterCount(CounterType.LORE, 0);

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Demon Berserker"))
                .findFirst()
                .orElseThrow();
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
