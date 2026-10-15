package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.e.EmrakulThePromisedEnd;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheHeronMoon.class, EmrakulThePromisedEnd.class, GrizzlyBears.class, TormodsCrypt.class})
@DisplayName("The Heron Moon")
class TheHeronMoonTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping adds colorless mana")
    void tappingAddsColorlessMana() {
        Permanent moon = addCreatureReady(player1, new TheHeronMoon());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(moon.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The second ability exiles the bottom card of an opponent's library")
    void exilesBottomCard() {
        addCreatureReady(player1, new TheHeronMoon());
        Card top = new GrizzlyBears();
        Card bottom = new GrizzlyBears();
        harness.setLibrary(player2, List.of(top, bottom));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
        assertThat(gd.exiledCards).extracting(entry -> entry.card()).contains(bottom);
        Permanent moon = findPermanent(player1, "The Heron Moon");
        assertThat(moon.getCounterCount(CounterType.RELEASE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Thirteen release counters sacrifice the land and cast an Emrakul copy")
    void thirteenReleaseCountersCastEmrakulCopy() {
        Permanent moon = addCreatureReady(player1, new TheHeronMoon());
        moon.setCounterCount(CounterType.RELEASE, 12);
        Card exiledCard = new GrizzlyBears();
        harness.setLibrary(player2, List.of(exiledCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(moon);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();

        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Emrakul, the Promised End")).isNotNull();
    }

    @Test
    @DisplayName("Simultaneously exiling several opponent-owned cards adds only one release counter")
    void simultaneousExileAddsOneCounter() {
        Permanent moon = addCreatureReady(player1, new TheHeronMoon());
        harness.addToBattlefield(player1, new TormodsCrypt());
        Card first = new TormodsCrypt();
        Card second = new TormodsCrypt();
        harness.setGraveyard(player2, List.of(first, second));

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(first, second);
        assertThat(moon.getCounterCount(CounterType.RELEASE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiling the controller's own cards does not add release counters")
    void ownCardsDoNotAddCounters() {
        Permanent moon = addCreatureReady(player1, new TheHeronMoon());
        harness.addToBattlefield(player1, new TormodsCrypt());
        harness.setGraveyard(player1, List.of(new TormodsCrypt()));

        harness.activateAbility(player1, 1, null, player1.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(moon.getCounterCount(CounterType.RELEASE)).isZero();
    }

    @Test
    @DisplayName("An empty opponent library produces no exile event or release counter")
    void emptyLibraryDoesNotAddCounter() {
        Permanent moon = addCreatureReady(player1, new TheHeronMoon());
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(moon.getCounterCount(CounterType.RELEASE)).isZero();
        assertThat(moon.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("The library-exile ability cannot target its controller")
    void cannotTargetController() {
        addCreatureReady(player1, new TheHeronMoon());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An exile trigger adds its counter on resolution and does not release Emrakul below thirteen")
    void counterAddedOnResolutionBelowThreshold() {
        Permanent moon = addCreatureReady(player1, new TheHeronMoon());
        moon.setCounterCount(CounterType.RELEASE, 11);
        harness.addToBattlefield(player1, new TormodsCrypt());
        harness.setGraveyard(player2, List.of(new TormodsCrypt()));

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(moon.getCounterCount(CounterType.RELEASE)).isEqualTo(11);

        resolveAllTriggers();

        assertThat(moon.getCounterCount(CounterType.RELEASE)).isEqualTo(12);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(moon);
        assertThat(countPermanents(player1, "Emrakul, the Promised End")).isZero();
    }

    @Test
    @DisplayName("Separate exile events each add a release counter")
    void separateExileEventsAddSeparateCounters() {
        Permanent moon = addCreatureReady(player1, new TheHeronMoon());
        harness.addToBattlefield(player1, new TormodsCrypt());
        harness.addToBattlefield(player1, new TormodsCrypt());
        harness.setGraveyard(player2, List.of(new TormodsCrypt()));

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();
        harness.setGraveyard(player2, List.of(new TormodsCrypt()));
        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        assertThat(moon.getCounterCount(CounterType.RELEASE)).isEqualTo(2);
    }
}
