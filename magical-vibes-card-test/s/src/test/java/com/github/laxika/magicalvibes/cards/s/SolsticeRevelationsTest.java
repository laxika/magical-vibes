package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AppaSteadfastGuardian;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SolsticeRevelations.class, Mountain.class, GrizzlyBears.class, AppaSteadfastGuardian.class})
class SolsticeRevelationsTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles until a nonland card and offers it when its mana value is below the Mountain count")
    void offersEligibleCardForFreeCast() {
        addMountains(3);
        GrizzlyBears hit = new GrizzlyBears();
        Mountain revealedLand = new Mountain();
        castSolstice(List.of(revealedLand, hit));

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(hit);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(revealedLand, hit);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() == hit
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(revealedLand);
    }

    @Test
    @DisplayName("Puts the nonland card into hand when its mana value is not below the Mountain count")
    void putsIneligibleCardIntoHand() {
        addMountains(2);
        GrizzlyBears hit = new GrizzlyBears();
        Mountain revealedLand = new Mountain();
        castSolstice(List.of(revealedLand, hit));

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(revealedLand);
    }

    @Test
    @DisplayName("Declining an eligible free cast puts the nonland card into hand")
    void declinePutsCardIntoHand() {
        addMountains(3);
        GrizzlyBears hit = new GrizzlyBears();
        Mountain revealedLand = new Mountain();
        castSolstice(List.of(revealedLand, hit));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.stack).noneMatch(entry -> entry.getCard() == hit);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(revealedLand);
    }

    @Test
    @DisplayName("Exiles the whole library when no nonland card is found")
    void exilesAllCardsWhenNoNonlandCardIsFound() {
        addMountains(1);
        Mountain first = new Mountain();
        Mountain second = new Mountain();
        castSolstice(List.of(first, second));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(first, second);
    }

    @Test
    @DisplayName("Flashback resolves the exile effect and exiles Solstice Revelations")
    void flashbackResolvesAndExilesSpell() {
        SolsticeRevelations spell = new SolsticeRevelations();
        SolsticeRevelations hit = new SolsticeRevelations();
        Mountain land = new Mountain();
        harness.setGraveyard(player1, List.of(spell));
        harness.setLibrary(player1, List.of(land, hit));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(hit);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(land, spell);
        harness.assertNotInGraveyard(player1, "Solstice Revelations");
    }

    @Test
    @DisplayName("Casting the exiled card triggers Appa's exile-cast ability")
    void freeCastTriggersExileCastAbilities() {
        harness.addToBattlefield(player1, new AppaSteadfastGuardian());
        addMountains(4);
        SolsticeRevelations hit = new SolsticeRevelations();
        castSolstice(List.of(hit));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ally")).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Solstice Revelations");
    }

    @Test
    @DisplayName("Stops at the first nonland card and leaves the remaining library in order")
    void preservesLibraryAfterFirstNonland() {
        Mountain land = new Mountain();
        SolsticeRevelations hit = new SolsticeRevelations();
        Mountain remainingLand = new Mountain();
        SolsticeRevelations remainingSpell = new SolsticeRevelations();

        castSolstice(List.of(land, hit, remainingLand, remainingSpell));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingLand, remainingSpell);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(hit);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Opponent's Mountains do not qualify the exiled card for a free cast")
    void ignoresOpponentsMountains() {
        addMountains(2);
        for (int i = 0; i < 4; i++) {
            harness.addToBattlefield(player2, new Mountain());
        }
        SolsticeRevelations hit = new SolsticeRevelations();

        castSolstice(List.of(hit));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(hit);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("An empty library resolves without creating a choice")
    void resolvesWithEmptyLibrary() {
        castSolstice(List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Solstice Revelations");
    }

    private void castSolstice(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new SolsticeRevelations(), "{2}{R}");
        harness.passBothPriorities();
    }

    private void addMountains(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Mountain());
        }
    }
}
