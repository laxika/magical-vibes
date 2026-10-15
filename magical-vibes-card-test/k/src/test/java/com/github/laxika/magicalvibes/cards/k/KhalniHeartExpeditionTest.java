package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OranRiefSurvivalist;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KhalniHeartExpedition.class, Forest.class, Plains.class, OranRiefSurvivalist.class})
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
        OranRiefSurvivalist survivalist = new OranRiefSurvivalist();
        harness.setLibrary(player1, List.of(forest, plains, survivalist));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .containsExactlyInAnyOrder(forest, plains);
        assertThat(search.params().destination())
                .isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().hasType(CardType.LAND))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
        assertThat(expedition.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(expedition);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(survivalist);
    }

    @Test
    @DisplayName("The ability cannot be activated without three quest counters")
    void cannotActivateWithoutThreeQuestCounters() {
        addExpedition();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landfallCanBeDeclined() {
        Permanent expedition = addExpedition();
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(expedition.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    void opponentsLandDoesNotTriggerLandfall() {
        Permanent expedition = addExpedition();
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(expedition.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    void costsArePaidBeforeSearchResolves() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 4);
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(expedition.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(expedition);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(expedition.getCard());
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void searchCanFindZeroLands() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 3);
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void searchCanStopAfterOneLandAndLandsWaitForSelectionToFinish() {
        Permanent expedition = addExpedition();
        expedition.setCounterCount(CounterType.QUEST, 3);
        Forest forest = new Forest();
        Plains plains = new Plains();
        harness.setLibrary(player1, List.of(forest, plains));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(land -> {
                    assertThat(land.getCard()).isSameAs(forest);
                    assertThat(land.isTapped()).isTrue();
                });
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addExpedition() {
        Permanent expedition = harness.addToBattlefieldAndReturn(player1, new KhalniHeartExpedition());
        expedition.setSummoningSick(false);
        return expedition;
    }

}
