package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DiscardOwnHandThenDrawThatManyEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShatteredPerceptionTest extends BaseCardTest {

    @Test
    @DisplayName("Has DiscardOwnHandThenDrawThatManyEffect on SPELL slot and flashback {5}{R}")
    void hasCorrectProperties() {
        ShatteredPerception card = new ShatteredPerception();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(DiscardOwnHandThenDrawThatManyEffect.class);

        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{5}{R}");
    }

    @Test
    @DisplayName("Casting discards remaining hand then draws that many cards")
    void discardsHandThenDrawsThatMany() {
        setDeck(player1, List.of(new Island(), new Island(), new Island()));
        harness.setHand(player1, List.of(
                new ShatteredPerception(),
                new GrizzlyBears(),
                new GrizzlyBears(),
                new Island()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // After casting, hand had 3 cards (spell left hand). Discard 3, draw 3.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId()))
                .allMatch(c -> c.getName().equals("Island"));
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shattered Perception"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("With empty hand after casting, discards nothing and draws nothing")
    void emptyHandDoesNothing() {
        setDeck(player1, List.of(new Island()));
        harness.setHand(player1, List.of(new ShatteredPerception()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shattered Perception"));
    }

    @Test
    @DisplayName("Cast from hand goes to graveyard after resolving")
    void normalCastGoesToGraveyard() {
        setDeck(player1, List.of(new Island(), new Island()));
        harness.setHand(player1, List.of(new ShatteredPerception(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shattered Perception"));
    }

    @Test
    @DisplayName("Flashback casts from graveyard, then the spell is exiled")
    void flashbackCastsThenExiles() {
        setDeck(player1, List.of(new Island(), new Island(), new Island()));
        harness.setGraveyard(player1, List.of(new ShatteredPerception()));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Island()));
        harness.addMana(player1, ManaColor.RED, 6);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId()))
                .allMatch(c -> c.getName().equals("Island"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Shattered Perception"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shattered Perception"));
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        setDeck(player1, List.of(new Island(), new Island(), new Island()));
        harness.setGraveyard(player1, List.of(new ShatteredPerception()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castFlashback(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
