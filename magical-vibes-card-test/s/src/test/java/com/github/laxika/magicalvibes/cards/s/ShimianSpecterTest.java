package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenAugur;
import com.github.laxika.magicalvibes.cards.a.AugurOfSkulls;
import com.github.laxika.magicalvibes.cards.d.DryadArbor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShimianSpecter.class, AugurOfSkulls.class, StreetWraith.class, DryadArbor.class, AvenAugur.class})
class ShimianSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage prompts the controller to pick a nonland card from the revealed hand")
    void combatDamagePromptsChoice() {
        addAttackingSpecter(player1);
        harness.setHand(player2, List.of(new AugurOfSkulls(), new StreetWraith(), new DryadArbor()));

        resolveCombatAndTrigger();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        // Lands are excluded and only cards actually in the revealed hand are choosable.
        assertThat(choice.options()).containsExactlyInAnyOrder("Augur of Skulls", "Street Wraith");
    }

    @Test
    @DisplayName("Exiles every copy of the chosen card from hand, graveyard, and library")
    void exilesAllCopiesFromAllZones() {
        addAttackingSpecter(player1);
        harness.setHand(player2, List.of(new AugurOfSkulls(), new AugurOfSkulls(), new StreetWraith()));
        harness.setGraveyard(player2, List.of(new AugurOfSkulls()));
        harness.setLibrary(player2, List.of(new AugurOfSkulls()));

        resolveCombatAndTrigger();
        harness.handleListChoice(player1, "Augur of Skulls");

        long exiled = gd.getPlayerExiledCards(player2.getId()).stream()
                .filter(c -> c.getName().equals("Augur of Skulls")).count();
        assertThat(exiled).isEqualTo(4);
        harness.assertNotInHand(player2, "Augur of Skulls");
        harness.assertNotInGraveyard(player2, "Augur of Skulls");
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(c -> c.getName().equals("Augur of Skulls"));

        // Cards with a different name are untouched.
        harness.assertInHand(player2, "Street Wraith");
    }

    @Test
    @DisplayName("Deals no damage beyond combat damage — only the exile happens")
    void dealsNoExtraDamage() {
        addAttackingSpecter(player1);
        harness.setHand(player2, List.of(new AugurOfSkulls(), new AugurOfSkulls()));

        int lifeAfterCombat;
        resolveCombatAndTrigger();
        lifeAfterCombat = gd.playerLifeTotals.get(player2.getId());
        harness.handleListChoice(player1, "Augur of Skulls");

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeAfterCombat);
    }

    @Test
    @DisplayName("No nonland choice when the revealed hand holds only lands")
    void noPromptWhenHandIsAllLands() {
        addAttackingSpecter(player1);
        harness.setHand(player2, List.of(new DryadArbor()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
        harness.assertInHand(player2, "Dryad Arbor");
    }

    @Test
    @DisplayName("No nonland choice when the damaged player's hand is empty")
    void noPromptWhenHandEmpty() {
        addAttackingSpecter(player1);
        harness.setHand(player2, List.of());

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNull();
    }

    @Test
    @DisplayName("The whole hand is revealed before the controller chooses a nonland card")
    void revealsWholeHandBeforeChoice() {
        addAttackingSpecter(player1);
        harness.setHand(player2, List.of(new AugurOfSkulls(), new DryadArbor()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals their hand")
                && entry.plainText().contains("Augur of Skulls")
                && entry.plainText().contains("Dryad Arbor"));
    }

    @Test
    @DisplayName("A hand containing only lands is still revealed")
    void revealsAllLandHand() {
        addAttackingSpecter(player1);
        harness.setHand(player2, List.of(new DryadArbor()));

        resolveCombatAndTrigger();

        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals their hand")
                && entry.plainText().contains("Dryad Arbor"));
    }

    @Test
    @DisplayName("Choosing a name still allows the controller to choose which hidden-zone copies to find")
    void offersSearchChoiceInsteadOfForcingAllHiddenCopiesIntoExile() {
        addAttackingSpecter(player1);
        harness.setHand(player2, List.of(new AugurOfSkulls(), new AugurOfSkulls()));
        harness.setGraveyard(player2, List.of(new AugurOfSkulls()));
        harness.setLibrary(player2, List.of(new AugurOfSkulls(), new StreetWraith()));

        resolveCombatAndTrigger();
        harness.handleListChoice(player1, "Augur of Skulls");

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("The controller can inspect the library even when there is no nonland card to choose")
    void offersLibrarySearchWithAllLandHand() {
        addAttackingSpecter(player1);
        harness.setHand(player2, List.of(new DryadArbor()));
        harness.setLibrary(player2, List.of(new AugurOfSkulls(), new StreetWraith()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.assertInHand(player2, "Dryad Arbor");
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("No trigger occurs when the Specter deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        addAttackingSpecter(player1);
        addCreatureReady(player2, new AvenAugur());
        int lifeBeforeCombat = gd.playerLifeTotals.get(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBeforeCombat);
    }

    private void addAttackingSpecter(Player player) {
        Permanent specter = addCreatureReady(player, new ShimianSpecter());
        specter.setAttacking(true);
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
