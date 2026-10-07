package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.v.ValorInAkros;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TotemGuideHartebeest.class, Pacifism.class, GrizzlyBears.class, ValorInAkros.class})
class TotemGuideHartebeestTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability offers only Aura cards from the library")
    void acceptingEtbAbilityOffersAuras() {
        Card aura = new Pacifism();
        setupAndCast(List.of(aura, new GrizzlyBears()));

        resolveMayAbility();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(aura);
    }

    @Test
    @DisplayName("Choosing an Aura card puts it into hand")
    void choosingAuraPutsItIntoHand() {
        Card aura = new Pacifism();
        setupAndCast(List.of(aura));

        resolveMayAbility();
        harness.handleMayAbilityChosen(player1, true);
        GameData gd = harness.getGameData();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(aura);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB ability skips the library search")
    void decliningEtbAbilitySkipsSearch() {
        setupAndCast(List.of(new Pacifism()));

        resolveMayAbility();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Finding an Aura reveals it, removes it from the library, and shuffles without taking a second Aura")
    void searchRevealsAndTakesOnlyOneAura() {
        Card chosen = new Pacifism();
        Card remaining = new Pacifism();
        setupAndCast(List.of(chosen, remaining));

        resolveMayAbility();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.gameLog).anySatisfy(entry -> assertThat(entry.plainText())
                .contains("reveals Pacifism", "puts it into their hand", "Library is shuffled."));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A restricted search can fail to find even with an Aura available and still shuffles")
    void mayFailToFindAnAvailableAura() {
        Card aura = new Pacifism();
        setupAndCast(List.of(aura));

        resolveMayAbility();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(aura);
        assertThat(gd.gameLog).anySatisfy(entry -> assertThat(entry.plainText())
                .contains("chooses not to take a card. Library is shuffled."));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting a search with no Aura available finishes and shuffles")
    void searchWithNoAurasFinishes() {
        Card creature = new GrizzlyBears();
        setupAndCast(List.of(creature));

        resolveMayAbility();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.gameLog).anySatisfy(entry -> assertThat(entry.plainText())
                .contains("finds no", "Library is shuffled."));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting a search of an empty library finishes normally")
    void searchOfEmptyLibraryFinishes() {
        setupAndCast(List.of());

        resolveMayAbility();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The search excludes non-Aura enchantments and searches only its controller's library")
    void excludesNonAuraEnchantmentsAndOpponentsLibrary() {
        Card aura = new Pacifism();
        Card enchantment = new ValorInAkros();
        Card opponentsAura = new Pacifism();
        setupAndCast(List.of(enchantment, aura));
        harness.setLibrary(player2, List.of(opponentsAura));

        resolveMayAbility();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(aura);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(aura);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(enchantment);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentsAura);
    }

    @Test
    @DisplayName("Declining the ability leaves library order unchanged and does not shuffle")
    void decliningAbilityLeavesLibraryUntouched() {
        Card first = new TotemGuideHartebeest();
        Card second = new TotemGuideHartebeest();
        Card third = new TotemGuideHartebeest();
        setupAndCast(List.of(first, second, third));

        resolveMayAbility();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second, third);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog).noneSatisfy(entry -> assertThat(entry.plainText()).contains("Library is shuffled."));
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void setupAndCast(List<Card> library) {
        harness.setHand(player1, List.of(new TotemGuideHartebeest()));
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
    }

    private void resolveMayAbility() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
