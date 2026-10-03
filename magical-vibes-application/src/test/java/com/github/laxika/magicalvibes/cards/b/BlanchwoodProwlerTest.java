package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.cards.e.EpicConfrontation;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlanchwoodProwler.class, Forest.class, ArgothianSprite.class, EpicConfrontation.class})
class BlanchwoodProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three cards and offers a milled land for the hand")
    void acceptsMilledLand() {
        Forest forest = new Forest();
        setLibrary(forest, new EpicConfrontation(), new ArgothianSprite());

        Permanent prowler = castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(forest);
        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Declining the milled land puts a +1/+1 counter on Blanchwood Prowler")
    void declinesMilledLandAndGetsCounter() {
        Forest forest = new Forest();
        setLibrary(forest, new EpicConfrontation(), new ArgothianSprite());

        Permanent prowler = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No milled land automatically puts a +1/+1 counter on Blanchwood Prowler")
    void noMilledLandGetsCounter() {
        setLibrary(new EpicConfrontation(), new ArgothianSprite(), new EpicConfrontation());

        Permanent prowler = castAndResolveEtb();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void canDeclineFirstLandAndTakeSecondWithoutCounter() {
        Forest first = new Forest();
        Forest second = new Forest();
        EpicConfrontation nonland = new EpicConfrontation();
        setLibrary(first, nonland, second);

        Permanent prowler = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(first, nonland);
        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void acceptingOneLandEndsAllOtherOffers() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        setLibrary(first, second, third);

        Permanent prowler = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(second, third);
        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void decliningAllLandsAddsOnlyOneCounter() {
        setLibrary(new Forest(), new Forest(), new Forest());

        Permanent prowler = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);
        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void shortLibraryStillAllowsReturningMilledLand() {
        Forest forest = new Forest();
        setLibrary(forest);

        Permanent prowler = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void emptyLibraryStillAddsCounter() {
        setLibrary();

        Permanent prowler = castAndResolveEtb();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotReturnLandThatWasAlreadyInGraveyardOrBelowMilledCards() {
        Forest oldLand = new Forest();
        Forest unMilledLand = new Forest();
        harness.setGraveyard(player1, List.of(oldLand));
        setLibrary(new ArgothianSprite(), new EpicConfrontation(), new ArgothianSprite(), unMilledLand);

        Permanent prowler = castAndResolveEtb();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unMilledLand);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oldLand).hasSize(4);
        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent castAndResolveEtb() {
        harness.castFromHand(player1, new BlanchwoodProwler(), "{1}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Blanchwood Prowler");
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
