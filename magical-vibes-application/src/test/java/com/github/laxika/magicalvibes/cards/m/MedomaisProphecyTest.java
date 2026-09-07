package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MedomaisProphecy.class, Forest.class, Island.class, Shock.class})
class MedomaisProphecyTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I scries two")
    void chapterIScriesTwo() {
        Permanent saga = addSagaWithLore(0);
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest()));

        advanceToNextChapter();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(2);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
    }

    @Test
    @DisplayName("Chapter II stores the chosen card name")
    void chapterIIChoosesCardName() {
        Permanent saga = addSagaWithLore(1);
        harness.setLibrary(player1, List.of(new Forest(), new Island()));

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "Forest");

        assertThat(saga.getChosenName()).isEqualTo("Forest");
    }

    @Test
    @DisplayName("Chapter III draws only for the first spell with the chosen name")
    void chapterIIIDrawsForFirstChosenNameSpell() {
        addSagaWithLore(1);
        harness.setLibrary(player1, List.of(new Shock(), new Forest(), new Island(), new Forest()));

        advanceToNextChapter();
        harness.handleListChoice(player1, "Shock");

        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest(), new Island()));
        advanceToNextChapter();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Chapter IV looks at each library and then sacrifices the Saga")
    void chapterIVLooksAtEachLibrary() {
        Permanent saga = addSagaWithLore(3);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Island()));

        advanceToNextChapter();

        assertThat(gd.playerDecks.get(player1.getId())).first().isInstanceOf(Forest.class);
        assertThat(gd.playerDecks.get(player2.getId())).first().isInstanceOf(Island.class);
        assertThat(gameLogContains("looks at the top card of each player's library")).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new MedomaisProphecy());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
