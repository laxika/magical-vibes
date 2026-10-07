package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EverflowingChalice;
import com.github.laxika.magicalvibes.cards.m.MooglesValor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonGoodKingMogXII.class, CounselOfTheSoratami.class, GrizzlyBears.class, MooglesValor.class, EverflowingChalice.class})
class SummonGoodKingMogXIITest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I creates two lifelinking Moogles")
    void chapterICreatesMoogles() {
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(moogles(player1)).hasSize(2).allSatisfy(moogle -> {
            assertThat(moogle.getCard().getPower()).isEqualTo(1);
            assertThat(moogle.getCard().getToughness()).isEqualTo(2);
            assertThat(moogle.getCard().getKeywords()).contains(com.github.laxika.magicalvibes.model.Keyword.LIFELINK);
        });
    }

    @Test
    @DisplayName("Chapter II copies a non-Saga token after a noncreature spell is cast")
    void chapterIICopiesTokenOnNoncreatureSpell() {
        createOneMoogleToken();
        addSagaWithLore(1);
        advanceToNextChapter();

        int mooglesBefore = moogles(player1).size();
        castNoncreatureSpell(new CounselOfTheSoratami());

        assertThat(moogles(player1)).hasSize(mooglesBefore + 1);
    }

    @Test
    @DisplayName("Chapter III copies a non-Saga token after a noncreature spell is cast")
    void chapterIIICopiesTokenOnNoncreatureSpell() {
        createOneMoogleToken();
        addSagaWithLore(2);
        advanceToNextChapter();

        int mooglesBefore = moogles(player1).size();
        castNoncreatureSpell(new CounselOfTheSoratami());

        assertThat(moogles(player1)).hasSize(mooglesBefore + 1);
    }

    @Test
    @DisplayName("Chapter IV puts two +1/+1 counters on each other Moogle")
    void chapterIVBoostsOtherMoogles() {
        createOneMoogleToken();
        Permanent saga = addSagaWithLore(3);
        Permanent moogle = moogles(player1).getFirst();

        advanceToNextChapter();

        assertThat(moogle.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(saga.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void chapterIITriggersForEveryNoncreatureSpell() {
        addSagaWithLore(0);
        advanceToNextChapter();
        advanceToNextChapter();
        Permanent original = moogles(player1).getFirst();
        original.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        castChaliceAndCopy(original);
        castChaliceAndCopy(original);

        assertThat(moogles(player1)).hasSize(4);
        assertThat(moogles(player1)).filteredOn(p -> !p.getId().equals(original.getId()))
                .allSatisfy(p -> assertThat(p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    void chapterIIDoesNotCopyNontokenPermanents() {
        addSagaWithLore(1);
        advanceToNextChapter();

        harness.castFromHand(player1, new EverflowingChalice(), "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Everflowing Chalice"))
                .hasSize(1);
        assertThat(moogles(player1)).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIIDoesNotTriggerForOpponentsSpells() {
        addSagaWithLore(0);
        advanceToNextChapter();
        advanceToNextChapter();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player2, new EverflowingChalice(), "{0}");
        harness.passBothPriorities();

        assertThat(moogles(player1)).hasSize(2);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIIDoesNotTriggerForCreatureSpells() {
        addSagaWithLore(0);
        advanceToNextChapter();
        advanceToNextChapter();

        harness.castFromHand(player1, new SummonGoodKingMogXII(), "{4}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(moogles(player1)).hasSize(4);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIVBoostsOtherNontokenMooglesButNotOpponentsMoogles() {
        Permanent source = addSagaWithLore(3);
        Permanent other = harness.addToBattlefieldAndReturn(player1, new SummonGoodKingMogXII());
        other.setCounterCount(CounterType.LORE, 1);
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new SummonGoodKingMogXII());
        opposing.setCounterCount(CounterType.LORE, 1);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opposing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Summon: Good King Mog XII");
    }

    @Test
    void delayedTriggerContinuesAfterSagaIsSacrificed() {
        Permanent saga = addSagaWithLore(0);
        advanceToNextChapter();
        advanceToNextChapter();
        Permanent original = moogles(player1).getFirst();
        saga.setCounterCount(CounterType.LORE, 3);
        advanceToNextChapter();
        harness.assertInGraveyard(player1, "Summon: Good King Mog XII");

        castChaliceAndCopy(original);

        assertThat(moogles(player1)).hasSize(3);
    }

    @Test
    void delayedTriggerExpiresAtEndOfTurn() {
        addSagaWithLore(0);
        advanceToNextChapter();
        advanceToNextChapter();
        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new MooglesValor(), "{3}{W}{W}");
        harness.passBothPriorities();

        assertThat(moogles(player1)).hasSize(5);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castChaliceAndCopy(Permanent original) {
        harness.castFromHand(player1, new EverflowingChalice(), "{0}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, original.getId());
        harness.passBothPriorities();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonGoodKingMogXII());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void createOneMoogleToken() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.castFromHand(player1, new MooglesValor(), "{3}{W}{W}");
        harness.passBothPriorities();
    }

    private void castNoncreatureSpell(Card card) {
        harness.castFromHand(player1, card, "{2}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private List<Permanent> moogles(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> "Moogle".equals(permanent.getCard().getName()))
                .toList();
    }
}
