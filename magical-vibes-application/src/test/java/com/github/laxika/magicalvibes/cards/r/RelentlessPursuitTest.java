package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RelentlessPursuit.class, Forest.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class RelentlessPursuitTest extends BaseCardTest {

    @Test
    @DisplayName("First pick offers only creature cards among the top four")
    void firstPickOffersCreatures() {
        setupTopFour(new GrizzlyBears(), new Shock(), new Forest(), new HillGiant());

        resolvePursuit();

        assertThat(searchCards()).containsExactlyInAnyOrder("Grizzly Bears", "Hill Giant");
    }

    @Test
    @DisplayName("Revealing a creature and a land puts both into hand and the rest into the graveyard")
    void takesCreatureAndLandRestToGraveyard() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        Card forest = new Forest();
        Card giant = new HillGiant();
        setupTopFour(bears, shock, forest, giant);

        resolvePursuit();

        chooseCard(0);
        assertThat(searchCards()).containsExactly("Forest");
        chooseCard(0);

        GameData data = harness.getGameData();
        assertThat(data.playerHands.get(player1.getId())).contains(bears, forest);
        assertThat(data.playerGraveyards.get(player1.getId())).contains(shock, giant);
        assertThat(data.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The second pick is land-only after taking a creature")
    void cannotTakeTwoCreatures() {
        setupTopFour(new GrizzlyBears(), new HillGiant(), new Forest(), new Shock());

        resolvePursuit();
        chooseCard(0);

        assertThat(searchCards()).containsExactly("Forest");
    }

    @Test
    @DisplayName("Declining both picks puts all revealed cards into the graveyard")
    void decliningBothBinsEverything() {
        setupTopFour(new GrizzlyBears(), new Forest(), new Shock(), new HillGiant());

        resolvePursuit();
        chooseCard(-1);
        chooseCard(-1);

        GameData data = harness.getGameData();
        assertThat(data.playerHands.get(player1.getId())).isEmpty();
        assertThat(data.playerGraveyards.get(player1.getId()).stream().map(Card::getName))
                .contains("Grizzly Bears", "Forest", "Shock", "Hill Giant");
        assertThat(data.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The top four cards are publicly revealed")
    void recordsPublicReveal() {
        setupTopFour(new GrizzlyBears(), new Forest(), new Shock(), new Shock());

        resolvePursuit();

        assertThat(harness.getGameData().gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals") && log.contains("Relentless Pursuit"));
    }

    @Test
    void canTakeOnlyLandAfterDecliningCreature() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        setupTopFour(bears, forest);

        resolvePursuit();
        chooseCard(-1);
        chooseCard(0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void canTakeOnlyCreatureAfterDecliningLand() {
        Card bears = new GrizzlyBears();
        Card forest = new Forest();
        setupTopFour(bears, forest);

        resolvePursuit();
        chooseCard(0);
        chooseCard(-1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void offersLandDirectlyWhenNoCreatureIsRevealed() {
        Card forest = new Forest();
        Card shock = new Shock();
        setupTopFour(shock, forest);

        resolvePursuit();
        assertThat(searchCards()).containsExactly("Forest");
        chooseCard(0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void finishesAfterCreaturePickWhenNoLandIsRevealed() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        setupTopFour(bears, shock);

        resolvePursuit();
        chooseCard(0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void millsIneligibleCardsWithoutOfferingAChoice() {
        Card first = new Shock();
        Card second = new Shock();
        setupTopFour(first, second);

        resolvePursuit();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void leavesCardsBelowTopFourInTheirOriginalOrder() {
        Card fifth = new HillGiant();
        Card sixth = new Forest();
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock(), fifth, sixth));

        resolvePursuit();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth, sixth);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof Shock).hasSize(4);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());

        resolvePursuit();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof RelentlessPursuit);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void resolvePursuit() {
        harness.setHand(player1, List.of(new RelentlessPursuit()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void chooseCard(int index) {
        harness.handleCardChosen(player1, index);
    }

    private List<String> searchCards() {
        return harness.getGameData().interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().stream().map(Card::getName).toList();
    }

    private void setupTopFour(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
