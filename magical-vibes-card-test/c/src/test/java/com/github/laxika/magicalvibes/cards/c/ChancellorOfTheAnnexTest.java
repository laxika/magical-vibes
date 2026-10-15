package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GitaxianProbe;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChancellorOfTheAnnex.class, GrizzlyBears.class, SuntailHawk.class, Forest.class, GitaxianProbe.class})
class ChancellorOfTheAnnexTest extends BaseCardTest {

    @Test
    @DisplayName("Triggers when opponent casts a spell — puts triggered ability on stack")
    void triggersOnOpponentSpell() {
        harness.addToBattlefield(player1, new ChancellorOfTheAnnex());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Chancellor of the Annex");
    }

    @Test
    @DisplayName("Does NOT trigger when controller casts a spell")
    void doesNotTriggerOnControllerSpell() {
        harness.addToBattlefield(player1, new ChancellorOfTheAnnex());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        // Only the creature spell on the stack — no triggered ability
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Counters spell when opponent has no mana to pay")
    void countersWhenOpponentCannotPay() {
        harness.addToBattlefield(player1, new ChancellorOfTheAnnex());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2); // exact cost, no extra mana

        harness.castCreature(player2, 0);

        // Resolve the triggered ability — opponent has no mana left
        harness.passBothPriorities();

        // Spell is countered (in graveyard, not on battlefield)
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent is prompted to pay when they have mana")
    void opponentPromptedWhenTheyHaveMana() {
        harness.addToBattlefield(player1, new ChancellorOfTheAnnex());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 3); // 2 to cast, 1 extra

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Spell is not countered when opponent pays {1}")
    void spellNotCounteredWhenOpponentPays() {
        harness.addToBattlefield(player1, new ChancellorOfTheAnnex());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, true);

        // Spell should still be on the stack (not countered)
        harness.assertNotInGraveyard(player2, "Grizzly Bears");

        // Resolve the creature spell
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Spell is countered when opponent declines to pay")
    void spellCounteredWhenOpponentDeclines() {
        harness.addToBattlefield(player1, new ChancellorOfTheAnnex());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);

        // Spell should be countered
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Multiple Chancellors each trigger independently")
    void multipleTriggerIndependently() {
        harness.addToBattlefield(player1, new ChancellorOfTheAnnex());
        harness.addToBattlefield(player1, new ChancellorOfTheAnnex());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);

        // Two triggered abilities on the stack (plus the creature spell)
        long triggeredCount = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Triggers on every opponent spell, not just the first")
    void triggersOnEveryOpponentSpell() {
        harness.addToBattlefield(player1, new ChancellorOfTheAnnex());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Cast first spell with exact mana — auto-countered (no mana to pay tax)
        harness.setHand(player2, List.of(new SuntailHawk(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.castCreature(player2, 0); // Suntail Hawk (W), no mana left
        harness.passBothPriorities(); // resolve trigger → auto-counter

        // Cast second spell — should still trigger
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0); // Grizzly Bears (1G)

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Chancellor of the Annex");
    }

    @Test
    @DisplayName("Chancellor in opening hand prompts reveal before the first turn")
    void openingHandPromptsRevealChoice() {
        GameTestHarness h = new GameTestHarness();
        Player p1 = h.getPlayer1();
        GameData gameData = h.getGameData();

        h.setHand(p1, List.of(new ChancellorOfTheAnnex()));
        h.skipMulligan();

        assertThat(gameData.stack).isEmpty();

        // Game should be awaiting may ability choice from p1
        assertThat(gameData.interaction.isAwaitingInput()).isTrue();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting reveal registers a delayed counter trigger")
    void acceptingRevealRegistersDelayedTrigger() {
        GameTestHarness h = new GameTestHarness();
        Player p1 = h.getPlayer1();
        GameData gameData = h.getGameData();

        h.setHand(p1, List.of(new ChancellorOfTheAnnex()));
        h.skipMulligan();

        h.handleMayAbilityChosen(p1, true);

        assertThat(gameData.openingHandRevealTriggers).hasSize(1);
        assertThat(gameData.openingHandRevealTriggers.getFirst().revealingPlayerId()).isEqualTo(p1.getId());
        h.assertInHand(p1, "Chancellor of the Annex");
        assertThat(gameData.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("reveals Chancellor of the Annex"));
    }

    @Test
    @DisplayName("Declining reveal does not register a delayed trigger")
    void decliningRevealDoesNotRegisterTrigger() {
        GameTestHarness h = new GameTestHarness();
        Player p1 = h.getPlayer1();
        GameData gameData = h.getGameData();

        h.setHand(p1, List.of(new ChancellorOfTheAnnex()));
        h.skipMulligan();

        h.handleMayAbilityChosen(p1, false);

        assertThat(gameData.openingHandRevealTriggers).isEmpty();
    }

    @Test
    @DisplayName("Opening hand trigger counters opponent's first spell unless they pay {1}")
    void openingHandTriggerCountersFirstSpell() {
        GameTestHarness h = new GameTestHarness();
        Player p1 = h.getPlayer1();
        Player p2 = h.getPlayer2();
        GameData gameData = h.getGameData();

        h.setHand(p1, List.of(new ChancellorOfTheAnnex()));
        h.skipMulligan();

        // Accept the reveal
        h.handleMayAbilityChosen(p1, true);

        h.forceActivePlayer(p2);
        h.forceStep(TurnStep.PRECOMBAT_MAIN);
        h.clearPriorityPassed();

        h.setHand(p2, List.of(new SuntailHawk()));
        h.addMana(p2, ManaColor.WHITE, 1); // exact cost, no extra

        h.castCreature(p2, 0);

        // Delayed trigger fires — triggered ability on the stack
        assertThat(gameData.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isGreaterThanOrEqualTo(1);

        // Resolve trigger — opponent can't pay, spell is countered
        h.passBothPriorities();

        h.assertInGraveyard(p2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Opening hand trigger only fires on first spell of the game")
    void openingHandTriggerOnlyFirstSpell() {
        GameTestHarness h = new GameTestHarness();
        Player p1 = h.getPlayer1();
        Player p2 = h.getPlayer2();
        GameData gameData = h.getGameData();

        h.setHand(p1, List.of(new ChancellorOfTheAnnex()));
        h.skipMulligan();

        // Accept the reveal
        h.handleMayAbilityChosen(p1, true);

        // Chancellor stays in hand (not on battlefield), so only the opening hand trigger fires

        h.forceActivePlayer(p2);
        h.forceStep(TurnStep.PRECOMBAT_MAIN);
        h.clearPriorityPassed();

        // First spell with exact mana: triggers delayed counter, auto-countered
        h.setHand(p2, List.of(new SuntailHawk(), new GrizzlyBears()));
        h.addMana(p2, ManaColor.WHITE, 1);

        h.castCreature(p2, 0); // Suntail Hawk (W), no mana left

        // Count triggered abilities from the opening hand trigger
        long openingHandTriggeredCount = gameData.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Chancellor of the Annex"))
                .count();
        assertThat(openingHandTriggeredCount).isEqualTo(1);

        h.passBothPriorities(); // resolve trigger — auto-counters the spell

        // Second spell: should NOT get an opening hand trigger (Chancellor not on battlefield)
        h.forceStep(TurnStep.PRECOMBAT_MAIN);
        h.clearPriorityPassed();
        h.addMana(p2, ManaColor.GREEN, 2);
        h.castCreature(p2, 0); // Grizzly Bears

        // No triggered ability (Chancellor is in hand, not on battlefield; opening hand trigger already used)
        long secondTriggeredCount = gameData.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Chancellor of the Annex"))
                .count();
        assertThat(secondTriggeredCount).isEqualTo(0);
    }

    @Test
    @DisplayName("Multiple Chancellors in opening hand create multiple delayed triggers")
    void multipleChancellorsInOpeningHand() {
        GameTestHarness h = new GameTestHarness();
        Player p1 = h.getPlayer1();
        Player p2 = h.getPlayer2();
        GameData gameData = h.getGameData();

        h.setHand(p1, List.of(new ChancellorOfTheAnnex(), new ChancellorOfTheAnnex()));
        h.skipMulligan();

        h.handleMayAbilityChosen(p1, true);
        h.handleMayAbilityChosen(p1, true);

        assertThat(gameData.openingHandRevealTriggers).hasSize(2);

        h.forceActivePlayer(p2);
        h.forceStep(TurnStep.PRECOMBAT_MAIN);
        h.clearPriorityPassed();

        h.setHand(p2, List.of(new SuntailHawk()));
        h.addMana(p2, ManaColor.WHITE, 1);

        h.castCreature(p2, 0);

        // Two triggered abilities from the two Chancellor reveals
        long triggeredCount = gameData.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Chancellor of the Annex"))
                .count();
        assertThat(triggeredCount).isEqualTo(2);
    }

    @Test
    @DisplayName("Opening hand trigger allows opponent to pay {1} to avoid counter")
    void openingHandTriggerOpponentCanPay() {
        GameTestHarness h = new GameTestHarness();
        Player p1 = h.getPlayer1();
        Player p2 = h.getPlayer2();
        GameData gameData = h.getGameData();

        h.setHand(p1, List.of(new ChancellorOfTheAnnex()));
        h.skipMulligan();

        // Accept the reveal
        h.handleMayAbilityChosen(p1, true);

        h.forceActivePlayer(p2);
        h.forceStep(TurnStep.PRECOMBAT_MAIN);
        h.clearPriorityPassed();

        h.setHand(p2, List.of(new SuntailHawk()));
        h.addMana(p2, ManaColor.WHITE, 2); // 1 to cast, 1 to pay

        h.castCreature(p2, 0);
        h.passBothPriorities(); // resolve trigger

        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        h.handleMayAbilityChosen(p2, true);

        // Spell not countered
        h.assertNotInGraveyard(p2, "Suntail Hawk");

        // Resolve the creature spell
        h.passBothPriorities();

        h.assertOnBattlefield(p2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Opponent can generate mana when asked to pay the counter tax")
    void opponentCanPayUsingUntappedLand() {
        harness.addToBattlefield(player1, new ChancellorOfTheAnnex());
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("The battlefield ability also counters noncreature spells")
    void countersOpponentSorcery() {
        harness.addToBattlefield(player1, new ChancellorOfTheAnnex());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GitaxianProbe()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Gitaxian Probe");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Counter trigger still resolves after Chancellor leaves the battlefield")
    void triggerSurvivesSourceLeavingBattlefield() {
        harness.addToBattlefield(player1, new ChancellorOfTheAnnex());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castCreature(player2, 0);
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying the opening-hand tax consumes it even when the spell resolves")
    void paidOpeningHandTaxDoesNotApplyToSecondSpell() {
        GameTestHarness h = new GameTestHarness();
        Player p1 = h.getPlayer1();
        Player p2 = h.getPlayer2();
        GameData gameData = h.getGameData();
        h.setHand(p1, List.of(new ChancellorOfTheAnnex()));
        h.skipMulligan();
        h.handleMayAbilityChosen(p1, true);
        h.forceActivePlayer(p2);
        h.forceStep(TurnStep.PRECOMBAT_MAIN);
        h.clearPriorityPassed();
        h.setHand(p2, List.of(new SuntailHawk(), new GrizzlyBears()));
        h.addMana(p2, ManaColor.WHITE, 2);

        h.castCreature(p2, 0);
        h.passBothPriorities();
        h.handleMayAbilityChosen(p2, true);
        h.passBothPriorities();
        h.assertOnBattlefield(p2, "Suntail Hawk");

        h.forceStep(TurnStep.PRECOMBAT_MAIN);
        h.clearPriorityPassed();
        h.addMana(p2, ManaColor.GREEN, 2);
        h.castCreature(p2, 0);

        assertThat(gameData.stack).hasSize(1);
        h.passBothPriorities();
        h.assertOnBattlefield(p2, "Grizzly Bears");
    }
}
