package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AjaniTheGreathearted;
import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.c.CharmedStray;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.p.PastInFlames;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IgniteTheBeacon.class, AjaniTheGreathearted.class, GideonBlackblade.class, CharmedStray.class})
class IgniteTheBeaconTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for up to two planeswalker cards and puts the chosen cards into hand")
    void searchesForUpToTwoPlaneswalkers() {
        AjaniTheGreathearted ajani = new AjaniTheGreathearted();
        GideonBlackblade gideon = new GideonBlackblade();
        CharmedStray bears = new CharmedStray();
        harness.setLibrary(player1, List.of(ajani, bears, gideon));
        cast();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrder(ajani, gideon);
        assertThat(search.params().reveals()).isTrue();

        int ajaniIndex = search.params().cards().indexOf(ajani);
        assertThat(ajaniIndex).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, ajaniIndex);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(ajani, gideon);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bears);
        harness.assertInGraveyard(player1, "Ignite the Beacon");
    }

    @Test
    @DisplayName("Allows finding fewer than two planeswalker cards")
    void mayFindOnlyOnePlaneswalker() {
        AjaniTheGreathearted ajani = new AjaniTheGreathearted();
        GideonBlackblade gideon = new GideonBlackblade();
        harness.setLibrary(player1, List.of(ajani, gideon));
        cast();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        int ajaniIndex = search.params().cards().indexOf(ajani);
        assertThat(ajaniIndex).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, ajaniIndex);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).contains(ajani).doesNotContain(gideon);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(gideon);
    }

    @Test
    void mayFindZeroDespiteAvailablePlaneswalkers() {
        AjaniTheGreathearted ajani = new AjaniTheGreathearted();
        GideonBlackblade gideon = new GideonBlackblade();
        harness.setLibrary(player1, List.of(ajani, gideon));
        cast();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(ajani, gideon);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Ignite the Beacon");
    }

    @Test
    void resolvesWithoutMatchingCards() {
        CharmedStray stray = new CharmedStray();
        harness.setLibrary(player1, List.of(stray));
        cast();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(stray);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Ignite the Beacon");
    }

    @Test
    void resolvesWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        cast();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Ignite the Beacon");
    }

    @Test
    void finishesAutomaticallyWhenOnlyOnePlaneswalkerExists() {
        AjaniTheGreathearted ajani = new AjaniTheGreathearted();
        CharmedStray stray = new CharmedStray();
        harness.setLibrary(player1, List.of(ajani, stray));
        cast();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(ajani);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(stray);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Ignite the Beacon");
    }

    @Test
    void mayFindTwoCopiesWithTheSameName() {
        AjaniTheGreathearted first = new AjaniTheGreathearted();
        AjaniTheGreathearted second = new AjaniTheGreathearted();
        harness.setLibrary(player1, List.of(first, second));
        cast();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({PastInFlames.class})
    void stillFindsTwoPlaneswalkersWhenCastWithGrantedFlashback() {
        AjaniTheGreathearted ajani = new AjaniTheGreathearted();
        GideonBlackblade gideon = new GideonBlackblade();
        harness.setLibrary(player1, List.of(ajani, gideon));
        harness.setGraveyard(player1, List.of(new IgniteTheBeacon()));
        harness.setHand(player1, List.of(new PastInFlames()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(ajani, gideon);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card instanceof IgniteTheBeacon);
    }

    private void cast() {
        harness.setHand(player1, List.of(new IgniteTheBeacon()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castAndResolveInstant(player1, 0);
    }
}
