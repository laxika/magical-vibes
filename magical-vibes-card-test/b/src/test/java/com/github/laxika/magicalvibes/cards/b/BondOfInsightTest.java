package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.CallousDismissal;
import com.github.laxika.magicalvibes.cards.a.ArborealGrazer;
import com.github.laxika.magicalvibes.cards.n.NoEscape;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BondOfInsight.class, CallousDismissal.class, ArborealGrazer.class, NoEscape.class})
class BondOfInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Mills each player, returns up to two spells, and exiles itself")
    void millsReturnsSpellsAndExilesItself() {
        Card instant = new NoEscape();
        Card sorcery = new CallousDismissal();
        Card extraInstant = new NoEscape();
        BondOfInsight spell = new BondOfInsight();
        harness.setGraveyard(player1, List.of(instant, sorcery, extraInstant));
        harness.setLibrary(player1, fourCreatures());
        harness.setLibrary(player2, fourCreatures());
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

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
        Card creature = new ArborealGrazer();
        BondOfInsight spell = new BondOfInsight();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, fourCreatures());
        harness.setLibrary(player2, fourCreatures());
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId())).contains(spell.getId());
    }

    @Test
    @DisplayName("May decline all returns even with eligible cards")
    void mayReturnZeroCards() {
        Card instant = new NoEscape();
        Card sorcery = new CallousDismissal();
        BondOfInsight spell = new BondOfInsight();
        harness.setGraveyard(player1, List.of(instant, sorcery));
        harness.setLibrary(player1, fourCreatures());
        harness.setLibrary(player2, fourCreatures());
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleGraveyardCardChosen(player1, -1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(instant, sorcery).doesNotContain(spell);
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId())).contains(spell.getId());
    }

    @Test
    @DisplayName("May return just one newly milled spell and leave the opponent's spells alone")
    void mayReturnOneNewlyMilledCard() {
        Card instant = new NoEscape();
        Card sorcery = new CallousDismissal();
        Card opposingSpell = new NoEscape();
        BondOfInsight spell = new BondOfInsight();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.setLibrary(player1, List.of(instant, sorcery));
        harness.setLibrary(player2, List.of(opposingSpell));
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(instant));
        harness.handleGraveyardCardChosen(player1, -1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingSpell);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId())).contains(spell.getId());
    }

    @Test
    @DisplayName("Can return two sorceries including a different Bond of Insight")
    void returnsTwoSorceriesFromGraveyard() {
        Card otherBond = new BondOfInsight();
        Card sorcery = new CallousDismissal();
        BondOfInsight spell = new BondOfInsight();
        harness.setGraveyard(player1, List.of(otherBond, sorcery));
        harness.setLibrary(player1, fourCreatures());
        harness.setLibrary(player2, fourCreatures());
        harness.setHand(player1, List.of(spell));
        addMana();

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(otherBond));
        harness.handleGraveyardCardChosen(player1, gd.playerGraveyards.get(player1.getId()).indexOf(sorcery));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(otherBond, sorcery);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(otherBond, sorcery, spell);
        assertThat(gd.exiledCards.stream().map(entry -> entry.card().getId())).contains(spell.getId()).doesNotContain(otherBond.getId());
    }

    private List<Card> fourCreatures() {
        return List.of(new ArborealGrazer(), new ArborealGrazer(), new ArborealGrazer(), new ArborealGrazer());
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
