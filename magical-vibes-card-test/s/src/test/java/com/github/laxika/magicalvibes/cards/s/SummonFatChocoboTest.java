package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonFatChocobo.class, Forest.class, SazhsChocobo.class})
class SummonFatChocoboTest extends BaseCardTest {

    @Test
    void chapterICreatesBirdWithLandfallBoost() {
        addSagaWithLore(0);
        harness.setHand(player1, List.of(new Forest()));

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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(creature.getGrantedKeywords()).contains(Keyword.TRAMPLE);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(creature.getGrantedKeywords()).doesNotContain(Keyword.TRAMPLE);

        saga.setCounterCount(CounterType.LORE, 2);
        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(creature.getGrantedKeywords()).contains(Keyword.TRAMPLE);
    }

    @Test
    void chapterIVGrantsTrampleAndThenSacrificesTheSaga() {
        Permanent saga = addSagaWithLore(3);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());

        advanceToNextChapter();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);

        harness.passBothPriorities();

        assertThat(creature.getGrantedKeywords()).contains(Keyword.TRAMPLE);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card == saga.getCard());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    void kerplunkAlsoGivesFatChocoboItselfTrample(int previousLore) {
        Permanent saga = addSagaWithLore(previousLore);

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, saga, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void castingSagaCreatesOneBirdOnEntry() {
        harness.castFromHand(player1, new SummonFatChocobo(), "{4}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
        assertThat(findBird().getEffectivePower()).isEqualTo(2);
        assertThat(findBird().getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void landfallBoostsAccumulateAndExpireAtEndOfTurn() {
        addSagaWithLore(0);
        advanceToNextChapter();
        harness.passBothPriorities();
        Permanent bird = findBird();

        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new Forest());
        harness.passBothPriorities();

        assertThat(bird.getEffectivePower()).isEqualTo(4);
        assertThat(bird.getEffectiveToughness()).isEqualTo(2);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(bird.getEffectivePower()).isEqualTo(2);
        assertThat(bird.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void opponentsLandDoesNotBoostBird() {
        addSagaWithLore(0);
        advanceToNextChapter();
        harness.passBothPriorities();
        Permanent bird = findBird();

        harness.enterBattlefieldAndReturn(player2, new Forest());
        harness.passBothPriorities();

        assertThat(bird.getEffectivePower()).isEqualTo(2);
        assertThat(bird.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void kerplunkAffectsOnlyControlledCreaturesPresentAtResolution() {
        addSagaWithLore(1);
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new SazhsChocobo());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToNextChapter();
        harness.passBothPriorities();
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player1, new SazhsChocobo());

        assertThat(gqs.hasKeyword(gd, ally, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponent, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, land, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, lateCreature, Keyword.TRAMPLE)).isFalse();
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
