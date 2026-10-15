package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({TomakulScrapsmith.class, Millstone.class, Forest.class, Shock.class})
class TomakulScrapsmithTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three and offers a milled artifact for the hand")
    void acceptsMilledArtifact() {
        Millstone millstone = new Millstone();
        setLibrary(millstone, new Forest(), new Shock());

        Permanent scrapsmith = castAndResolveEtb();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(millstone);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(scrapsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining the milled artifact puts a +1/+1 counter on Tomakul Scrapsmith")
    void declinesMilledArtifactAndGetsCounter() {
        Millstone millstone = new Millstone();
        setLibrary(millstone, new Forest(), new Shock());

        Permanent scrapsmith = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(millstone);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(scrapsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No milled artifact automatically puts a +1/+1 counter on Tomakul Scrapsmith")
    void noMilledArtifactGetsCounter() {
        setLibrary(new Forest(), new Shock(), new Forest());

        Permanent scrapsmith = castAndResolveEtb();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(scrapsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Accepting one of multiple milled artifacts returns only that card")
    void returnsOnlyOneArtifact() {
        Millstone first = new Millstone();
        Millstone second = new Millstone();
        Forest land = new Forest();
        setLibrary(first, second, land);

        Permanent scrapsmith = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(second, land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(scrapsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining the first artifact still permits returning the second")
    void choosesLaterArtifact() {
        Millstone first = new Millstone();
        Millstone second = new Millstone();
        setLibrary(first, second, new Forest());

        Permanent scrapsmith = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(scrapsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(scrapsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining all milled artifacts puts exactly one counter on the source")
    void declinesAllArtifacts() {
        Millstone first = new Millstone();
        Millstone second = new Millstone();
        Forest land = new Forest();
        setLibrary(first, second, land);

        Permanent scrapsmith = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(scrapsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second, land);
        assertThat(scrapsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Artifacts already in the graveyard or below the three milled cards are ineligible")
    void onlyCardsMilledThisWayAreEligible() {
        Millstone oldArtifact = new Millstone();
        Millstone unMilledArtifact = new Millstone();
        harness.setGraveyard(player1, List.of(oldArtifact));
        setLibrary(new Forest(), new Shock(), new Forest(), unMilledArtifact);

        Permanent scrapsmith = castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(unMilledArtifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(oldArtifact).hasSize(4);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(scrapsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A library with fewer than three cards still permits returning a milled artifact")
    void shortLibraryStillReturnsArtifact() {
        Millstone artifact = new Millstone();
        setLibrary(artifact);

        Permanent scrapsmith = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(scrapsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An empty library gives the counter without requiring a card to be milled")
    void emptyLibraryGetsCounter() {
        setLibrary();

        Permanent scrapsmith = castAndResolveEtb();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(scrapsmith.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castAndResolveEtb() {
        harness.castFromHand(player1, new TomakulScrapsmith(), "{2}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Tomakul Scrapsmith");
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
