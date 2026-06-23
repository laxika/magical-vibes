package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FaithlessLootingTest extends BaseCardTest {

    @Test
    @DisplayName("Has DrawAndDiscardCardEffect(2, 2) on SPELL slot and flashback {2}{R}")
    void hasCorrectProperties() {
        FaithlessLooting card = new FaithlessLooting();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(DrawAndDiscardCardEffect.class);
        DrawAndDiscardCardEffect effect =
                (DrawAndDiscardCardEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.drawAmount()).isEqualTo(2);
        assertThat(effect.discardAmount()).isEqualTo(2);

        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{2}{R}");
    }

    @Test
    @DisplayName("Casting draws two cards then discards two cards")
    void drawsTwoThenDiscardsTwo() {
        setDeck(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new FaithlessLooting(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        // Hand after casting (spell leaves hand): 2 cards. Draw 2 -> 4 cards.
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // After drawing two, the effect awaits two discard choices.
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        // Net: had 2 (after cast), +2 draw, -2 discard = 2 cards.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        // Two discarded cards plus the resolved Faithless Looting spell itself.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Faithless Looting"));
    }

    @Test
    @DisplayName("Cast from hand goes to graveyard after resolving")
    void normalCastGoesToGraveyard() {
        setDeck(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new FaithlessLooting()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Drew two cards (deck had 2), now discard the two non-Looting cards.
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Faithless Looting"));
    }

    @Test
    @DisplayName("Flashback casts from graveyard, then the spell is exiled")
    void flashbackCastsThenExiles() {
        setDeck(player1, List.of(new Island(), new Island()));
        harness.setGraveyard(player1, List.of(new FaithlessLooting()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        // Draw two, then discard two.
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        GameData gd = harness.getGameData();
        // Flashback spell is exiled, not returned to graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Faithless Looting"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Faithless Looting"));
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        setDeck(player1, List.of(new Island(), new Island()));
        harness.setGraveyard(player1, List.of(new FaithlessLooting()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
