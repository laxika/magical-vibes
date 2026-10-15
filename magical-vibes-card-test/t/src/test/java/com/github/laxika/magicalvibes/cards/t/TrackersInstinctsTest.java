package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.cards.f.FaithlessLooting;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrackersInstincts.class, DawntreaderElk.class, FaithlessLooting.class, EvolvingWilds.class})
class TrackersInstinctsTest extends BaseCardTest {

    

    @Test
    @DisplayName("Resolving enters library reveal choice when multiple creatures are revealed")
    void resolvingEntersRevealChoiceState() {
        setupTopCards(List.of(new DawntreaderElk(), new DawntreaderElk(), new FaithlessLooting(), new EvolvingWilds()));

        harness.setHand(player1, List.of(new TrackersInstincts()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
    }

    @Test
    @DisplayName("Choosing a creature puts it in hand and the rest into graveyard")
    void choosingCreaturePutsOneInHandRestInGraveyard() {
        Card bears0 = new DawntreaderElk();
        Card bears1 = new DawntreaderElk();
        Card shock = new FaithlessLooting();
        Card forest = new EvolvingWilds();
        setupTopCards(List.of(bears0, bears1, shock, forest));

        harness.setHand(player1, List.of(new TrackersInstincts()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.handleMultipleCardsChosen(player1, List.of(bears1.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(bears1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears0, shock, forest);
    }

    @Test
    @DisplayName("When only one creature is revealed, it goes to hand automatically")
    void singleCreatureAutoToHand() {
        Card bears = new DawntreaderElk();
        Card shock = new FaithlessLooting();
        Card forest = new EvolvingWilds();
        setupTopCards(List.of(shock, forest, bears, new FaithlessLooting()));

        harness.setHand(player1, List.of(new TrackersInstincts()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock, forest);
    }

    @Test
    @DisplayName("When no creatures are revealed, all cards go to graveyard")
    void noCreaturesAllToGraveyard() {
        Card shock0 = new FaithlessLooting();
        Card shock1 = new FaithlessLooting();
        Card forest = new EvolvingWilds();
        setupTopCards(List.of(shock0, shock1, forest, new FaithlessLooting()));

        harness.setHand(player1, List.of(new TrackersInstincts()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock0, shock1, forest);
    }

    @Test
    @DisplayName("Spell goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        setupTopCards(List.of(new DawntreaderElk(), new FaithlessLooting(), new EvolvingWilds(), new FaithlessLooting()));

        harness.setHand(player1, List.of(new TrackersInstincts()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.assertInGraveyard(player1, "Tracker's Instincts");
    }

    @Test
    @DisplayName("Flashback from graveyard works correctly")
    void flashbackFromGraveyard() {
        Card bears = new DawntreaderElk();
        setupTopCards(List.of(bears, new FaithlessLooting(), new EvolvingWilds(), new FaithlessLooting()));

        harness.setGraveyard(player1, List.of(new TrackersInstincts()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        harness.assertNotInGraveyard(player1, "Tracker's Instincts");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Tracker's Instincts"));
    }

    @Test
    @DisplayName("Game log records reveal")
    void gameLogRecordsReveal() {
        setupTopCards(List.of(new DawntreaderElk(), new FaithlessLooting(), new EvolvingWilds(), new FaithlessLooting()));

        harness.setHand(player1, List.of(new TrackersInstincts()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("reveals") && log.contains("Tracker's Instincts"));
    }

    @Test
    @DisplayName("Choosing no creature is rejected when creatures are revealed")
    void mustChooseCreatureWhenAvailable() {
        Card first = new DawntreaderElk();
        Card second = new DawntreaderElk();
        setupTopCards(List.of(first, second, new FaithlessLooting(), new EvolvingWilds()));
        harness.setHand(player1, List.of(new TrackersInstincts()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second);
    }

    @Test
    @DisplayName("A short library reveals all available cards")
    void shortLibraryResolvesWithAvailableCards() {
        Card creature = new DawntreaderElk();
        Card land = new EvolvingWilds();
        setupTopCards(List.of(land, creature));
        harness.setHand(player1, List.of(new TrackersInstincts()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library still allows flashback to resolve and exile the spell")
    void emptyLibraryFlashbackResolves() {
        setupTopCards(List.of());
        Card spell = new TrackersInstincts();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveFlashback(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void setupTopCards(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
