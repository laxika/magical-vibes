package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.a.Artillerize;
import com.github.laxika.magicalvibes.cards.n.NoxiousRevival;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.CardUsedExtension;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Tag("scryfall")
@ExtendWith(CardUsedExtension.class)
@CardUsed({ChancellorOfTheSpires.class, Shock.class, GrizzlyBears.class,
        CounselOfTheSoratami.class, Artillerize.class, NoxiousRevival.class})
class ChancellorOfTheSpiresTest {

    protected GameTestHarness harness;
    protected Player player1;
    protected Player player2;
    protected GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        // Do NOT call skipMulligan() here — opening hand tests need to set hand first
    }

    @Test
    @DisplayName("Chancellor reveal is chosen before the first turn, without using the stack")
    void openingHandRevealIsPregameChoice() {
        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.skipMulligan();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Accepting the pregame reveal creates a mandatory first-upkeep trigger")
    void revealedChancellorTriggersAtFirstUpkeep() {
        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.skipMulligan();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.stack).isEmpty();

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(ChancellorOfTheSpires.class);
    }

    @Test
    @DisplayName("Revealed Chancellor mills seven automatically when its upkeep trigger resolves")
    void openingHandTriggerMillsOpponent() {
        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.skipMulligan();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.handleMayAbilityChosen(player1, true);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        int graveyardSizeBefore = gd.playerGraveyards.get(player2.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 7);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardSizeBefore + 7);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Declining the pregame reveal creates no upkeep trigger and mills nothing")
    void decliningRevealDoesNotMill() {
        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.skipMulligan();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.stack).isEmpty();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Chancellor stays in hand after revealing it and resolving its upkeep trigger")
    void chancellorRemainsInHandAfterTrigger() {
        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.skipMulligan();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Chancellor of the Spires");
    }

    @Test
    @DisplayName("ETB with instant/sorcery in opponent's graveyard prompts graveyard choice")
    void etbPromptsGraveyardChoice() {
        harness.skipMulligan();

        // Put a Shock in opponent's graveyard
        harness.setGraveyard(player2, List.of(new Shock()));

        // Cast Chancellor
        harness.castFromHand(player1, new ChancellorOfTheSpires(), "{4}{U}{U}{U}");
        harness.passBothPriorities(); // resolve creature spell → ETB → graveyard choice

        // Should be prompting for graveyard choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }

    @Test
    @DisplayName("ETB only shows instant/sorcery cards from opponent's graveyard")
    void etbOnlyShowsInstantSorceryFromOpponent() {
        harness.skipMulligan();

        // Put a creature and an instant in opponent's graveyard
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(shock, bears));

        harness.castFromHand(player1, new ChancellorOfTheSpires(), "{4}{U}{U}{U}");
        harness.passBothPriorities();

        // Only Shock should be selectable (instant), not Grizzly Bears (creature)
        List<UUID> validIds = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds();
        assertThat(validIds).hasSize(1);
        assertThat(validIds).contains(shock.getId());
    }

    @Test
    @DisplayName("ETB casts non-targeted sorcery from opponent's graveyard without paying mana cost")
    void etbCastsNonTargetedSpellFromGraveyard() {
        harness.skipMulligan();

        CounselOfTheSoratami counsel = new CounselOfTheSoratami();
        harness.setGraveyard(player2, List.of(counsel));

        harness.castFromHand(player1, new ChancellorOfTheSpires(), "{4}{U}{U}{U}");
        harness.passBothPriorities(); // resolve creature → ETB → graveyard choice

        // Select the Counsel of the Soratami from graveyard
        harness.handleMultipleCardsChosen(player1, List.of(counsel.getId()));
        harness.passBothPriorities(); // resolve ETB trigger → queues may-cast

        // Accept the may-cast
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities(); // resolve the cast Counsel of the Soratami

        // The Chancellor left the hand, then the sorcery drew two cards.
        assertThat(gd.playerHands.get(player1.getId()).size()).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("ETB casts targeted instant from opponent's graveyard and prompts for target")
    void etbCastsTargetedSpellFromGraveyard() {
        harness.skipMulligan();

        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));

        // Add a creature for Shock to target
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.setLife(player2, 20);

        harness.castFromHand(player1, new ChancellorOfTheSpires(), "{4}{U}{U}{U}");
        harness.passBothPriorities(); // resolve creature → ETB → graveyard choice

        // Select Shock from opponent's graveyard
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities(); // resolve ETB trigger → queues may-cast

        // Accept the may-cast
        harness.handleMayAbilityChosen(player1, true);

        // Should prompt for target (Shock needs a target)
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // Choose Grizzly Bears as target
        harness.handlePermanentChosen(player1, bearsId);
        harness.passBothPriorities(); // resolve Shock → deals 2 damage to Grizzly Bears

        // Grizzly Bears (2/2) should be destroyed by 2 damage
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining may-cast leaves the card in opponent's graveyard")
    void decliningMayCastLeavesCardInGraveyard() {
        harness.skipMulligan();

        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));

        harness.castFromHand(player1, new ChancellorOfTheSpires(), "{4}{U}{U}{U}");
        harness.passBothPriorities(); // resolve creature → ETB → graveyard choice

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities(); // resolve ETB trigger → queues may-cast

        // Decline the may-cast
        harness.handleMayAbilityChosen(player1, false);

        // Shock should still be in opponent's graveyard
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    @DisplayName("ETB with no instant/sorcery in any opponent's graveyard does not prompt")
    void etbWithNoValidTargetsDoesNotPrompt() {
        harness.skipMulligan();

        // Only creature in opponent's graveyard — no valid targets
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));

        harness.castFromHand(player1, new ChancellorOfTheSpires(), "{4}{U}{U}{U}");
        harness.passBothPriorities(); // resolve creature → ETB

        // No graveyard choice should be prompted
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("ETB with empty opponent graveyard does not prompt")
    void etbWithEmptyOpponentGraveyardDoesNotPrompt() {
        harness.skipMulligan();

        harness.castFromHand(player1, new ChancellorOfTheSpires(), "{4}{U}{U}{U}");
        harness.passBothPriorities();

        // No graveyard choice should be prompted
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("ETB fizzles when targeted card is removed from graveyard before resolution")
    void etbFizzlesWhenTargetRemoved() {
        harness.skipMulligan();

        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));

        harness.castFromHand(player1, new ChancellorOfTheSpires(), "{4}{U}{U}{U}");
        harness.passBothPriorities(); // resolve creature → ETB → graveyard choice

        // Select Shock
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));

        // Remove Shock from graveyard before ETB trigger resolves
        gd.playerGraveyards.get(player2.getId()).clear();

        // Resolve ETB trigger → should fizzle
        harness.passBothPriorities();

        // Verify fizzle was logged
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("ETB does not include controller's own graveyard cards as targets")
    void etbDoesNotTargetOwnGraveyard() {
        harness.skipMulligan();

        // Put instant only in controller's graveyard
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(shock));
        harness.setGraveyard(player2, List.of());

        harness.castFromHand(player1, new ChancellorOfTheSpires(), "{4}{U}{U}{U}");
        harness.passBothPriorities();

        // No graveyard choice should be prompted (only own cards, not opponent's)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("A cast opponent-owned spell returns to that opponent's graveyard")
    void castSpellReturnsToOwnersGraveyard() {
        harness.skipMulligan();
        Shock shock = new Shock();
        harness.setGraveyard(player2, List.of(shock));
        harness.castFromHand(player1, new ChancellorOfTheSpires(), "{4}{U}{U}{U}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertInGraveyard(player2, "Shock");
        harness.assertNotInGraveyard(player1, "Shock");
    }

    @Test
    @DisplayName("Chancellor cannot cast Artillerize when its mandatory sacrifice cannot be paid")
    void cannotCastSpellWithUnpayableAdditionalCost() {
        harness.skipMulligan();
        Artillerize artillerize = new Artillerize();
        ChancellorOfTheSpires chancellor = new ChancellorOfTheSpires();
        harness.setGraveyard(player2, List.of(artillerize));
        harness.castFromHand(player1, chancellor, "{4}{U}{U}{U}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(artillerize.getId()));

        // The creature leaves before its triggered ability resolves.
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.setGraveyard(player1, List.of(chancellor));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, player2.getId());
        }

        assertThat(gd.stack).noneMatch(entry -> entry.getCard().getId().equals(artillerize.getId()));
        harness.assertInGraveyard(player2, "Artillerize");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Chancellor casts Noxious Revival with a graveyard card as its target")
    void castsSpellTargetingGraveyardCard() {
        harness.skipMulligan();
        NoxiousRevival revival = new NoxiousRevival();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(revival, bears));
        harness.castFromHand(player1, new ChancellorOfTheSpires(), "{4}{U}{U}{U}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(revival.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId()).isEqualTo(bears.getId());
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Noxious Revival");
        harness.assertLife(player1, 20);
    }
    @Test
    @DisplayName("Each revealed Chancellor creates its own seven-card mill trigger")
    void multipleRevealedChancellorsMillSeparately() {
        harness.setHand(player1, List.of(new ChancellorOfTheSpires(), new ChancellorOfTheSpires()));
        harness.skipMulligan();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.handleMayAbilityChosen(player1, true);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        int graveyardSizeBefore = gd.playerGraveyards.get(player2.getId()).size();
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 14);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardSizeBefore + 14);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The opening-hand trigger mills all remaining cards when fewer than seven remain")
    void openingHandTriggerMillsShortLibrary() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        harness.setLibrary(player2, List.of(first, second, third));
        harness.setHand(player1, List.of(new ChancellorOfTheSpires()));
        harness.skipMulligan();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(first, second, third);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
