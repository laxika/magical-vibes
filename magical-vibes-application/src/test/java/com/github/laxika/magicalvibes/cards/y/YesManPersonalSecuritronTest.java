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
        harness.passBothPriorities();

        Permanent yesMan = findPermanents(player2, "Yes Man, Personal Securitron").getFirst();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(yesMan.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    void createsTappedSoldiersForItsOwnerWhenItLeaves() {
        addReadyYesMan();
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        Permanent yesMan = findPermanents(player2, "Yes Man, Personal Securitron").getFirst();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, yesMan));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
        assertThat(findPermanents(player2, "Soldier")).isEmpty();
        assertThat(findPermanents(player1, "Soldier").getFirst().isTapped()).isTrue();
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
