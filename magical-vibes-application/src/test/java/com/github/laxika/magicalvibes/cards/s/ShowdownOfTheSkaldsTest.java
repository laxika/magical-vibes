package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShowdownOfTheSkalds.class, Forest.class, GrizzlyBears.class, Shock.class})
class ShowdownOfTheSkaldsTest extends BaseCardTest {

    @Test
    @DisplayName("Chapter I exiles the top four cards and grants play permission")
    void chapterIExilesTopFourCards() {
        List<Card> topCards = List.of(new Shock(), new Forest(), new Shock(), new Forest());
        harness.setLibrary(player1, topCards);
        addSaga(0);

        triggerChapter();
        harness.passBothPriorities();

        assertThat(topCards).allSatisfy(card -> {
            assertThat(gd.findExiledCard(card.getId())).isNotNull();
            assertThat(gd.exilePlayPermissions).containsEntry(card.getId(), player1.getId());
        });
    }

    @Test
    @DisplayName("Chapter II puts a counter on a creature you control when you cast a spell")
    void chapterIIPutsCounterOnCreatureYouControl() {
        addSaga(1);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        triggerChapter();
        harness.passBothPriorities();

        castCreatureToTrigger();
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validPermanentIds()).contains(ownCreature.getId())
                .doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Chapter III has the same temporary spell-cast trigger")
    void chapterIIIPutsCounterOnCreatureYouControl() {
        addSaga(2);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        triggerChapter();
        harness.passBothPriorities();

        castCreatureToTrigger();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new ShowdownOfTheSkalds());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void castCreatureToTrigger() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
    }

    @Test
    void chapterIAllowsLandPlayButKeepsTheLandLimit() {
        Card first = new Forest();
        Card second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new ShowdownOfTheSkalds(), "{2}{R}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.castFromExile(player1, first.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(first.getId()));
        assertThatThrownBy(() -> harness.castFromExile(player1, second.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(second);
    }

    @Test
    void chapterIRequiresNormalManaToCastExiledCards() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        harness.castFromHand(player1, new ShowdownOfTheSkalds(), "{2}{R}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castFromExile(player1, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> assertThat(permanent.getCard().getId()).isEqualTo(creature.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
    }

    @Test
    void chapterIIPutsACounterForEverySpellCast() {
        addSaga(1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        triggerChapter();
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Showdown of the Skalds");
        harness.assertInGraveyard(player1, "Showdown of the Skalds");

        for (int i = 0; i < 2; i++) {
            castCreatureToTrigger();
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, creature.getId());
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void chapterIIAlsoTriggersForNoncreatureSpells() {
        addSaga(1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        triggerChapter();
        harness.passBothPriorities();
        harness.castFromHand(player1, new ShowdownOfTheSkalds(), "{2}{R}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void chapterIPermissionLastsThroughTheEndOfTheNextTurn() {
        Card exiled = new Forest();
        harness.setLibrary(player1, List.of(exiled, new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.castFromHand(player1, new ShowdownOfTheSkalds(), "{2}{R}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsKey(exiled.getId());
        assertThatThrownBy(() -> harness.castFromExile(player2, exiled.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsKey(exiled.getId());
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(gd.exilePlayPermissions).doesNotContainKey(exiled.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiled);
    }

    @Test
    void chapterIIDoesNotTriggerOnOpponentSpellsAndExpiresThisTurn() {
        addSaga(1);
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        triggerChapter();
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
