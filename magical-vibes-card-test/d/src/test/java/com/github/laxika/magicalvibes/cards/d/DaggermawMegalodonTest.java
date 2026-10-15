package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.i.Island;
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

@CardUsed({DaggermawMegalodon.class, Island.class})
class DaggermawMegalodonTest extends BaseCardTest {

    @Test
    @DisplayName("Islandcycling discards the card and offers only Islands")
    void islandcyclingDiscardsAndSearchesForAnIsland() {
        Island island = new Island();
        harness.setHand(player1, List.of(new DaggermawMegalodon()));
        harness.setLibrary(player1, List.of(island, new DaggermawMegalodon()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Daggermaw Megalodon");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Daggermaw Megalodon");
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(island);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Island");
        assertThat(gd.playerDecks.get(player1.getId())).doesNotContain(island);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Islandcycling may fail to find even when an Island is available")
    void mayDeclineToFindIsland() {
        Island island = new Island();
        harness.setHand(player1, List.of(new DaggermawMegalodon()));
        harness.setLibrary(player1, List.of(island));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Islandcycling resolves without finding anything in an empty library")
    void emptyLibrary() {
        harness.setHand(player1, List.of(new DaggermawMegalodon()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Daggermaw Megalodon");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Islandcycling cannot be activated with less than two mana")
    void insufficientManaDoesNotDiscard() {
        harness.setHand(player1, List.of(new DaggermawMegalodon()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Daggermaw Megalodon");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Vigilance keeps Daggermaw Megalodon untapped when it attacks")
    void vigilanceKeepsAttackerUntapped() {
        Permanent megalodon = addCreatureReady(player1, new DaggermawMegalodon());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(megalodon.isAttacking()).isTrue();
        assertThat(megalodon.isTapped()).isFalse();
    }
}
