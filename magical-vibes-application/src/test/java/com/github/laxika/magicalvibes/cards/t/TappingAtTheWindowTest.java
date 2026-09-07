package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TappingAtTheWindow.class, Forest.class, GrizzlyBears.class, Shock.class})
class TappingAtTheWindowTest extends BaseCardTest {

    @Test
    void choosesOneCreatureAndPutsTheRestIntoTheGraveyard() {
        Card forest = new Forest();
        Card creature = new GrizzlyBears();
        Card shock = new Shock();
        setLibrary(forest, creature, shock);

        castFromHand();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(creature);
        chooseCard(0);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayDeclineCreatureAndPutAllThreeIntoTheGraveyard() {
        Card forest = new Forest();
        Card creature = new GrizzlyBears();
        Card shock = new Shock();
        setLibrary(forest, creature, shock);

        castFromHand();
        chooseCard(-1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, creature, shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void flashbackExilesTheSpellAfterResolution() {
        Card forest = new Forest();
        Card creature = new GrizzlyBears();
        Card shock = new Shock();
        TappingAtTheWindow spell = new TappingAtTheWindow();
        setLibrary(forest, creature, shock);
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        chooseCard(0);

        assertThat(gd.playerHands.get(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(forest, shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
    }

    private void castFromHand() {
        harness.setHand(player1, List.of(new TappingAtTheWindow()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void chooseCard(int index) {
        harness.getGameService().handleInteractionAnswer(
                harness.getGameData(), player1, new InteractionAnswer.LibraryCardChosen(index));
    }

    private void setLibrary(Card... cards) {
        GameData gameData = harness.getGameData();
        gameData.playerDecks.get(player1.getId()).clear();
        gameData.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
