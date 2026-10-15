package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummonValefor.class, AirElemental.class, GrizzlyBears.class, SerraAngel.class})
class SummonValeforTest extends BaseCardTest {

    @Test
    void chapterIHasEachOpponentChooseAmongTiedGreatestManaValueCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new SerraAngel());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent saga = addSagaWithLore(0);

        advanceToNextChapter();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handlePermanentChosen(player2, first.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(first).contains(second);
        assertThat(gd.playerHands.get(player2.getId())).contains(first.getCard());
        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
    }

    @Test
    void chaptersIIThroughIVTapAndStunUpToOneTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(1);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    @Test
    void chapterIDoesNothingWhenAnOpponentControlsNoCreatures() {
        harness.setHand(player2, java.util.List.of());
        addSagaWithLore(0);

        advanceToNextChapter();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    void enteringTriggersChapterIAndReturnsOnlyOpponentsGreatestCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new SerraAngel());
        Permanent greatest = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent smaller = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        Permanent saga = harness.enterBattlefieldAndReturn(player1, new SummonValefor());
        harness.passBothPriorities();

        assertThat(saga.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature, saga);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(smaller).doesNotContain(greatest);
        assertThat(gd.playerHands.get(player2.getId())).contains(greatest.getCard());
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    void laterChaptersStunAlreadyTappedCreatureAndSacrificeAfterFinalChapter(int startingLore) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        target.tap();
        target.setCounterCount(CounterType.STUN, 1);
        Permanent saga = addSagaWithLore(startingLore);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(2);
        if (startingLore == 3) {
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(saga);
            assertThat(gd.playerGraveyards.get(player1.getId())).contains(saga.getCard());
        } else {
            assertThat(gd.playerBattlefields.get(player1.getId())).contains(saga);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 3})
    void tapAndStunChaptersAllowChoosingNoTarget(int startingLore) {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        addSagaWithLore(startingLore);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getCounterCount(CounterType.STUN)).isZero();
    }

    @Test
    void chapterIITargetsControllersOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        addSagaWithLore(1);

        advanceToNextChapter();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(target.getCounterCount(CounterType.STUN)).isEqualTo(1);
    }

    private Permanent addSagaWithLore(int lore) {
        Permanent saga = harness.addToBattlefieldAndReturn(player1, new SummonValefor());
        saga.setCounterCount(CounterType.LORE, lore);
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
