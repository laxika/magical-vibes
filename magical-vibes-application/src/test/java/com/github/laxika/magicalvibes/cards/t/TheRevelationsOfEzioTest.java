package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AssassinInitiate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({TheRevelationsOfEzio.class, AssassinInitiate.class, GrizzlyBears.class})
class TheRevelationsOfEzioTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I destroys only a tapped creature an opponent controls")
    void chapterIDestroysTappedOpponentCreature() {
        Permanent ownTapped = addCreatureReady(player1, new GrizzlyBears());
        ownTapped.tap();
        Permanent opponentTapped = addCreatureReady(player2, new GrizzlyBears());
        opponentTapped.tap();
        Permanent opponentUntapped = addCreatureReady(player2, new GrizzlyBears());

        castSaga();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opponentTapped.getId())
                .doesNotContain(ownTapped.getId(), opponentUntapped.getId());

        harness.handlePermanentChosen(player1, opponentTapped.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(opponentTapped);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownTapped);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opponentUntapped);
    }

    @Test
    @DisplayName("Chapter II puts a +1/+1 counter on each attacking Assassin you control this turn")
    void chapterIIBuffsAttackingAssassins() {
        addSagaWithLore(1);
        Permanent assassin = addCreatureReady(player1, new AssassinInitiate());
        Permanent nonAssassin = addCreatureReady(player1, new GrizzlyBears());

        triggerNextChapter();
        harness.passBothPriorities();

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(assassin)));
        resolveAllTriggers();

        assertThat(assassin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonAssassin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Chapter III returns a target Assassin with an additional +1/+1 counter")
    void chapterIIIReturnsAssassinWithCounter() {
        Permanent saga = addSagaWithLore(2);
        AssassinInitiate assassinCard = new AssassinInitiate();
        GrizzlyBears nonAssassinCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(assassinCard, nonAssassinCard));

        triggerNextChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(assassinCard.getId())
                .doesNotContain(nonAssassinCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(assassinCard.getId()));
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Assassin Initiate");
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(saga).isNotIn(gd.playerBattlefields.get(player1.getId()));
    }

    private void castSaga() {
        harness.setHand(player1, List.of(new TheRevelationsOfEzio()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent addSagaWithLore(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheRevelationsOfEzio());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
