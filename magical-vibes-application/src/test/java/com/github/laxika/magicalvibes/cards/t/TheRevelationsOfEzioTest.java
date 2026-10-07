package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AssassinInitiate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BasimIbnIshaq;
import com.github.laxika.magicalvibes.cards.m.MentorOfTheMeek;
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

@CardUsed({TheRevelationsOfEzio.class, AssassinInitiate.class, GrizzlyBears.class,
        BasimIbnIshaq.class, MentorOfTheMeek.class})
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

    @Test
    @DisplayName("Chapter I does not destroy its target if it becomes untapped")
    void chapterITargetBecomesUntapped() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.tap();

        castSaga();
        harness.handlePermanentChosen(player1, target.getId());
        target.untap();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Chapter II counters each attacking Assassin but not an attacking non-Assassin")
    void chapterIIHandlesMultipleMixedAttackers() {
        addSagaWithLore(1);
        Permanent first = addCreatureReady(player1, new AssassinInitiate());
        Permanent second = addCreatureReady(player1, new AssassinInitiate());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());

        triggerNextChapter();
        harness.passBothPriorities();

        declareAttackers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(first),
                gd.playerBattlefields.get(player1.getId()).indexOf(second),
                gd.playerBattlefields.get(player1.getId()).indexOf(other)));
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(other.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Chapter II does not counter an opponent's attacking Assassin")
    void chapterIIExcludesOpponentAssassin() {
        addSagaWithLore(1);
        Permanent opponentAssassin = addCreatureReady(player2, new AssassinInitiate());

        triggerNextChapter();
        harness.passBothPriorities();

        declareAttackers(player2, List.of(
                gd.playerBattlefields.get(player2.getId()).indexOf(opponentAssassin)));
        resolveAllTriggers();

        assertThat(opponentAssassin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Chapter III cannot target an Assassin in an opponent's graveyard")
    void chapterIIIExcludesOpponentGraveyard() {
        addSagaWithLore(2);
        AssassinInitiate ownCard = new AssassinInitiate();
        AssassinInitiate opponentCard = new AssassinInitiate();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));

        triggerNextChapter();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).contains(ownCard.getId())
                .doesNotContain(opponentCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(ownCard.getId()));
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Assassin Initiate")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opponentCard);
    }
    @Test
    @DisplayName("Chapter III's entry counter applies before Mentor of the Meek checks power")
    void chapterIIICounterAppliesBeforeEntryTriggers() {
        addSagaWithLore(2);
        harness.addToBattlefield(player1, new MentorOfTheMeek());
        BasimIbnIshaq assassinCard = new BasimIbnIshaq();
        harness.setGraveyard(player1, List.of(assassinCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        triggerNextChapter();
        harness.handleMultipleCardsChosen(player1, List.of(assassinCard.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Basim Ibn Ishaq")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
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
