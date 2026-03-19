package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CloudreaderSphinxTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Cloudreader Sphinx has scry 2 ETB effect")
    void hasCorrectProperties() {
        CloudreaderSphinx card = new CloudreaderSphinx();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(ScryEffect.class);
        ScryEffect effect = (ScryEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.count()).isEqualTo(2);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Cloudreader Sphinx puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new CloudreaderSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Cloudreader Sphinx");
    }

    @Test
    @DisplayName("Resolving Cloudreader Sphinx enters battlefield and triggers ETB scry")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        harness.setHand(player1, List.of(new CloudreaderSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Cloudreader Sphinx"));

        // ETB triggered ability should be on stack
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Cloudreader Sphinx");
    }

    @Test
    @DisplayName("Resolving ETB enters scry state with 2 cards")
    void resolvingEtbEntersScryState() {
        harness.setHand(player1, List.of(new CloudreaderSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.SCRY);
        assertThat(gd.interaction.scryContext()).isNotNull();
        assertThat(gd.interaction.scryContext().cards()).hasSize(2);
    }

    // ===== Scry 2 functionality =====

    @Test
    @DisplayName("Scry 2 keeping both cards on top preserves them in order")
    void scryBothOnTop() {
        harness.setHand(player1, List.of(new CloudreaderSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        harness.getGameService().handleScryCompleted(gd, player1, List.of(0, 1), List.of());

        assertThat(deck.get(0)).isSameAs(originalTop0);
        assertThat(deck.get(1)).isSameAs(originalTop1);
    }

    @Test
    @DisplayName("Scry 2 putting both cards on bottom moves them to bottom")
    void scryBothOnBottom() {
        harness.setHand(player1, List.of(new CloudreaderSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        harness.getGameService().handleScryCompleted(gd, player1, List.of(), List.of(0, 1));

        assertThat(deck.get(0)).isNotSameAs(originalTop0);
        int deckSize = deck.size();
        assertThat(deck.get(deckSize - 2)).isSameAs(originalTop0);
        assertThat(deck.get(deckSize - 1)).isSameAs(originalTop1);
    }

    @Test
    @DisplayName("Scry 2 putting one on top and one on bottom splits correctly")
    void scrySplitTopAndBottom() {
        harness.setHand(player1, List.of(new CloudreaderSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        GameData gd = harness.getGameData();
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        // Keep card 1 on top, put card 0 on bottom
        harness.getGameService().handleScryCompleted(gd, player1, List.of(1), List.of(0));

        assertThat(deck.get(0)).isSameAs(originalTop1);
        int deckSize = deck.size();
        assertThat(deck.get(deckSize - 1)).isSameAs(originalTop0);
    }

    @Test
    @DisplayName("Completing scry clears awaiting state")
    void scryCompletionClearsState() {
        harness.setHand(player1, List.of(new CloudreaderSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        GameData gd = harness.getGameData();
        harness.getGameService().handleScryCompleted(gd, player1, List.of(0, 1), List.of());

        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.interaction.scryContext()).isNull();
    }
}
