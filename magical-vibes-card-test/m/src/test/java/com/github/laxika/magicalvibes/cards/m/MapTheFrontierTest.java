package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HostileDesert;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MapTheFrontier.class, Forest.class, HostileDesert.class, EvolvingWilds.class, GrizzlyBears.class})
class MapTheFrontierTest extends BaseCardTest {

    @Test
    @DisplayName("Offers basic lands and Deserts, then puts up to two onto the battlefield tapped")
    void searchesForBasicLandsAndDeserts() {
        castMapTheFrontier();
        harness.setLibrary(player1, List.of(new Forest(), new HostileDesert(), new EvolvingWilds(), new GrizzlyBears()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(2)
                .allMatch(card -> card instanceof Forest || card instanceof HostileDesert);

        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 2);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard() instanceof Forest || p.getCard() instanceof HostileDesert)
                .allMatch(Permanent::isTapped);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Excludes nonbasic non-Desert lands")
    void excludesOtherLands() {
        castMapTheFrontier();
        harness.setLibrary(player1, List.of(new EvolvingWilds(), new GrizzlyBears()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard() instanceof EvolvingWilds);
    }

    @Test
    @DisplayName("Can find zero cards even when eligible lands exist")
    void canChooseZeroLands() {
        castMapTheFrontier();
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can stop after one land while another eligible land remains")
    void canChooseOnlyOneLand() {
        castMapTheFrontier();
        Forest chosen = new Forest();
        Forest remaining = new Forest();
        harness.setLibrary(player1, List.of(chosen, remaining));
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1)
                .anyMatch(p -> p.getCard().getId().equals(chosen.getId()) && p.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Two lands with the same name enter together, with no third pick")
    void putsTwoSameNamedLandsOntoBattlefieldTogether() {
        castMapTheFrontier();
        Forest first = new Forest();
        Forest second = new Forest();
        Forest remaining = new Forest();
        harness.setLibrary(player1, List.of(first, second, remaining));
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 2)
                .anyMatch(p -> p.getCard().getId().equals(first.getId()) && p.isTapped())
                .anyMatch(p -> p.getCard().getId().equals(second.getId()) && p.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Finishes after finding the only eligible land")
    void findsOnlyAvailableLand() {
        castMapTheFrontier();
        Forest chosen = new Forest();
        GrizzlyBears nonland = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosen, nonland));
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 1)
                .anyMatch(p -> p.getCard().getId().equals(chosen.getId()) && p.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonland);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolves with an empty library")
    void resolvesWithEmptyLibrary() {
        castMapTheFrontier();
        harness.setLibrary(player1, List.of());
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can find two Deserts without finding a basic land")
    void canChooseTwoDeserts() {
        castMapTheFrontier();
        HostileDesert first = new HostileDesert();
        HostileDesert second = new HostileDesert();
        harness.setLibrary(player1, List.of(first, second));
        int battlefieldBefore = gd.playerBattlefields.get(player1.getId()).size();

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldBefore + 2)
                .anyMatch(p -> p.getCard().getId().equals(first.getId()) && p.isTapped())
                .anyMatch(p -> p.getCard().getId().equals(second.getId()) && p.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castMapTheFrontier() {
        harness.setHand(player1, List.of(new MapTheFrontier()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
    }
}
