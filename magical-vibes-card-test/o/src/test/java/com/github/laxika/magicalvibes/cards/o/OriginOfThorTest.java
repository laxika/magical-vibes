package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OriginOfThor.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class OriginOfThorTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I may discard a card to draw two cards")
    void chapterIDiscardsAndDrawsTwoCards() {
        Card discarded = new GrizzlyBears();
        Card firstDraw = new Forest();
        Card secondDraw = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        addSaga(0);

        triggerChapter();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw, secondDraw);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("Chapter II puts a counter on a creature you control for each spell cast that turn")
    void chapterIICountersCreatureAfterSpellCast() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSaga(1);

        triggerChapter();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(ownCreature.getId()).doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Chapter III deals the chosen creature's power to each opponent")
    void chapterIIIDealsTargetCreaturePowerToOpponents() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent saga = addSaga(2);

        triggerChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(source.getId()).doesNotContain(opponentCreature.getId());
        harness.handlePermanentChosen(player1, source.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(source).doesNotContain(saga);
    }

    @Test
    void chapterIMayBeDeclined() {
        Card kept = new GrizzlyBears();
        Card undrawn = new Forest();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(undrawn));
        addSaga(0);

        triggerChapter();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void chapterICannotDrawWithoutDiscarding() {
        Card undrawn = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(undrawn));
        addSaga(0);

        triggerChapter();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
    }

    @Test
    void chapterIITriggersForMultipleSpellsEvenAfterSagaLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent saga = addSaga(1);
        triggerChapter();
        gd.playerBattlefields.get(player1.getId()).remove(saga);
        gd.playerGraveyards.get(player1.getId()).add(saga.getCard());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        for (int i = 0; i < 2; i++) {
            harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, creature.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void chapterIIIUsesPowerAtResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        addSaga(2);
        triggerChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(15);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void chapterIIIDoesNotDealDamageWhenTargetLeaves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent saga = addSaga(2);
        triggerChapter();
        harness.handlePermanentChosen(player1, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new OriginOfThor());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
