package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlogurkTheOverslime.class, Mountain.class, GrizzlyBears.class})
class SlogurkTheOverslimeTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when a land card enters its controller's graveyard")
    void gainsCounterWhenLandIsPutIntoGraveyard() {
        Permanent slogurk = harness.addToBattlefieldAndReturn(player1, new SlogurkTheOverslime());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, mountain));
        resolveAllTriggers();

        assertThat(slogurk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes three counters to return itself and up to three lands from the graveyard")
    void returnsItselfAndThreeTargetLands() {
        Card first = new Mountain();
        Card second = new Mountain();
        Card third = new Mountain();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, new ArrayList<>(List.of(first, second, third, creature)));

        Permanent slogurk = harness.addToBattlefieldAndReturn(player1, new SlogurkTheOverslime());
        slogurk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(first.getId(), second.getId(), third.getId());
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .contains(((Card) slogurk.getCard()).getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(creature.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(
                permanent -> permanent.getCard().getId().equals(slogurk.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot activate without three +1/+1 counters")
    void requiresThreeCounters() {
        Permanent slogurk = harness.addToBattlefieldAndReturn(player1, new SlogurkTheOverslime());
        slogurk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
