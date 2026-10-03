package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SinisterSabotage;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChemistersInsight.class, Forest.class, Plains.class, SinisterSabotage.class})
class ChemistersInsightTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards when cast from hand")
    void drawsTwoCards() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new ChemistersInsight()));
        addMana();

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Chemister's Insight");
    }

    @Test
    @DisplayName("Jump-start discards any card, draws two, and exiles the spell")
    void jumpStartDiscardsDrawsAndExiles() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setGraveyard(player1, List.of(new ChemistersInsight()));
        harness.setHand(player1, List.of(new Plains()));
        addMana();

        harness.castJumpStart(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Plains");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Chemister's Insight"));
    }

    @Test
    @DisplayName("Jump-start requires a card in hand to discard")
    void jumpStartRequiresDiscard() {
        harness.setGraveyard(player1, List.of(new ChemistersInsight()));
        harness.setHand(player1, List.of());
        addMana();

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Jump-start can discard a nonland card and pays the discard before resolution")
    void jumpStartDiscardsNonlandAsCost() {
        ChemistersInsight spell = new ChemistersInsight();
        ChemistersInsight discarded = new ChemistersInsight();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(first, second));
        addMana();

        harness.castJumpStart(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    @DisplayName("Jump-start still requires the spell's mana cost")
    void jumpStartRequiresMana() {
        ChemistersInsight spell = new ChemistersInsight();
        Plains discard = new Plains();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discard);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(spell);
    }

    @Test
    @DisplayName("Countering a jump-start spell exiles it without drawing or refunding the discard")
    void counteredJumpStartIsExiled() {
        ChemistersInsight spell = new ChemistersInsight();
        Plains discard = new Plains();
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discard));
        harness.setLibrary(player1, List.of(first, second));
        addMana();
        harness.setHand(player2, List.of(new SinisterSabotage()));
        harness.setLibrary(player2, List.of(new Forest()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castJumpStart(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(spell);
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
