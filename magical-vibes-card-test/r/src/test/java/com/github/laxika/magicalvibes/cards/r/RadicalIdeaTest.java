package com.github.laxika.magicalvibes.cards.r;

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

@CardUsed({RadicalIdea.class, Forest.class, Plains.class, SinisterSabotage.class})
class RadicalIdeaTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when cast from hand")
    void drawsACard() {
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new RadicalIdea()));
        addMana();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        harness.assertInGraveyard(player1, "Radical Idea");
    }

    @Test
    @DisplayName("Jump-start discards a card, draws a card, and exiles Radical Idea")
    void jumpStartDiscardsDrawsAndExiles() {
        RadicalIdea spell = new RadicalIdea();
        Plains discarded = new Plains();
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        addMana();

        harness.castJumpStart(player1, 0, 0);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(discarded.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(spell.getId()));
    }

    @Test
    @DisplayName("Jump-start requires a card in hand to discard")
    void jumpStartRequiresDiscard() {
        harness.setGraveyard(player1, List.of(new RadicalIdea()));
        harness.setHand(player1, List.of());
        addMana();

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Jump-start pays its discard before the spell resolves")
    void discardsBeforeResolution() {
        RadicalIdea spell = new RadicalIdea();
        RadicalIdea discarded = new RadicalIdea();
        RadicalIdea drawn = new RadicalIdea();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        addMana();

        harness.castJumpStart(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
    }

    @Test
    @DisplayName("Jump-start still requires the normal mana cost")
    void jumpStartRequiresMana() {
        RadicalIdea spell = new RadicalIdea();
        RadicalIdea discarded = new RadicalIdea();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castJumpStart(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A countered jump-start spell is exiled and does not draw")
    void counteredJumpStartIsExiled() {
        RadicalIdea spell = new RadicalIdea();
        RadicalIdea discarded = new RadicalIdea();
        RadicalIdea topCard = new RadicalIdea();
        harness.setGraveyard(player1, List.of(spell));
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player2, List.of(new SinisterSabotage()));
        harness.setLibrary(player2, List.of(new RadicalIdea()));
        addMana();
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castJumpStart(player1, 0, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, spell.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(spell);
        assertThat(gd.stack).isEmpty();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
