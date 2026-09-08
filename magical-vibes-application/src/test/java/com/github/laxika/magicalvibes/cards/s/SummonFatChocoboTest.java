package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonFatChocobo.class, Forest.class, GrizzlyBears.class})
class SummonFatChocoboTest extends BaseCardTest {

    @Test
    void chapterICreatesBirdWithLandfallBoost() {
        addSagaWithLore(0);
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        advanceToNextChapter();
        harness.passBothPriorities();

        Permanent bird = findBird();
        assertThat(bird.getEffectivePower()).isEqualTo(2);
        assertThat(bird.getEffectiveToughness()).isEqualTo(2);

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(bird.getEffectivePower()).isEqualTo(3);
        assertThat(bird.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void chaptersIIAndIIIGrantTrampleUntilEndOfTurn() {
        Permanent saga = addSagaWithLore(1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(creature.getGrantedKeywords()).contains(Keyword.TRAMPLE);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);

        saga.setCounterCount(CounterType.LORE, 2);
        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(creature.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    void chapterIVGrantsTrampleAndThenSacrificesTheSaga() {
        Permanent saga = addSagaWithLore(3);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToNextChapter();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);

        harness.passBothPriorities();

        assertThat(creature.getGrantedKeywords()).contains(Keyword.TRAMPLE);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card == saga.getCard());
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonFatChocobo());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent findBird() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.BIRD))
                .findFirst()
                .orElseThrow();
    }
}
