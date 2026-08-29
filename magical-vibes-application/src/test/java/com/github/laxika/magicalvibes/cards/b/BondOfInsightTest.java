package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BondOfInsight.class, Divination.class, GrizzlyBears.class, HolyDay.class})
class BondOfInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Mills each player, returns up to two spells, and exiles itself")
    void millsReturnsSpellsAndExilesItself() {
        Card instant = new HolyDay();
        Card sorcery = new Divination();
        Card extraInstant = new HolyDay();
        BondOfInsight spell = new BondOfInsight();
        harness.setGraveyard(player1, List.of(instant, sorcery, extraInstant));
        harness.setLibrary(player1, fourCreatures());
        harness.setLibrary(player2, fourCreatures());
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(instant));
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(sorcery));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(instant, sorcery).doesNotContain(extraInstant);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(extraInstant);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId())).contains(spell.getId());
    }

    @Test
    @DisplayName("Exiles itself when no instant or sorcery cards are in the graveyard")
    void exilesItselfWithoutEligibleGraveyardCards() {
        Card creature = new GrizzlyBears();
        BondOfInsight spell = new BondOfInsight();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, fourCreatures());
        harness.setLibrary(player2, fourCreatures());
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId())).contains(spell.getId());
    }

    private List<Card> fourCreatures() {
        return List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
