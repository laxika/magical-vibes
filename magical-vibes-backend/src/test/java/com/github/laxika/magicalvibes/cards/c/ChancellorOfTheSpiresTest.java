package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMillsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChancellorOfTheSpiresTest {

    protected GameTestHarness harness;
    protected Player player1;
    protected Player player2;
    protected GameService gs;
    protected GameQueryService gqs;
    protected GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gqs = harness.getGameQueryService();
        gd = harness.getGameData();
        // Do NOT call skipMulligan() here — opening hand tests need to set hand first
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Chancellor has ON_OPENING_HAND_REVEAL MayEffect wrapping EachOpponentMillsEffect(7)")
    void hasOpeningHandMillEffect() {
        ChancellorOfTheSpires card = new ChancellorOfTheSpires();

        assertThat(card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_OPENING_HAND_REVEAL).getFirst();
        assertThat(may.wrapped()).isInstanceOf(EachOpponentMillsEffect.class);
        EachOpponentMillsEffect effect = (EachOpponentMillsEffect) may.wrapped();
        assertThat(effect.count()).isEqualTo(7);
    }

    @Test
    @DisplayName("Chancellor has ON_ENTER_BATTLEFIELD CastTargetInstantOrSorceryFromGraveyardEffect")
    void hasETBCastFromGraveyardEffect() {
        ChancellorOfTheSpires card = new ChancellorOfTheSpires();

        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst())
                .isInstanceOf(CastTargetInstantOrSorceryFromGraveyardEffect.class);
    }

    // ===== Opening hand trigger: mill =====

    @Test
    @DisplayName("Chancellor in opening hand prompts may ability at first upkeep")
    void openingHandTriggerPromptsMayAbility() {
        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.skipMulligan();

        // CR 603.5: MayEffect goes on the stack, resolve it to get the may prompt
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
    }

    @Test
    @DisplayName("CR 603.5: MayEffect triggered ability goes on the stack at first upkeep")
    void mayEffectGoesOnStackAtFirstUpkeep() {
        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.skipMulligan();

        // CR 603.5: MayEffect goes on the stack immediately (not as a pending may ability)
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Chancellor of the Spires");
    }

    @Test
    @DisplayName("Resolving Chancellor opening hand trigger mills opponent 7 cards")
    void openingHandTriggerMillsOpponent() {
        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.skipMulligan();

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        // CR 603.5: MayEffect goes on the stack, resolve it to get the may prompt
        harness.passBothPriorities();

        // Accept — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        int deckSizeAfter = gd.playerDecks.get(player2.getId()).size();
        assertThat(deckSizeBefore - deckSizeAfter).isEqualTo(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSizeGreaterThanOrEqualTo(7);
    }

    @Test
    @DisplayName("Declining Chancellor reveal does not mill opponent")
    void decliningRevealDoesNotMill() {
        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.skipMulligan();

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        // CR 603.5: MayEffect goes on the stack, resolve it to get the may prompt
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        int deckSizeAfter = gd.playerDecks.get(player2.getId()).size();
        assertThat(deckSizeBefore).isEqualTo(deckSizeAfter);
    }

    @Test
    @DisplayName("Chancellor stays in hand after opening hand trigger")
    void chancellorRemainsInHandAfterTrigger() {
        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.skipMulligan();

        // CR 603.5: MayEffect goes on the stack, resolve it to get the may prompt
        harness.passBothPriorities();

        // Accept — inner effect resolves inline
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Chancellor of the Spires"));
    }

    // ===== ETB: cast from opponent's graveyard =====

    @Test
    @DisplayName("ETB with instant/sorcery in opponent's graveyard prompts graveyard choice")
    void etbPromptsGraveyardChoice() {
        harness.skipMulligan();

        // Put a Shock in opponent's graveyard
        harness.setGraveyard(player2, List.of(new Shock()));

        // Cast Chancellor
        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB → graveyard choice

        // Should be prompting for graveyard choice
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("ETB only shows instant/sorcery cards from opponent's graveyard")
    void etbOnlyShowsInstantSorceryFromOpponent() {
        harness.skipMulligan();

        // Put a creature and an instant in opponent's graveyard
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(shock, bears));

        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Only Shock should be selectable (instant), not Grizzly Bears (creature)
        Set<UUID> validIds = gd.interaction.multiSelection().multiGraveyardValidCardIds();
        assertThat(validIds).hasSize(1);
        assertThat(validIds).contains(shock.getId());
    }

    @Test
    @DisplayName("ETB casts non-targeted sorcery from opponent's graveyard without paying mana cost")
    void etbCastsNonTargetedSpellFromGraveyard() {
        harness.skipMulligan();

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player2, List.of(counsel));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → ETB → graveyard choice

        // Select the Counsel of the Soratami from graveyard
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities(); // resolve ETB trigger → queues may-cast

        // Accept the may-cast
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the cast Counsel of the Soratami

        // Player1 should have drawn 2 cards from Counsel of the Soratami
        // handSizeBefore was after Chancellor left hand (cast), so hand is empty
        // Then drew 2 cards from Counsel = 2
        assertThat(gd.playerHands.get(player1.getId()).size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("ETB casts targeted instant from opponent's graveyard and prompts for target")
    void etbCastsTargetedSpellFromGraveyard() {
        harness.skipMulligan();

        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));

        // Add a creature for Shock to target
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → ETB → graveyard choice

        // Select Shock from opponent's graveyard
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities(); // resolve ETB trigger → queues may-cast

        // Accept the may-cast
        harness.handleMayAbilityChosen(player1, true);

        // Should prompt for target (Shock needs a target)
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // Choose Grizzly Bears as target
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities(); // resolve Shock → deals 2 damage to Grizzly Bears

        // Grizzly Bears (2/2) should be destroyed by 2 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining may-cast leaves the card in opponent's graveyard")
    void decliningMayCastLeavesCardInGraveyard() {
        harness.skipMulligan();

        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));

        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → ETB → graveyard choice

        harness.handleMultipleGraveyardCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities(); // resolve ETB trigger → queues may-cast

        // Decline the may-cast
        harness.handleMayAbilityChosen(player1, false);

        // Shock should still be in opponent's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("ETB with no instant/sorcery in any opponent's graveyard does not prompt")
    void etbWithNoValidTargetsDoesNotPrompt() {
        harness.skipMulligan();

        // Only creature in opponent's graveyard — no valid targets
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → ETB

        // No graveyard choice should be prompted
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("ETB with empty opponent graveyard does not prompt")
    void etbWithEmptyOpponentGraveyardDoesNotPrompt() {
        harness.skipMulligan();

        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // No graveyard choice should be prompted
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("ETB fizzles when targeted card is removed from graveyard before resolution")
    void etbFizzlesWhenTargetRemoved() {
        harness.skipMulligan();

        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));

        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature → ETB → graveyard choice

        // Select Shock
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(shock.getId()));

        // Remove Shock from graveyard before ETB trigger resolves
        gd.playerGraveyards.get(player2.getId()).clear();

        // Resolve ETB trigger → should fizzle
        harness.passBothPriorities();

        // Verify fizzle was logged
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("ETB does not include controller's own graveyard cards as targets")
    void etbDoesNotTargetOwnGraveyard() {
        harness.skipMulligan();

        // Put instant only in controller's graveyard
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setGraveyard(player2, List.of());

        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.addMana(player1, ManaColor.BLUE, 7);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // No graveyard choice should be prompted (only own cards, not opponent's)
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
    }
}
