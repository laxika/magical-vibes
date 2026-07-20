package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the fuzz-test invariants: each check must fire on a state that violates it
 * and stay silent on legitimate states, because a buggy invariant makes every fuzz batch
 * noisy. Violating states are built directly on the game data — the point is to verify the
 * detectors, not the engine paths that would normally prevent these states.
 */
@Tag("scryfall")
class FuzzInvariantsTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;
    private FuzzInvariants invariants;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        invariants = new FuzzInvariants(harness.getGameQueryService(), Map.of());
    }

    /** Two-strike rule: a violation must be observed on two consecutive polls to be reported. */
    private String checkTwice() {
        invariants.check(gd);
        return invariants.check(gd);
    }

    private static Card creature(String name, int power, int toughness) {
        Card c = new Card();
        c.setName(name);
        c.setType(CardType.CREATURE);
        c.setManaCost("{1}");
        c.setPower(power);
        c.setToughness(toughness);
        return c;
    }

    private static Card legendaryCreature(String name) {
        Card c = creature(name, 2, 2);
        c.setSupertypes(Set.of(CardSupertype.LEGENDARY));
        return c;
    }

    private static Card aura(String name) {
        Card c = new Card();
        c.setName(name);
        c.setType(CardType.ENCHANTMENT);
        c.setManaCost("{1}");
        c.setSubtypes(List.of(CardSubtype.AURA));
        return c;
    }

    private static Card equipment(String name) {
        Card c = new Card();
        c.setName(name);
        c.setType(CardType.ARTIFACT);
        c.setManaCost("{1}");
        c.setSubtypes(List.of(CardSubtype.EQUIPMENT));
        return c;
    }

    private StackEntry stackEntry(String cardName) {
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature(cardName, 1, 1),
                player1.getId(), "test entry", new ArrayList<>());
    }

    // ------------------------------------------------------------------
    // Baseline
    // ------------------------------------------------------------------

    @Test
    void cleanStateHasNoViolations() {
        assertThat(invariants.check(gd)).isNull();
        assertThat(invariants.check(gd)).isNull();
    }

    // ------------------------------------------------------------------
    // Game-over SBA (CR 704.5a-c)
    // ------------------------------------------------------------------

    @Test
    void lifeAtZeroWithEmptyStackMustEndTheGame() {
        harness.setLife(player2, 0);
        assertThat(invariants.check(gd)).isNull();
        assertThat(invariants.check(gd)).contains("loss SBA has not ended the game");
    }

    @Test
    void transientZeroLifeIsNotReported() {
        harness.setLife(player2, 0);
        assertThat(invariants.check(gd)).isNull();
        harness.setLife(player2, 5);
        assertThat(invariants.check(gd)).isNull();
    }

    @Test
    void zeroLifeIsNotReportedOnceTheGameIsFinished() {
        harness.setLife(player2, -3);
        gd.status = GameStatus.FINISHED;
        assertThat(checkTwice()).isNull();
    }

    @Test
    void gameOverCheckWaitsForAnEmptyStack() {
        harness.setLife(player2, 0);
        gd.stack.add(stackEntry("On Stack"));
        assertThat(checkTwice()).isNull();
    }

    @Test
    void gameOverCheckIsSuppressedWhileAwaitingInput() {
        harness.setLife(player2, 0);
        gd.interaction.beginInteraction(
                new PendingInteraction.XValueChoice(player1.getId(), 3, "Pick X", "Test"));
        assertThat(checkTwice()).isNull();
    }

    // ------------------------------------------------------------------
    // Dangling pendingEffectResolutionEntry
    // ------------------------------------------------------------------

    @Test
    void danglingPendingEffectResolutionEntryIsReported() {
        gd.pendingEffectResolutionEntry = stackEntry("Parked Spell");
        assertThat(invariants.check(gd)).isNull();
        assertThat(invariants.check(gd)).contains("Parked Spell");
    }

    @Test
    void parkedEntryIsAllowedWhileAnInteractionIsActive() {
        gd.pendingEffectResolutionEntry = stackEntry("Parked Spell");
        gd.interaction.beginInteraction(
                new PendingInteraction.XValueChoice(player1.getId(), 3, "Pick X", "Test"));
        assertThat(checkTwice()).isNull();
    }

    @Test
    void parkedEntryIsAllowedWhileAnInteractionIsQueued() {
        gd.pendingEffectResolutionEntry = stackEntry("Parked Spell");
        gd.pendingInteractions.add(
                new PendingInteraction.XValueChoice(player1.getId(), 3, "Pick X", "Test"));
        assertThat(checkTwice()).isNull();
    }

    // ------------------------------------------------------------------
    // Legend rule (CR 704.5j)
    // ------------------------------------------------------------------

    @Test
    void duplicateLegendariesUnderOneControllerAreReported() {
        harness.addToBattlefield(player1, legendaryCreature("Isamaru, Hound of Konda"));
        harness.addToBattlefield(player1, legendaryCreature("Isamaru, Hound of Konda"));
        assertThat(invariants.check(gd)).isNull();
        assertThat(invariants.check(gd)).contains("Isamaru, Hound of Konda");
    }

    @Test
    void sameLegendUnderDifferentControllersIsLegal() {
        harness.addToBattlefield(player1, legendaryCreature("Isamaru, Hound of Konda"));
        harness.addToBattlefield(player2, legendaryCreature("Isamaru, Hound of Konda"));
        assertThat(checkTwice()).isNull();
    }

    @Test
    void duplicateNonLegendariesAreLegal() {
        harness.addToBattlefield(player1, creature("Grizzly Bears", 2, 2));
        harness.addToBattlefield(player1, creature("Grizzly Bears", 2, 2));
        assertThat(checkTwice()).isNull();
    }

    // ------------------------------------------------------------------
    // Attachment existence (CR 704.5m/n/q)
    // ------------------------------------------------------------------

    @Test
    void auraAttachedToNothingIsReported() {
        harness.addToBattlefield(player1, aura("Loose Aura"));
        assertThat(invariants.check(gd)).isNull();
        assertThat(invariants.check(gd)).contains("Loose Aura");
    }

    @Test
    void auraAttachedToDepartedPermanentIsReported() {
        Permanent orphan = harness.addToBattlefieldAndReturn(player1, aura("Orphan Aura"));
        orphan.setAttachedTo(UUID.randomUUID());
        assertThat(invariants.check(gd)).isNull();
        assertThat(invariants.check(gd)).contains("Orphan Aura");
    }

    @Test
    void auraAttachedToExistingPermanentOrPlayerIsLegal() {
        Permanent host = harness.addToBattlefieldAndReturn(player1, creature("Host", 2, 2));
        Permanent creatureAura = harness.addToBattlefieldAndReturn(player1, aura("Creature Aura"));
        creatureAura.setAttachedTo(host.getId());
        Permanent curse = harness.addToBattlefieldAndReturn(player1, aura("Player Curse"));
        curse.setAttachedTo(player2.getId());
        assertThat(checkTwice()).isNull();
    }

    @Test
    void unattachedEquipmentIsLegal() {
        harness.addToBattlefield(player1, equipment("Spare Sword"));
        assertThat(checkTwice()).isNull();
    }

    @Test
    void equipmentAttachedToDepartedPermanentIsReported() {
        Permanent sword = harness.addToBattlefieldAndReturn(player1, equipment("Lost Sword"));
        sword.setAttachedTo(UUID.randomUUID());
        assertThat(invariants.check(gd)).isNull();
        assertThat(invariants.check(gd)).contains("Lost Sword");
    }

    // ------------------------------------------------------------------
    // Tokens in hidden zones (CR 704.5d)
    // ------------------------------------------------------------------

    @Test
    void tokenCardInHandIsReported() {
        Card token = creature("Saproling", 1, 1);
        token.setToken(true);
        gd.playerHands.get(player1.getId()).add(token);
        assertThat(invariants.check(gd)).isNull();
        assertThat(invariants.check(gd)).contains("Saproling").contains("hand");
    }

    @Test
    void tokenCardInLibraryIsReported() {
        Card token = creature("Saproling", 1, 1);
        token.setToken(true);
        gd.playerDecks.get(player1.getId()).add(token);
        assertThat(invariants.check(gd)).isNull();
        assertThat(invariants.check(gd)).contains("Saproling").contains("library");
    }

    @Test
    void tokenCardInGraveyardIsAllowed() {
        // The engine deliberately keeps dead tokens in graveyard lists and filters them
        // at read sites, so the invariant must not flag them there.
        Card token = creature("Saproling", 1, 1);
        token.setToken(true);
        gd.playerGraveyards.get(player1.getId()).add(token);
        assertThat(checkTwice()).isNull();
    }

    // ------------------------------------------------------------------
    // Moved pre-existing checks (regression guards for the extraction)
    // ------------------------------------------------------------------

    @Test
    void vanishedCardIsReportedByConservation() {
        Card tracked = creature("Vanisher", 2, 2);
        gd.playerHands.get(player1.getId()).add(tracked);
        invariants = new FuzzInvariants(harness.getGameQueryService(),
                Map.of(tracked.getId(), tracked.getName()));
        assertThat(checkTwice()).isNull();

        gd.playerHands.get(player1.getId()).remove(tracked);
        assertThat(invariants.check(gd)).isNull();
        assertThat(invariants.check(gd)).contains("Vanisher").contains("found 0 times");
    }

    @Test
    void zeroToughnessCreatureSurvivingSbaIsReported() {
        harness.addToBattlefield(player1, creature("Doomed", 0, 0));
        assertThat(invariants.check(gd)).isNull();
        assertThat(invariants.check(gd)).contains("Doomed");
    }
}
