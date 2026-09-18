package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KrosanWayfarer;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
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

@CardUsed({KrosanVerge.class, Forest.class, Plains.class, Island.class, KrosanWayfarer.class})
class KrosanVergeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new KrosanVerge()));

        harness.playLand(player1, 0);

        Permanent verge = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(verge.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds one colorless mana")
    void tapAddsColorlessMana() {
        harness.addToBattlefield(player1, new KrosanVerge());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Fetches one Forest and one Plains onto the battlefield tapped")
    void fetchesForestAndPlains() {
        Forest forest = new Forest();
        Plains plains = new Plains();
        Island island = new Island();
        KrosanWayfarer wayfarer = new KrosanWayfarer();
        addVergeAndMana();
        harness.setLibrary(player1, List.of(forest, plains, island, wayfarer));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch forestSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(forestSearch.params().cards()).containsExactly(forest);
        assertThat(forestSearch.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        PendingInteraction.LibrarySearch plainsSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(plainsSearch.params().cards()).containsExactly(plains);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof Forest || permanent.getCard() instanceof Plains)
                .hasSize(2)
                .allMatch(Permanent::isTapped);
        harness.assertInGraveyard(player1, "Krosan Verge");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(island, wayfarer);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Still fetches a Plains when no Forest is available")
    void fetchesAvailableLandWhenForestIsMissing() {
        Plains plains = new Plains();
        Island island = new Island();
        addVergeAndMana();
        harness.setLibrary(player1, List.of(plains, island));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(plains);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Plains && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Still fetches a Forest when no Plains is available")
    void fetchesAvailableForestWhenPlainsIsMissing() {
        Forest forest = new Forest();
        Island island = new Island();
        KrosanWayfarer wayfarer = new KrosanWayfarer();
        addVergeAndMana();
        harness.setLibrary(player1, List.of(forest, island, wayfarer));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch forestSearch =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(forestSearch.params().cards()).containsExactly(forest);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Forest && permanent.isTapped());
        harness.assertInGraveyard(player1, "Krosan Verge");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(island, wayfarer);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addVergeAndMana() {
        harness.addToBattlefield(player1, new KrosanVerge());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
