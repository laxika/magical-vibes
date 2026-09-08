package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.SummonEsperMaduin;
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

@CardUsed({EsperOrigins.class, SummonEsperMaduin.class, Forest.class, GrizzlyBears.class})
class EsperOriginsTest extends BaseCardTest {

    @Test
    void normalCastSurveilsGainsLifeAndGoesToGraveyard() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new EsperOrigins()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertInGraveyard(player1, "Esper Origins");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void flashbackTransformsTheSpellWithAFinalityCounter() {
        harness.setLibrary(player1, List.of());
        EsperOrigins card = new EsperOrigins();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        Permanent saga = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof SummonEsperMaduin)
                .findFirst()
                .orElseThrow();
        assertThat(saga.isTransformed()).isTrue();
        assertThat(saga.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
        harness.assertNotInGraveyard(player1, "Esper Origins");
        harness.assertLife(player1, 22);
    }

    @Test
    void firstChapterRevealsPermanentToHand() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonEsperMaduin());
        saga.setCounterCount(CounterType.LORE, 0);
        harness.setLibrary(player1, List.of(new Forest()));

        advanceToNextChapter();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void thirdChapterBoostsAndGivesTrampleToOtherCreatures() {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonEsperMaduin());
        saga.setCounterCount(CounterType.LORE, 2);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
