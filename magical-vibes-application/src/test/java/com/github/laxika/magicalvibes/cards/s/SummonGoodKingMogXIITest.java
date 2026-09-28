package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MooglesValor;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonGoodKingMogXII.class, CounselOfTheSoratami.class, GrizzlyBears.class, MooglesValor.class})
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

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonGoodKingMogXII());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void createOneMoogleToken() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new MooglesValor()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castInstant(player1, 0, (UUID) null);
        harness.passBothPriorities();
    }

    private void castNoncreatureSpell(Card card) {
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.castInstant(player1, 0, (UUID) null);
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
