package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattleAtTheHelvault.class, GrizzlyBears.class, Forest.class, Naturalize.class})
class BattleAtTheHelvaultTest extends BaseCardTest {

    @Test
    @DisplayName("Chapters I and II exile up to one non-Saga, nonland permanent per player")
    void chaptersIAndIIExileOnePermanentPerPlayer() {
        Permanent saga = addSaga(0);
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent opponentSaga = harness.addToBattlefieldAndReturn(player2, new BattleAtTheHelvault());

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .contains(ownBear.getId(), opponentBear.getId())
                .doesNotContain(opponentForest.getId(), opponentSaga.getId());

        harness.handlePermanentChosen(player1, ownBear.getId());
        harness.handlePermanentChosen(player1, opponentBear.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ownBear);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentBear);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownBear.getCard());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentBear.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentForest, opponentSaga);
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);

        Permanent chapterTwoBear = addCreatureReady(player2, new GrizzlyBears());
        triggerChapter();
        harness.handlePermanentChosen(player1, chapterTwoBear.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(chapterTwoBear);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(chapterTwoBear.getCard());
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Exiled permanents return when Battle at the Helvault leaves")
    void exiledPermanentsReturnWhenSagaLeaves() {
        Permanent saga = addSaga(0);
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBear = addCreatureReady(player2, new GrizzlyBears());

        triggerChapter();
        harness.handlePermanentChosen(player1, ownBear.getId());
        harness.handlePermanentChosen(player1, opponentBear.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castInstant(player2, 0, saga.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownBear);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentBear);
        assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
    }

    @Test
    @DisplayName("Chapter III creates legendary Avacyn with flying, vigilance, and indestructible")
    void chapterIIICreatesAvacyn() {
        addSaga(2);

        triggerChapter();
        harness.passBothPriorities();

        Permanent avacyn = findPermanent(player1, "Avacyn");
        assertThat(avacyn.getCard().getPower()).isEqualTo(8);
        assertThat(avacyn.getCard().getToughness()).isEqualTo(8);
        assertThat(avacyn.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(avacyn.getCard().getKeywords())
                .contains(Keyword.FLYING, Keyword.VIGILANCE, Keyword.INDESTRUCTIBLE);
        assertThat(avacyn.getCard().getSubtypes())
                .containsExactly(CardSubtype.ANGEL);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new BattleAtTheHelvault());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

}
