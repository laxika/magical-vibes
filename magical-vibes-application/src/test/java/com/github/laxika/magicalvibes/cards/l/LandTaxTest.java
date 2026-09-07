package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MishrasFactory;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LandTax.class, Forest.class, GrizzlyBears.class, MishrasFactory.class, Plains.class})
class LandTaxTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep with an opponent controlling more lands prompts the may ability")
    void upkeepPromptsMayAbility() {
        setupLandTax();
        givePlayerLands(player2, 1);
        setupLibrary();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting offers only basic land cards for the search")
    void acceptingOffersOnlyBasicLands() {
        setupLandTax();
        givePlayerLands(player2, 1);
        setupLibrary();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .isNotEmpty()
                .allMatch(c -> c instanceof Plains);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().remainingCount())
                .isEqualTo(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().reveals()).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().canFailToFind()).isTrue();
    }

    @Test
    @DisplayName("Chosen basic lands go to hand")
    void chosenBasicLandsGoToHand() {
        setupLandTax();
        givePlayerLands(player2, 1);
        setupLibrary();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card instanceof Plains);
    }

    @Test
    @DisplayName("Choosing three basic lands puts all three into hand")
    void choosingThreeBasicLandsPutsThemIntoHand() {
        setupLandTax();
        givePlayerLands(player2, 1);
        setupLibrary();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2)
                .allMatch(card -> card instanceof GrizzlyBears || card instanceof MishrasFactory);
    }

    @Test
    @DisplayName("Choosing fewer than three basic lands is allowed")
    void choosingFewerThanThreeBasicLandsIsAllowed() {
        setupLandTax();
        givePlayerLands(player2, 1);
        setupLibrary();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4)
                .filteredOn(card -> card instanceof Plains).hasSize(2);
    }

    @Test
    @DisplayName("Declining does not search")
    void decliningDoesNotSearch() {
        setupLandTax();
        givePlayerLands(player2, 1);
        setupLibrary();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("No trigger when you control at least as many lands")
    void noTriggerWhenNotFewerLands() {
        setupLandTax();
        givePlayerLands(player1, 1);
        givePlayerLands(player2, 1);
        setupLibrary();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("No trigger when you control more lands")
    void noTriggerWhenYouControlMoreLands() {
        setupLandTax();
        givePlayerLands(player1, 2);
        givePlayerLands(player2, 1);
        setupLibrary();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Land Tax triggers only during its controller's upkeep")
    void triggersOnlyDuringItsControllersUpkeep() {
        setupLandTax();
        givePlayerLands(player2, 1);
        setupLibrary();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("No search when the library has no basic land cards")
    void noSearchWhenLibraryHasNoBasicLands() {
        setupLandTax();
        givePlayerLands(player2, 1);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new MishrasFactory()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2)
                .allMatch(card -> card instanceof GrizzlyBears || card instanceof MishrasFactory);
    }

    private void setupLandTax() {
        harness.addToBattlefield(player1, new LandTax());
    }

    private void givePlayerLands(Player player, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player, new Forest());
        }
    }

    private void setupLibrary() {
        Plains first = new Plains();
        Plains second = new Plains();
        Plains third = new Plains();
        harness.setLibrary(player1, List.of(first, second, third, new GrizzlyBears(), new MishrasFactory()));
    }
}
