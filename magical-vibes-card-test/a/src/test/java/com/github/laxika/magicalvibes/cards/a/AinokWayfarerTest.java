package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AinokWayfarer.class, Forest.class, Plains.class})
class AinokWayfarerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three and offers a milled land for the hand")
    void acceptsMilledLand() {
        Plains plains = new Plains();
        setLibrary(plains, new Forest(), new AinokWayfarer());

        Permanent wayfarer = castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(plains);
        assertThat(wayfarer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining a milled land puts a +1/+1 counter on Ainok Wayfarer")
    void declinesMilledLandAndGetsCounter() {
        Plains plains = new Plains();
        setLibrary(plains, new Forest(), new AinokWayfarer());

        Permanent wayfarer = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(wayfarer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No milled land automatically puts a +1/+1 counter on Ainok Wayfarer")
    void noMilledLandGetsCounter() {
        setLibrary(new AinokWayfarer(), new AinokWayfarer(), new AinokWayfarer());

        Permanent wayfarer = castAndResolveEtb();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(wayfarer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("A later milled land can be chosen without getting a counter")
    void choosesSecondMilledLand() {
        Plains plains = new Plains();
        Forest forest = new Forest();
        AinokWayfarer nonland = new AinokWayfarer();
        setLibrary(plains, forest, nonland);

        Permanent wayfarer = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(plains, nonland);
        assertThat(wayfarer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Lands already in the graveyard are not eligible")
    void cannotReturnPreviouslyMilledLand() {
        Plains oldLand = new Plains();
        harness.setGraveyard(player1, List.of(oldLand));
        setLibrary(new AinokWayfarer(), new AinokWayfarer(), new AinokWayfarer());

        Permanent wayfarer = castAndResolveEtb();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4).contains(oldLand);
        assertThat(wayfarer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("A land can be returned when fewer than three cards remain")
    void returnsLandFromShortLibrary() {
        Forest forest = new Forest();
        setLibrary(forest);

        Permanent wayfarer = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(wayfarer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An empty library still results in a counter")
    void emptyLibraryGetsCounter() {
        setLibrary();

        Permanent wayfarer = castAndResolveEtb();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(wayfarer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent castAndResolveEtb() {
        harness.castFromHand(player1, new AinokWayfarer(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Ainok Wayfarer");
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
