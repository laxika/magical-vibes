package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HeirOfFalkenrath.class})
class HeirOfFalkenrathTest extends BaseCardTest {

    @Test
    @CardUsed({GrizzlyBears.class})
    void discardingACardTransformsHeir() {
        Permanent heir = harness.addToBattlefieldAndReturn(player1, new HeirOfFalkenrath());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, indexOf(heir), null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardCostChoice.class);

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(heir.isTransformed()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotActivateWithoutACardToDiscard() {
        Permanent heir = harness.addToBattlefieldAndReturn(player1, new HeirOfFalkenrath());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(heir), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(heir.isTransformed()).isFalse();
    }

    @Test
    void discardIsPaidBeforeTransformationResolves() {
        Permanent heir = harness.addToBattlefieldAndReturn(player1, new HeirOfFalkenrath());
        harness.setHand(player1, List.of(new HeirOfFalkenrath()));

        harness.activateAbility(player1, indexOf(heir), null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Heir of Falkenrath");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(heir.isTransformed()).isFalse();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(heir.isTransformed()).isTrue();
    }

    @Test
    void cannotActivateAgainWhileFirstActivationIsOnStack() {
        Permanent heir = harness.addToBattlefieldAndReturn(player1, new HeirOfFalkenrath());
        harness.setHand(player1, List.of(new HeirOfFalkenrath(), new HeirOfFalkenrath()));

        harness.activateAbility(player1, indexOf(heir), null, null);
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(heir), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(heir.isTransformed()).isTrue();
    }

    @Test
    void eachHeirCanActivateOnceInTheSameTurn() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HeirOfFalkenrath());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HeirOfFalkenrath());
        harness.setHand(player1, List.of(new HeirOfFalkenrath(), new HeirOfFalkenrath()));

        harness.activateAbility(player1, indexOf(first), null, null);
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, indexOf(second), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.isTransformed()).isTrue();
        assertThat(second.isTransformed()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void tappedSummoningSickHeirCanTransformAndStaysTapped() {
        Permanent heir = harness.addToBattlefieldAndReturn(player1, new HeirOfFalkenrath());
        heir.tap();
        heir.setSummoningSick(true);
        harness.setHand(player1, List.of(new HeirOfFalkenrath()));

        harness.activateAbility(player1, indexOf(heir), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(heir.isTransformed()).isTrue();
        assertThat(heir.isTapped()).isTrue();
        assertThat(heir.isSummoningSick()).isTrue();
    }

    @Test
    void transformedHeirCannotBeBlockedByGroundCreatureAndCannotActivateFrontAbility() {
        Permanent heir = harness.addToBattlefieldAndReturn(player1, new HeirOfFalkenrath());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new HeirOfFalkenrath());
        harness.setHand(player1, List.of(new HeirOfFalkenrath(), new HeirOfFalkenrath()));

        assertThat(harness.getBlockLegalityService().canBlockAttacker(gd, blocker, heir,
                gd.playerBattlefields.get(player2.getId()))).isTrue();

        harness.activateAbility(player1, indexOf(heir), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(harness.getBlockLegalityService().canBlockAttacker(gd, blocker, heir,
                gd.playerBattlefields.get(player2.getId()))).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(heir), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(heir.isTransformed()).isTrue();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
