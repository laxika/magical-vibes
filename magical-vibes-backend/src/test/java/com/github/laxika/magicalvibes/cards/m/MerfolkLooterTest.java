package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MerfolkLooterTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Merfolk Looter has correct card properties")
    void hasCorrectProperties() {
        MerfolkLooter card = new MerfolkLooter();

        assertThat(card.getName()).isEqualTo("Merfolk Looter");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getPower()).isEqualTo(1);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.MERFOLK, CardSubtype.ROGUE);
        assertThat(card.getCardText()).isEqualTo("{T}: Draw a card, then discard a card.");
    }

    @Test
    @DisplayName("Has one activated ability with tap and no mana cost")
    void hasCorrectAbility() {
        MerfolkLooter card = new MerfolkLooter();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        ActivatedAbility ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isNeedsTarget()).isFalse();
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(DrawCardEffect.class);
        assertThat(ability.getEffects().get(1)).isInstanceOf(DiscardCardEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Merfolk Looter puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new MerfolkLooter()));
        harness.addMana(player1, "U", 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Merfolk Looter");
    }

    @Test
    @DisplayName("Resolving puts Merfolk Looter onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new MerfolkLooter()));
        harness.addMana(player1, "U", 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Merfolk Looter"));
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new MerfolkLooter()));
        harness.addMana(player1, "U", 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new MerfolkLooter()));
        harness.addMana(player1, "U", 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Merfolk Looter"))
                .findFirst().orElseThrow();
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== Activated ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        addReadyLooter(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Merfolk Looter");
    }

    @Test
    @DisplayName("Activating ability taps Merfolk Looter")
    void activatingTapsLooter() {
        Permanent looter = addReadyLooter(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(looter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate when tapped")
    void cannotActivateWhenTapped() {
        Permanent looter = addReadyLooter(player1);
        looter.tap();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        MerfolkLooter card = new MerfolkLooter();
        Permanent looter = new Permanent(card);
        looter.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(looter);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Looting ability resolution =====

    @Test
    @DisplayName("Resolving draws a card then prompts for discard")
    void resolvingDrawsThenPromptsForDiscard() {
        addReadyLooter(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // After drawing, hand should have 2 cards (original + drawn)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        // Should be awaiting discard
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.awaitingCardChoicePlayerId).isEqualTo(player1.getId());
        assertThat(gd.gameLog).anyMatch(log -> log.contains("draws a card"));
    }

    @Test
    @DisplayName("Completing discard moves card to graveyard and hand size stays the same")
    void completingDiscardMovesToGraveyard() {
        addReadyLooter(player1);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Hand has [GrizzlyBears, Forest], discard the bears at index 0
        harness.handleCardChosen(player1, 0);

        // Hand should have 1 card (the Forest drawn)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Forest");
        // Graveyard should have the discarded card
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        // No longer awaiting input
        assertThat(gd.awaitingInput).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("discards") && log.contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("Can choose to discard the drawn card instead")
    void canDiscardTheDrawnCard() {
        addReadyLooter(player1);
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        setDeck(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Hand has [GrizzlyBears, Forest], discard the Forest at index 1
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId()).getFirst().getName()).isEqualTo("Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Looting with empty deck still prompts discard if hand has cards")
    void lootingWithEmptyDeckStillDiscardsIfHandHasCards() {
        addReadyLooter(player1);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        // Empty deck - clear it
        gd.playerDecks.get(player1.getId()).clear();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // No card drawn, hand still has 1 card
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        // Should still be awaiting discard
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards to draw"));
    }

    @Test
    @DisplayName("Looting with empty deck and empty hand skips discard")
    void lootingWithEmptyDeckAndEmptyHandSkipsDiscard() {
        addReadyLooter(player1);
        harness.setHand(player1, new ArrayList<>());
        gd.playerDecks.get(player1.getId()).clear();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // No card drawn, hand still empty - discard should be skipped
        assertThat(gd.awaitingInput).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards to discard"));
    }

    @Test
    @DisplayName("Net card count stays the same after full loot cycle")
    void netCardCountStaysSame() {
        addReadyLooter(player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new Forest()));
        setDeck(player1, List.of(new Mountain()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        // Hand size should remain the same (drew 1, discarded 1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    // ===== Combat =====

    @Test
    @DisplayName("Unblocked Merfolk Looter deals 1 damage to defending player")
    void dealsOneDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent atkPerm = new Permanent(new MerfolkLooter());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(atkPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    // ===== Helpers =====

    private Permanent addReadyLooter(Player player) {
        MerfolkLooter card = new MerfolkLooter();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
