package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GatherThePackTest extends BaseCardTest {

    @Test
    @DisplayName("Without spell mastery only one creature card may be kept")
    void withoutSpellMasteryKeepsOne() {
        Card bears = new GrizzlyBears();
        Card giant = new HillGiant();
        Card forest = new Forest();
        Card shock = new Shock();
        Card shock2 = new Shock();
        setupTopFive(bears, giant, forest, shock, shock2);

        resolveGatherThePack();

        assertThat(offeredCards()).containsExactly("Grizzly Bears", "Hill Giant");

        chooseCard(0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(giant, forest, shock, shock2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Spell mastery lets two creature cards be kept")
    void spellMasteryKeepsTwo() {
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        Card bears = new GrizzlyBears();
        Card giant = new HillGiant();
        Card forest = new Forest();
        Card shock = new Shock();
        Card bears2 = new GrizzlyBears();
        setupTopFive(bears, giant, forest, shock, bears2);

        resolveGatherThePack();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class)
                .maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), giant.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(bears, giant);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, shock, bears2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Only creature cards are eligible — lands and instants are never offered")
    void onlyCreaturesAreEligible() {
        Card bears = new GrizzlyBears();
        setupTopFive(bears, new Forest(), new Shock(), new Forest(), new Shock());

        resolveGatherThePack();

        assertThat(offeredCards()).containsExactly("Grizzly Bears");

        chooseCard(0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(bears);
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getName))
                .contains("Forest", "Shock", "Gather the Pack");
    }

    @Test
    @DisplayName("Declining the pick still bins all five into the graveyard")
    void decliningBinsEverything() {
        setupTopFive(new GrizzlyBears(), new Shock(), new Forest(), new HillGiant(), new Shock());

        resolveGatherThePack();
        chooseCard(-1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()).stream().map(Card::getName))
                .contains("Grizzly Bears", "Shock", "Forest", "Hill Giant");
    }

    @Test
    @DisplayName("With no creature cards revealed, all five go to the graveyard")
    void noCreaturesBinsEverything() {
        setupTopFive(new Shock(), new Shock(), new Forest(), new Forest(), new Shock());

        resolveGatherThePack();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("A single instant in the graveyard is not enough for spell mastery")
    void oneInstantIsNotSpellMastery() {
        harness.setGraveyard(player1, List.of(new Shock(), new Forest()));
        setupTopFive(new GrizzlyBears(), new HillGiant(), new Shock(), new Shock(), new Shock());

        resolveGatherThePack();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    @Test
    @DisplayName("Game log records the public reveal")
    void gameLogRecordsReveal() {
        setupTopFive(new GrizzlyBears(), new Shock(), new Forest(), new Shock(), new Shock());

        resolveGatherThePack();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("reveals") && log.contains("Gather the Pack"));
    }

    private void resolveGatherThePack() {
        harness.setHand(player1, List.of(new GatherThePack()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void chooseCard(int index) {
        harness.getGameService().handleInteractionAnswer(
                harness.getGameData(), player1, new InteractionAnswer.LibraryCardChosen(index));
    }

    private List<String> offeredCards() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards()
                .stream().map(Card::getName).toList();
    }

    private void setupTopFive(Card... cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
