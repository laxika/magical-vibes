package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Consider;
import com.github.laxika.magicalvibes.cards.d.DawnhartRejuvenator;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SlogurkTheOverslime.class, Mountain.class, DawnhartRejuvenator.class, Consider.class})
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
        Card creature = new DawnhartRejuvenator();
        harness.setGraveyard(player1, List.of(first, second, third, creature));

        Permanent slogurk = harness.addToBattlefieldAndReturn(player1, new SlogurkTheOverslime());
        slogurk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(com.github.laxika.magicalvibes.model.PendingInteraction.MultiGraveyardChoice.class);
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

    @Test
    void gainsCounterForLandFromLibrary() {
        Permanent slogurk = harness.addToBattlefieldAndReturn(player1, new SlogurkTheOverslime());
        Card land = new Mountain();
        Card drawn = new Mountain();
        harness.setLibrary(player1, List.of(land, drawn));
        harness.setHand(player1, List.of(new Consider()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(slogurk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    void doesNotGainCounterForOpponentsLand() {
        Permanent slogurk = harness.addToBattlefieldAndReturn(player1, new SlogurkTheOverslime());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, land));
        resolveAllTriggers();

        assertThat(slogurk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void paysCountersImmediatelyAndCanReturnWithoutAnyLands() {
        Permanent slogurk = harness.addToBattlefieldAndReturn(player1, new SlogurkTheOverslime());
        slogurk.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.setGraveyard(player1, List.of());

        harness.activateAbility(player1, 0, null, null);

        assertThat(slogurk.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(slogurk);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(slogurk.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(slogurk);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void mayChooseZeroLandsWhenItDies() {
        Card land = new Mountain();
        harness.setGraveyard(player1, List.of(land));
        Permanent slogurk = harness.addToBattlefieldAndReturn(player1, new SlogurkTheOverslime());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, slogurk));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land, slogurk.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
    }

    @Test
    void mayReturnOnlyOneLandWhenExiled() {
        Card first = new Mountain();
        Card second = new Mountain();
        Card opposingLand = new Mountain();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setGraveyard(player2, List.of(opposingLand));
        Permanent slogurk = harness.addToBattlefieldAndReturn(player1, new SlogurkTheOverslime());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToExile(gd, slogurk));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(first).doesNotContain(second, opposingLand);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingLand);
    }
}
