package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Khalni Heart Expedition")
class KhalniHeartExpeditionTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall offers a quest counter")
    void landfallOffersQuestCounter() {
        Permanent expedition = addExpedition();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(expedition.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removing three quest counters and sacrificing searches for two tapped basic lands")
    void removesCountersSacrificesAndSearchesForBasicLands() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 3);
        Forest forest = new Forest();
        Plains plains = new Plains();
        GrizzlyBears bears = new GrizzlyBears();
        setLibrary(forest, plains, bears);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .containsExactlyInAnyOrder(forest, plains);
        assertThat(search.params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
        assertThat(expedition.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(expedition);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
    }

    @Test
    @DisplayName("The ability cannot be activated without three quest counters")
    void cannotActivateWithoutThreeQuestCounters() {
        addExpedition();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addExpedition() {
        Permanent expedition = harness.addToBattlefieldAndReturn(player1, new KhalniHeartExpedition());
        expedition.setSummoningSick(false);
        return expedition;
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
