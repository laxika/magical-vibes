package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RavenousGigamole.class, Forest.class, GrizzlyBears.class, Shock.class})
class RavenousGigamoleTest extends BaseCardTest {

    @Test
    @DisplayName("ETB mills three and returns a milled creature to hand")
    void acceptsMilledCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(bears, new Forest(), new Shock());

        Permanent gigamole = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Shock");
        assertThat(gigamole.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining a milled creature puts a +1/+1 counter on Ravenous Gigamole")
    void declinesMilledCreatureAndGetsCounter() {
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(bears, new Forest(), new Shock());

        Permanent gigamole = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gigamole.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No milled creature automatically puts a +1/+1 counter on Ravenous Gigamole")
    void noMilledCreatureGetsCounter() {
        setLibrary(new Forest(), new Shock(), new Forest());

        Permanent gigamole = castAndResolveEtb();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gigamole.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Only one of multiple milled creatures can be returned")
    void returnsOnlyOneCreature() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        Forest remaining = new Forest();
        setLibrary(first, second, new Shock(), remaining);

        Permanent gigamole = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second).doesNotContain(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gigamole.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A later milled creature can be chosen after declining the first")
    void canChooseLaterCreature() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        setLibrary(first, second, new Forest());

        Permanent gigamole = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);
        assertThat(gigamole.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first).doesNotContain(second);
        assertThat(gigamole.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Declining every milled creature adds exactly one counter")
    void decliningAllCreaturesAddsOneCounter() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        setLibrary(first, second, new Forest());

        Permanent gigamole = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, false);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second).hasSize(3);
        assertThat(gigamole.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An empty library still gives a counter without returning an older graveyard creature")
    void emptyLibraryDoesNotReturnOlderCreature() {
        GrizzlyBears older = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(older));
        setLibrary();

        Permanent gigamole = castAndResolveEtb();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(older);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gigamole.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature can be returned when fewer than three cards remain")
    void shortLibraryCanReturnCreature() {
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(bears);

        Permanent gigamole = castAndResolveEtb();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gigamole.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent castAndResolveEtb() {
        harness.castFromHand(player1, new RavenousGigamole(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        return findPermanent(player1, "Ravenous Gigamole");
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
