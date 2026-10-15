package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.d.DawnhartRejuvenator;
import com.github.laxika.magicalvibes.cards.r.ReturnToNature;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TappingAtTheWindow.class, Forest.class, DawnhartRejuvenator.class, ReturnToNature.class})
class TappingAtTheWindowTest extends BaseCardTest {

    @Test
    void choosesOneCreatureAndPutsTheRestIntoTheGraveyard() {
        Card forest = new Forest();
        Card creature = new DawnhartRejuvenator();
        Card noncreature = new ReturnToNature();
        setLibrary(forest, creature, noncreature);

        castFromHand();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(creature);
        chooseCard(0);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, noncreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayDeclineCreatureAndPutAllThreeIntoTheGraveyard() {
        Card forest = new Forest();
        Card creature = new DawnhartRejuvenator();
        Card noncreature = new ReturnToNature();
        setLibrary(forest, creature, noncreature);

        castFromHand();
        chooseCard(-1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, creature, noncreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void flashbackExilesTheSpellAfterResolution() {
        Card forest = new Forest();
        Card creature = new DawnhartRejuvenator();
        Card noncreature = new ReturnToNature();
        TappingAtTheWindow spell = new TappingAtTheWindow();
        setLibrary(forest, creature, noncreature);
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        chooseCard(0);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, noncreature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    @Test
    void lookedAtCardsRemainPrivateUntilTheCreatureIsChosen() {
        Card forest = new Forest();
        Card creature = new DawnhartRejuvenator();
        Card noncreature = new ReturnToNature();
        setLibrary(forest, creature, noncreature);
        gd.gameLog.clear();

        castFromHand();

        assertThat(gd.gameLog).extracting(GameLogEntry::plainText)
                .noneMatch(message -> message.contains(forest.getName())
                        || message.contains(creature.getName())
                        || message.contains(noncreature.getName()));

        chooseCard(0);

        assertThat(gd.gameLog).extracting(GameLogEntry::plainText)
                .anyMatch(message -> message.contains("reveals " + creature.getName())
                        && message.contains("puts it into their hand"));
    }

    @Test
    void noCreaturePutsAllLookedAtCardsIntoGraveyardWithoutAChoice() {
        Card forest = new Forest();
        Card noncreature = new ReturnToNature();
        Card otherForest = new Forest();
        Card untouched = new DawnhartRejuvenator();
        setLibrary(forest, noncreature, otherForest, untouched);

        castFromHand();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(forest, noncreature, otherForest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void shortLibraryStillAllowsChoosingOneCreature() {
        Card first = new DawnhartRejuvenator();
        Card second = new DawnhartRejuvenator();
        setLibrary(first, second);

        castFromHand();
        chooseCard(1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void emptyLibraryResolvesWithoutAChoice() {
        setLibrary();

        castFromHand();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof TappingAtTheWindow);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void castFromHand() {
        harness.setHand(player1, List.of(new TappingAtTheWindow()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void chooseCard(int index) {
        harness.handleCardChosen(player1, index);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
