package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.s.SandskitterOutrider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TempestHawk.class, SandskitterOutrider.class})
class TempestHawkTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage lets its controller search for a Tempest Hawk")
    void combatDamageSearchesForTempestHawk() {
        harness.setLibrary(player1, List.of(new SandskitterOutrider(), new TempestHawk()));

        attackUnblocked();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).singleElement().extracting(Card::getName).isEqualTo("Tempest Hawk");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Tempest Hawk");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName).containsExactly("Sandskitter Outrider");
    }

    @Test
    @DisplayName("The ability does not trigger when Tempest Hawk deals no combat damage to a player")
    void blockedHawkDoesNotTrigger() {
        Permanent attacker = addCreatureReady(player1, new TempestHawk());
        addCreatureReady(player2, new TempestHawk());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ability leaves the library unchanged")
    void canDeclineSearch() {
        TempestHawk first = new TempestHawk();
        SandskitterOutrider second = new SandskitterOutrider();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of());

        attackUnblocked();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A restricted search can fail to find even when a Hawk is available")
    void canFailToFind() {
        TempestHawk hawk = new TempestHawk();
        harness.setLibrary(player1, List.of(hawk));
        harness.setHand(player1, List.of());

        attackUnblocked();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(hawk);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching without a matching card finishes without putting a card into hand")
    void searchWithoutMatchingCard() {
        SandskitterOutrider other = new SandskitterOutrider();
        harness.setLibrary(player1, List.of(other));
        harness.setHand(player1, List.of());

        attackUnblocked();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching an empty library finishes normally")
    void searchEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of());

        attackUnblocked();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("One combat damage trigger finds only one Hawk from multiple copies")
    void searchesForOnlyOneCopy() {
        TempestHawk first = new TempestHawk();
        TempestHawk second = new TempestHawk();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of());

        attackUnblocked();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
    private void attackUnblocked() {
        Permanent attacker = addCreatureReady(player1, new TempestHawk());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
    }
}
