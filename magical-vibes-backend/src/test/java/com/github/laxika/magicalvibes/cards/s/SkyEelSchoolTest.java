package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkyEelSchoolTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Sky-Eel School has ETB draw and discard effects")
    void hasEtbDrawAndDiscardEffects() {
        SkyEelSchool card = new SkyEelSchool();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(0)).isInstanceOf(DrawCardEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).get(1)).isInstanceOf(DiscardCardEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Sky-Eel School puts creature spell on stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SkyEelSchool()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Sky-Eel School");
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingPutsEtbOnStack() {
        harness.setHand(player1, List.of(new SkyEelSchool()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sky-Eel School");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("ETB draws a card then prompts for discard")
    void etbDrawsThenPromptsForDiscard() {
        harness.setHand(player1, List.of(new SkyEelSchool()));
        setDeck(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Drew one card (Forest), now awaiting discard choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.awaitingCardChoicePlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Completing discard results in net zero cards in hand (loot)")
    void completingDiscardResultsInLoot() {
        harness.setHand(player1, List.of(new SkyEelSchool()));
        setDeck(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Hand has 1 card (drew GrizzlyBears), discard it
        harness.handleCardChosen(player1, 0);

        // Hand should be empty (cast Sky-Eel School from hand, drew 1, discarded 1)
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Player can choose which card to discard")
    void canChooseWhichCardToDiscard() {
        harness.setHand(player1, List.of(new SkyEelSchool(), new Forest()));
        setDeck(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger

        // Hand has [Forest, GrizzlyBears] - discard Forest (index 0), keep Grizzly Bears
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).get(0).getName()).isEqualTo("Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Discarded card goes to graveyard")
    void discardedCardGoesToGraveyard() {
        harness.setHand(player1, List.of(new SkyEelSchool()));
        setDeck(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB trigger
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    // ===== Helpers =====

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
