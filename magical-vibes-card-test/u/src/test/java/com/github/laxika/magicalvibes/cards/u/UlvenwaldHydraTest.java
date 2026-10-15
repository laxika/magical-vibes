package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BywayCourier;
import com.github.laxika.magicalvibes.cards.h.HighlandLake;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UlvenwaldHydra.class, Forest.class, Plains.class, BywayCourier.class, HighlandLake.class})
class UlvenwaldHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal lands controlled by its controller")
    void powerAndToughnessEqualControlledLands() {
        Permanent hydra = addHydraReady(player1);
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Forest());

        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(2);
    }

    @Test
    @DisplayName("Entering the battlefield creates a may prompt to search for a land")
    void enteringTheBattlefieldCreatesMayPrompt() {
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting the may ability puts a chosen basic land onto the battlefield tapped")
    void acceptingMayPutsBasicLandOntoBattlefieldTapped() {
        setupLibraryWithBasicLand();
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .hasSize(2)
                .allMatch(card -> card.hasType(CardType.LAND));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Plains && permanent.isTapped());
        Permanent hydra = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof UlvenwaldHydra).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the may ability skips the library search")
    void decliningMaySkipsSearch() {
        setupLibraryWithBasicLand();
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("The search can find a nonbasic land and excludes nonland cards")
    void searchesForNonbasicLand() {
        HighlandLake land = new HighlandLake();
        harness.setLibrary(player1, List.of(land, new BywayCourier()));
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(land);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == land && permanent.isTapped());
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(land);
    }

    @Test
    @DisplayName("Searching a library without lands finishes without putting a card onto the battlefield")
    void searchWithoutLandsFinishes() {
        BywayCourier creature = new BywayCourier();
        harness.setLibrary(player1, List.of(creature));
        setupAndCast();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Power and toughness update when controlled lands enter and leave")
    void powerAndToughnessTrackLandChanges() {
        harness.addToBattlefield(player1, new Forest());
        Permanent hydra = addHydraReady(player1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new HighlandLake());

        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId()).remove(land);

        assertThat(gqs.getEffectivePower(gd, hydra)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, hydra)).isEqualTo(1);
    }

    private Permanent addHydraReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new UlvenwaldHydra());
    }

    private void setupAndCast() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new UlvenwaldHydra()));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.castCreature(player1, 0);
    }

    private void setupLibraryWithBasicLand() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new BywayCourier()));
    }
}
