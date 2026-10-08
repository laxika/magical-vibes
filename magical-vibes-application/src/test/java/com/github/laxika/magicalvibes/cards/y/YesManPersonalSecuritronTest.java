package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YesManPersonalSecuritron.class, Forest.class})
class YesManPersonalSecuritronTest extends BaseCardTest {

    @Test
    void givesControlDrawsAndAddsQuestCounter() {
        addReadyYesMan();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        Permanent yesMan = findPermanent(player2, "Yes Man, Personal Securitron");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(yesMan.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    void createsTappedSoldiersForItsOwnerWhenItLeaves() {
        addReadyYesMan();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();
        Permanent yesMan = findPermanent(player2, "Yes Man, Personal Securitron");

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, yesMan));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
        assertThat(findPermanents(player1, "Soldier").getFirst().isTapped()).isTrue();
    }

    @Test
    void drawAndCounterWaitForSeparateTriggeredAbility() {
        Permanent yesMan = addReadyYesMan();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Yes Man, Personal Securitron")).isSameAs(yesMan);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(yesMan.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(yesMan.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    void leavingBeforeControlTransferDoesNotDrawOrCreateSoldiers() {
        Permanent yesMan = addReadyYesMan();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, yesMan));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
    }

    @Test
    void createsOneTappedSoldierForEachQuestCounterAndIgnoresOtherCounters() {
        Permanent yesMan = addReadyYesMan();
        yesMan.setCounterCount(CounterType.QUEST, 3);
        yesMan.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, yesMan));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).hasSize(3).allMatch(Permanent::isTapped);
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
    }

    @Test
    void cannotTargetItsControllerOrActivateOnOpponentsTurn() {
        addReadyYesMan();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an opponent");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyYesMan() {
        YesManPersonalSecuritron yesManCard = new YesManPersonalSecuritron();
        yesManCard.setOwnerId(player1.getId());
        Permanent yesMan = addCreatureReady(player1, yesManCard);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return yesMan;
    }
}
