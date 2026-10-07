package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.BlowOffSteam;
import com.github.laxika.magicalvibes.cards.f.FrolickingFamiliar;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheConundrumOfBowls.class, GrizzlyBears.class, HillGiant.class, Opt.class,
        FrolickingFamiliar.class, BlowOffSteam.class})
class TheConundrumOfBowlsTest extends BaseCardTest {

    @Test
    void chapterISeeksCardWithManaValueLessThanHandSize() {
        Card sought = new Opt();
        Card equal = new GrizzlyBears();
        Card greater = new HillGiant();
        harness.setHand(player1, List.of(new Opt(), new Opt()));
        harness.setLibrary(player1, List.of(equal, sought, greater));
        addSaga(0);

        triggerChapter();

        assertThat(gd.playerHands.get(player1.getId())).contains(sought);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equal, greater);
    }

    @Test
    void chapterIISeeksCardWithManaValueGreaterThanHandSize() {
        Card lower = new Opt();
        Card equal = new GrizzlyBears();
        Card greater = new HillGiant();
        harness.setHand(player1, List.of(new Opt(), new Opt()));
        harness.setLibrary(player1, List.of(lower, equal, greater));
        addSaga(1);

        triggerChapter();

        assertThat(gd.playerHands.get(player1.getId())).contains(greater);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(lower, equal);
    }

    @Test
    void chapterIIIOffersSpellWithManaValueEqualToHandSizeForFree() {
        Card equal = new GrizzlyBears();
        Card greater = new HillGiant();
        harness.setHand(player1, List.of(equal, greater));
        addSaga(2);

        triggerChapter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(equal.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(greater);
    }

    @Test
    void enteringBattlefieldTriggersChapterI() {
        Card sought = new Opt();
        Card equal = new GrizzlyBears();
        harness.setHand(player1, List.of(new Opt(), new Opt()));
        harness.setLibrary(player1, List.of(equal, sought));

        harness.enterBattlefieldAndReturn(player1, new TheConundrumOfBowls());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(sought);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equal);
    }

    @Test
    void chapterIWithEmptyHandDoesNotSeek() {
        Card card = new Opt();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(card));
        addSaga(0);

        triggerChapter();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIIWithNoGreaterManaValueDoesNotSeek() {
        Card equal = new GrizzlyBears();
        Card lower = new Opt();
        harness.setHand(player1, List.of(new Opt(), new Opt()));
        harness.setLibrary(player1, List.of(equal, lower));
        addSaga(1);

        triggerChapter();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(equal, lower);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIIICanBeDeclinedAndSagaIsSacrificed() {
        Card eligible = new GrizzlyBears();
        Card other = new HillGiant();
        harness.setHand(player1, List.of(eligible, other));
        addSaga(2);

        triggerChapter();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(eligible, other);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "The Conundrum of Bowls");
        harness.assertInGraveyard(player1, "The Conundrum of Bowls");
    }

    @Test
    void chapterIIICastsOnlyOneOfMultipleEligibleSpells() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setHand(player1, List.of(first, second));
        addSaga(2);

        triggerChapter();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(first.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(second.getId()));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void chapterIIICanDeclineFirstEligibleSpellAndCastSecond() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setHand(player1, List.of(first, second));
        addSaga(2);

        triggerChapter();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(second.getId()));
    }

    @Test
    void chapterIIIWithoutEqualManaValueOffersNothing() {
        Card lower = new Opt();
        Card greater = new HillGiant();
        harness.setHand(player1, List.of(lower, greater));
        addSaga(2);

        triggerChapter();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(lower, greater);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "The Conundrum of Bowls");
    }

    @Test
    void chapterIIIOffersAdventureWhoseSpellManaValueMatchesHandSize() {
        Card adventure = new FrolickingFamiliar();
        harness.setHand(player1, List.of(adventure));
        addSaga(2);

        triggerChapter();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(adventure);
    }

    private Permanent addSaga(int loreCounters) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new TheConundrumOfBowls());
        saga.setCounterCount(CounterType.LORE, loreCounters);
        return saga;
    }

    private void triggerChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.passUntil(TurnStep.PRECOMBAT_MAIN);
        harness.passBothPriorities();
    }
}
