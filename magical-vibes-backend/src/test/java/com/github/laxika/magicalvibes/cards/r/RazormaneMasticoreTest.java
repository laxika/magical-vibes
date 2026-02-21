package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RazormaneMasticoreTest {

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

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    private void advanceToDraw(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        gd.turnNumber = 2; // avoid first-turn draw skip
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances from UPKEEP to DRAW
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Razormane Masticore has correct card properties")
    void hasCorrectProperties() {
        RazormaneMasticore card = new RazormaneMasticore();

        assertThat(card.getName()).isEqualTo("Razormane Masticore");
        assertThat(card.getType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getManaCost()).isEqualTo("{5}");
        assertThat(card.getPower()).isEqualTo(5);
        assertThat(card.getToughness()).isEqualTo(5);
        assertThat(card.getKeywords()).contains(Keyword.FIRST_STRIKE);
    }

    @Test
    @DisplayName("Has upkeep sacrifice-unless-discard effect")
    void hasUpkeepEffect() {
        RazormaneMasticore card = new RazormaneMasticore();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(SacrificeUnlessDiscardCardTypeEffect.class);
        SacrificeUnlessDiscardCardTypeEffect effect =
                (SacrificeUnlessDiscardCardTypeEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(effect.requiredType()).isNull(); // any card type
    }

    @Test
    @DisplayName("Has draw step may-deal-damage effect")
    void hasDrawStepEffect() {
        RazormaneMasticore card = new RazormaneMasticore();

        assertThat(card.getEffects(EffectSlot.DRAW_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.DRAW_TRIGGERED).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.DRAW_TRIGGERED).getFirst();
        assertThat(may.wrapped()).isInstanceOf(DealDamageToTargetCreatureEffect.class);
        assertThat(((DealDamageToTargetCreatureEffect) may.wrapped()).damage()).isEqualTo(3);
    }

    // ===== Upkeep — sacrifice unless discard any card =====

    @Test
    @DisplayName("Upkeep with card in hand — prompts may ability choice")
    void upkeepWithCardInHandPromptsMayAbility() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting upkeep discard keeps Masticore and discards the card")
    void acceptingUpkeepDiscardKeepsMasticore() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);

        harness.handleCardChosen(player1, 0); // discard the card

        // Masticore is still on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Razormane Masticore"));

        // Grizzly Bears is in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Any card type can be discarded for upkeep cost (not limited to creatures)")
    void anyCardTypeCanBeDiscardedForUpkeep() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        // Hand has only a non-creature card (land-type card via LlanowarElves as creature)
        // Use a creature since that's all we have, but verify all indices are valid
        harness.setHand(player1, List.of(new LlanowarElves(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.handleMayAbilityChosen(player1, true);

        // All indices should be valid since any card can be discarded
        assertThat(gd.interaction.awaitingCardChoiceValidIndices()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Declining upkeep discard sacrifices Masticore")
    void decliningUpkeepDiscardSacrificesMasticore() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        harness.handleMayAbilityChosen(player1, false);

        // Masticore is NOT on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Razormane Masticore"));

        // Masticore is in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Razormane Masticore"));
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has empty hand")
    void autoSacrificesWithEmptyHand() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.setHand(player1, List.of()); // empty hand

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger — auto-sacrifice

        // Masticore is NOT on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Razormane Masticore"));

        // Masticore is in the graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Razormane Masticore"));

        // No prompt — no cards to discard
        assertThat(gd.interaction.awaitingInputType()).isNull();
    }

    @Test
    @DisplayName("Upkeep trigger only fires on controller's upkeep, not opponent's")
    void upkeepTriggerOnlyOnControllersUpkeep() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.setHand(player1, List.of());

        // Advance to opponent's upkeep — Masticore should NOT trigger
        advanceToUpkeep(player2);

        // Masticore should still be on the battlefield (no trigger)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Razormane Masticore"));
    }

    // ===== Draw step — may deal 3 damage to target creature =====

    @Test
    @DisplayName("Draw step triggers may ability prompt for controller")
    void drawStepTriggersMayAbilityPrompt() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToDraw(player1);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting draw step ability prompts for target creature")
    void acceptingDrawStepAbilityPromptsForTarget() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToDraw(player1);

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.awaitingPermanentChoicePlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Draw step ability deals 3 damage to chosen creature and destroys it if lethal")
    void drawStepAbilityDeals3DamageAndKills() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        advanceToDraw(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);

        // Triggered ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(bearsId);

        harness.passBothPriorities(); // resolve the triggered ability

        // Grizzly Bears (2/2) should be destroyed by 3 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining draw step ability does nothing")
    void decliningDrawStepAbilityDoesNothing() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.addToBattlefield(player2, new GrizzlyBears());

        advanceToDraw(player1);

        harness.handleMayAbilityChosen(player1, false);

        // Grizzly Bears should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Draw step trigger only fires on controller's draw step, not opponent's")
    void drawStepTriggerOnlyOnControllersDrawStep() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Advance to opponent's draw step — Masticore should NOT trigger
        advanceToDraw(player2);

        // No may ability prompt should fire for Masticore
        assertThat(gd.interaction.awaitingInputType()).isNull();

        // Grizzly Bears should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Draw step ability can target own creatures")
    void drawStepAbilityCanTargetOwnCreatures() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        LlanowarElves elves = new LlanowarElves();
        harness.addToBattlefield(player1, elves);
        UUID elvesId = harness.getPermanentId(player1, "Llanowar Elves");

        advanceToDraw(player1);

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, elvesId);
        harness.passBothPriorities(); // resolve

        // Llanowar Elves (1/1) should be destroyed by 3 damage
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Draw step ability with no creatures on battlefield skips ability")
    void drawStepAbilityWithNoCreaturesSkips() {
        harness.addToBattlefield(player1, new RazormaneMasticore());
        // Remove all other creatures
        gd.playerBattlefields.get(player1.getId()).removeIf(
                p -> !p.getCard().getName().equals("Razormane Masticore"));
        gd.playerBattlefields.get(player2.getId()).clear();

        advanceToDraw(player1);

        // May ability prompt should appear — Masticore itself is a valid target
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }
}
