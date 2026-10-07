package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonShiva.class, Forest.class, GrizzlyBears.class, TrollAscetic.class})
class SummonShivaTest extends BaseCardTest {

    @Test
    void chapterITapsAndStunsAnOpponentCreature() {
        Permanent saga = addSagaWithLore(0);
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(opponentCreature.getId());
        assertThat(choice.validIds()).doesNotContain(ownCreature.getId());

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(saga).isIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    void chapterIITapsAndStunsAnOpponentCreature() {
        addSagaWithLore(1);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void chapterIIIDrawsForEachTappedOpponentCreature() {
        Permanent saga = addSagaWithLore(2);
        Permanent tappedOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        tappedOpponentCreature.tap();
        Permanent untappedOpponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent tappedOwnCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        tappedOwnCreature.tap();
        Permanent tappedOpponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        tappedOpponentLand.tap();
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new Forest(), new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(untappedOpponentCreature.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void heavenlyStrikeRequiresATargetWhenAnOpponentCreatureExists(int lore) {
        addSagaWithLore(lore);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(opponentCreature.getId());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void heavenlyStrikeCannotChooseAnOpponentCreatureWithHexproof(int lore) {
        addSagaWithLore(lore);
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent hexproofCreature = harness.addToBattlefieldAndReturn(player2, new TrollAscetic());

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(legalTarget.getId());
        assertThat(choice.validIds()).doesNotContain(hexproofCreature.getId());
    }

    @Test
    void chapterITriggersWhenCastAndEntersTheBattlefield() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.castFromHand(player1, new SummonShiva(), "{3}{U}{U}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(findPermanent(player1, "Summon: Shiva").getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    void chapterIIAddsAnotherStunCounterToAnAlreadyTappedCreature() {
        addSagaWithLore(1);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.tap();
        opponentCreature.setCounterCount(CounterType.STUN, 1);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isEqualTo(2);
        harness.performUntapStep(player2);
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isEqualTo(1);
        harness.performUntapStep(player2);
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isZero();
        harness.performUntapStep(player2);
        assertThat(opponentCreature.isTapped()).isFalse();
    }

    @Test
    void heavenlyStrikeDoesNothingIfTargetBecomesControlledByItsController() {
        addSagaWithLore(0);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        gd.playerBattlefields.get(player2.getId()).remove(opponentCreature);
        gd.playerBattlefields.get(player1.getId()).add(opponentCreature);
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(opponentCreature.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void chapterIIICountsTappedCreaturesAtResolutionAndWaitsToSacrifice() {
        Permanent saga = addSagaWithLore(2);
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        first.tap();
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToNextChapter();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        first.untap();
        second.tap();
        third.tap();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
    }

    @Test
    void chapterIIIDrawsNothingWhenNoOpponentCreatureIsTapped() {
        Permanent saga = addSagaWithLore(2);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToNextChapter();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonShiva());
        saga.setCounterCount(CounterType.LORE, lore);
        return saga;
    }

    private void advanceToNextChapter() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DRAW);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
