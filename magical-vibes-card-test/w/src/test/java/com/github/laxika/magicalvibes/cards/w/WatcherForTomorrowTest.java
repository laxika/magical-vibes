package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NabanDeanOfIteration;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WatcherForTomorrow.class, GrizzlyBears.class, Shock.class, Plains.class, Unsummon.class, NabanDeanOfIteration.class})
class WatcherForTomorrowTest extends BaseCardTest {

    @Test
    @DisplayName("Hideaway exiles one of the top four cards face down and enters tapped")
    void hideawayExilesCardFaceDownAndEntersTapped() {
        WatcherForTomorrow watcher = new WatcherForTomorrow();
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        Plains plains = new Plains();
        Plains untouched = new Plains();
        harness.setLibrary(player1, List.of(bears, shock, plains, new GrizzlyBears(), untouched));
        harness.setHand(player1, List.of(watcher));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Watcher for Tomorrow");
        assertThat(permanent.isTapped()).isTrue();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card().getId()).isEqualTo(bears.getId());
            assertThat(entry.sourcePermanentId()).isEqualTo(permanent.getId());
            assertThat(entry.faceDown()).isTrue();
        });

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(untouched);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 4))
                .contains(shock, plains).doesNotContain(bears, untouched);
    }

    @Test
    @DisplayName("When Watcher for Tomorrow leaves, its exiled card returns to its owner's hand")
    void returnsExiledCardWhenItLeavesBattlefield() {
        WatcherForTomorrow watcher = new WatcherForTomorrow();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears, new Shock(), new Plains(), new GrizzlyBears()));
        harness.setHand(player1, List.of(watcher));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        Permanent permanent = findPermanent(player1, "Watcher for Tomorrow");
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, permanent.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(bears.getId());
        assertThat(gd.exiledCards).noneMatch(entry -> entry.sourcePermanentId().equals(permanent.getId()));
    }

    @Test
    @DisplayName("Hideaway with a single card exiles it without a choice")
    void hideawayWithOneCard() {
        Plains card = new Plains();
        harness.setLibrary(player1, List.of(card));
        castWatcherAndResolveHideaway();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card().getId()).isEqualTo(card.getId());
            assertThat(entry.faceDown()).isTrue();
        });
    }

    @Test
    @DisplayName("Hideaway with an empty library does nothing and the leave trigger returns nothing")
    void hideawayWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        castWatcherAndResolveHideaway();
        Permanent watcher = findPermanent(player1, "Watcher for Tomorrow");
        assertThat(watcher.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.exiledCards).isEmpty();

        bounceWatcher(watcher);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Leaving before hideaway resolves leaves the subsequently exiled card in exile")
    void leavesBeforeHideawayResolves() {
        Plains card = new Plains();
        harness.setLibrary(player1, List.of(card));
        harness.setHand(player1, List.of(new WatcherForTomorrow()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent watcher = findPermanent(player1, "Watcher for Tomorrow");

        bounceWatcher(watcher);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.exiledCards).isEmpty();
        harness.passBothPriorities();

        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card().getId()).isEqualTo(card.getId());
            assertThat(entry.sourcePermanentId()).isEqualTo(watcher.getId());
        });
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .doesNotContain(card.getId());
    }

    @Test
    @DisplayName("A doubled hideaway returns every linked card when Watcher leaves")
    void doubledHideawayReturnsBothCards() {
        harness.addToBattlefield(player1, new NabanDeanOfIteration());
        Plains first = new Plains();
        Shock second = new Shock();
        harness.setLibrary(player1, List.of(first, second));
        castWatcherAndResolveHideaway();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.passBothPriorities();
        Permanent watcher = findPermanent(player1, "Watcher for Tomorrow");
        assertThat(gd.exiledCards).filteredOn(entry -> watcher.getId().equals(entry.sourcePermanentId()))
                .hasSize(2);

        bounceWatcher(watcher);

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(first.getId(), second.getId());
        assertThat(gd.exiledCards).noneMatch(entry -> watcher.getId().equals(entry.sourcePermanentId()));
    }

    private void castWatcherAndResolveHideaway() {
        harness.setHand(player1, List.of(new WatcherForTomorrow()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void bounceWatcher(Permanent watcher) {
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, watcher.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
