package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({WatcherForTomorrow.class, GrizzlyBears.class, Shock.class, Plains.class, Unsummon.class})
class WatcherForTomorrowTest extends BaseCardTest {

    @Test
    @DisplayName("Hideaway exiles one of the top four cards face down and enters tapped")
    void hideawayExilesCardFaceDownAndEntersTapped() {
        WatcherForTomorrow watcher = new WatcherForTomorrow();
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        Plains plains = new Plains();
        setupTopCards(List.of(bears, shock, plains, new GrizzlyBears()));
        harness.setHand(player1, List.of(watcher));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = permanentFor(watcher);
        assertThat(permanent.isTapped()).isTrue();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.exiledCards).anySatisfy(entry -> {
            assertThat(entry.card().getId()).isEqualTo(bears.getId());
            assertThat(entry.sourcePermanentId()).isEqualTo(permanent.getId());
            assertThat(entry.faceDown()).isTrue();
        });

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(0, 1, 2)));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("When Watcher for Tomorrow leaves, its exiled card returns to its owner's hand")
    void returnsExiledCardWhenItLeavesBattlefield() {
        WatcherForTomorrow watcher = new WatcherForTomorrow();
        GrizzlyBears bears = new GrizzlyBears();
        setupTopCards(List.of(bears, new Shock(), new Plains(), new GrizzlyBears()));
        harness.setHand(player1, List.of(watcher));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.forceActivePlayer(player1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        Permanent permanent = permanentFor(watcher);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, permanent.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId).contains(bears.getId());
        assertThat(gd.exiledCards).noneMatch(entry -> entry.sourcePermanentId().equals(permanent.getId()));
    }

    private void setupTopCards(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }

    private Permanent permanentFor(Card card) {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(card.getId()))
                .findFirst()
                .orElseThrow();
    }
}
