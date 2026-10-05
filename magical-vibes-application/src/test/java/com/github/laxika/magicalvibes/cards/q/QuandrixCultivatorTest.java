package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.BreedingPool;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({QuandrixCultivator.class, Forest.class, Island.class, Plains.class, BreedingPool.class, GrizzlyBears.class})
class QuandrixCultivatorTest extends BaseCardTest {

    @Test
    @DisplayName("The ETB searches for a basic Forest or Island and puts it onto the battlefield")
    void etbSearchesForBasicForestOrIsland() {
        Forest forest = new Forest();
        Island island = new Island();
        setUpLibrary(new Plains(), new BreedingPool(), forest, island, new GrizzlyBears());
        castCultivator();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest, island);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == island && !permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB search leaves the library and battlefield unchanged")
    void mayDeclineSearch() {
        Forest forest = new Forest();
        Island island = new Island();
        setUpLibrary(forest, island);
        castCultivator();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, island);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == forest || permanent.getCard() == island);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The search may fail to find a matching card")
    void mayFailToFind() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        setUpLibrary(forest, plains);
        castCultivator();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(forest);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, plains);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A searched Forest enters untapped and leaves the library")
    void mayChooseForest() {
        Forest forest = new Forest();
        Island island = new Island();
        setUpLibrary(forest, island);
        castCultivator();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == forest && !permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting a search with no eligible lands completes without moving a card")
    void noEligibleLands() {
        Plains plains = new Plains();
        BreedingPool pool = new BreedingPool();
        setUpLibrary(plains, pool);
        castCultivator();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(plains, pool);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == plains || permanent.getCard() == pool);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting a search of an empty library completes the trigger")
    void emptyLibrary() {
        setUpLibrary();
        castCultivator();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castCultivator() {
        harness.castFromHand(player1, new QuandrixCultivator(), "{1}{G}{G}{U}");
    }

    private void setUpLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
