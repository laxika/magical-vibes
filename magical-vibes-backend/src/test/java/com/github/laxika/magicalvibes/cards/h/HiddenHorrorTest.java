package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HiddenHorrorTest {

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
    @DisplayName("Hidden Horror has correct card properties")
    void hasCorrectProperties() {
        HiddenHorror card = new HiddenHorror();

        assertThat(card.getName()).isEqualTo("Hidden Horror");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getPower()).isEqualTo(4);
        assertThat(card.getToughness()).isEqualTo(4);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.HORROR);
    }

    @Test
    @DisplayName("Has ETB sacrifice-unless-discard effect")
    void hasEtbEffect() {
        HiddenHorror card = new HiddenHorror();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(SacrificeUnlessDiscardCardTypeEffect.class);
        SacrificeUnlessDiscardCardTypeEffect effect =
                (SacrificeUnlessDiscardCardTypeEffect) card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst();
        assertThat(effect.requiredType()).isEqualTo(CardType.CREATURE);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Hidden Horror puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new HiddenHorror()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Hidden Horror");
    }

    @Test
    @DisplayName("Resolving puts Hidden Horror on battlefield with ETB trigger on stack")
    void resolvingPutsOnBattlefieldWithEtbOnStack() {
        harness.setHand(player1, List.of(new HiddenHorror()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Hidden Horror is on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hidden Horror"));

        // ETB triggered ability is on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Hidden Horror");
    }

    // ===== ETB with creature card in hand — accept discard =====

    @Test
    @DisplayName("ETB resolves with creature in hand — prompts may ability choice")
    void etbWithCreatureInHandPromptsMayAbility() {
        harness.setHand(player1, List.of(new HiddenHorror()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        // Give controller a creature in hand for the ETB
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → may ability prompt

        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may ability prompts for creature card discard choice")
    void acceptingMayAbilityPromptsDiscard() {
        castHiddenHorrorWithCreatureInHand();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.awaitingCardChoicePlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Discarding creature card keeps Hidden Horror on the battlefield")
    void discardingCreatureKeepsHiddenHorror() {
        castHiddenHorrorWithCreatureInHand();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0); // discard the creature

        // Hidden Horror is still on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Hidden Horror"));

        // Grizzly Bears is in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Hand is empty
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== ETB with creature card in hand — decline discard =====

    @Test
    @DisplayName("Declining may ability sacrifices Hidden Horror")
    void decliningMayAbilitySacrificesHiddenHorror() {
        castHiddenHorrorWithCreatureInHand();

        harness.handleMayAbilityChosen(player1, false);

        // Hidden Horror is NOT on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hidden Horror"));

        // Hidden Horror is in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hidden Horror"));

        // Grizzly Bears is still in hand (was not discarded)
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== ETB with no creature card in hand — auto-sacrifice =====

    @Test
    @DisplayName("Auto-sacrifices when controller has no creature cards in hand")
    void autoSacrificesWithNoCreatureInHand() {
        harness.setHand(player1, List.of(new HiddenHorror()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        // Give controller a hand with only non-creature cards
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → auto-sacrifice

        // Hidden Horror is NOT on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hidden Horror"));

        // Hidden Horror is in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hidden Horror"));

        // No may ability prompt — it was automatic
        assertThat(gd.interaction.awaitingInput).isNull();

        // Hand is untouched
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);

        // Log confirms the sacrifice
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no creature card to discard")
                && log.contains("Hidden Horror") && log.contains("sacrificed"));
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has empty hand")
    void autoSacrificesWithEmptyHand() {
        harness.setHand(player1, List.of(new HiddenHorror()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.setHand(player1, List.of()); // empty hand
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB

        // Auto-sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Hidden Horror"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Hidden Horror"));
    }

    // ===== Filtered discard — only creature cards =====

    @Test
    @DisplayName("Discard choice only shows creature card indices when hand has mixed types")
    void discardChoiceOnlyShowsCreatureIndices() {
        harness.setHand(player1, List.of(new HiddenHorror()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        // Hand: [Forest, GrizzlyBears, Forest, LlanowarElves]
        harness.setHand(player1, List.of(new Forest(), new GrizzlyBears(), new Forest(), new LlanowarElves()));
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB → may ability

        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);

        harness.handleMayAbilityChosen(player1, true);

        // Only creature indices should be valid (index 1 = GrizzlyBears, index 3 = LlanowarElves)
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.awaitingCardChoiceValidIndices).containsExactlyInAnyOrder(1, 3);
    }

    // ===== Hidden Horror leaves battlefield before ETB resolves =====

    @Test
    @DisplayName("Player may still discard when Hidden Horror left battlefield (per ruling 2008-04-01)")
    void mayStillDiscardWhenHiddenHorrorLeftBattlefield() {
        harness.setHand(player1, List.of(new HiddenHorror()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities(); // resolve creature spell → ETB on stack

        // Manually remove Hidden Horror from the battlefield (simulating it being destroyed)
        Permanent horror = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hidden Horror"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(horror);

        harness.passBothPriorities(); // resolve ETB → may ability prompt (creature gone but still offered)

        // Player is still offered the choice to discard
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting discard when Hidden Horror already gone discards creature card")
    void acceptingDiscardWhenHiddenHorrorGone() {
        harness.setHand(player1, List.of(new HiddenHorror()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities(); // resolve creature spell

        // Remove Hidden Horror
        Permanent horror = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hidden Horror"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(horror);

        harness.passBothPriorities(); // resolve ETB
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.DISCARD_CHOICE);

        harness.handleCardChosen(player1, 0); // discard Grizzly Bears

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining discard when Hidden Horror already gone does nothing")
    void decliningDiscardWhenHiddenHorrorGone() {
        harness.setHand(player1, List.of(new HiddenHorror()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities(); // resolve creature spell

        // Remove Hidden Horror
        Permanent horror = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hidden Horror"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(horror);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve ETB
        harness.handleMayAbilityChosen(player1, false);

        // Hand untouched — player declined, no sacrifice needed
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Does nothing when Hidden Horror left and no creature cards in hand")
    void doesNothingWhenHiddenHorrorLeftAndNoCreatures() {
        harness.setHand(player1, List.of(new HiddenHorror()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.setHand(player1, List.of(new Forest()));
        harness.passBothPriorities(); // resolve creature spell

        // Remove Hidden Horror
        Permanent horror = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Hidden Horror"))
                .findFirst().orElseThrow();
        gd.playerBattlefields.get(player1.getId()).remove(horror);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities(); // resolve ETB → no creature cards, permanent gone, nothing

        // No prompt — nothing to sacrifice and nothing valid to discard
        assertThat(gd.interaction.awaitingInput).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    // ===== Helpers =====

    /**
     * Casts Hidden Horror with a creature (Grizzly Bears) in hand, resolves through
     * to the may ability prompt.
     */
    private void castHiddenHorrorWithCreatureInHand() {
        harness.setHand(player1, List.of(new HiddenHorror()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB → may ability prompt

        // Sanity check
        assertThat(gd.interaction.awaitingInput).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId).isEqualTo(player1.getId());
    }
}

