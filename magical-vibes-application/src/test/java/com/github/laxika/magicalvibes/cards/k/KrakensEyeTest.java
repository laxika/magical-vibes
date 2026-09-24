package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.c.CrazedGoblin;
import com.github.laxika.magicalvibes.cards.e.EchoingTruth;
import com.github.laxika.magicalvibes.cards.n.NeurokProdigy;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KrakensEye.class, NeurokProdigy.class, CrazedGoblin.class, EchoingTruth.class})
class KrakensEyeTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Kraken's Eye puts it on the stack as an artifact spell")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new KrakensEye(), "{2}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Kraken's Eye");
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Kraken's Eye resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.castFromHand(player1, new KrakensEye(), "{2}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Kraken's Eye");
    }

    // ===== Triggered ability: controller casts blue spell =====

    @Test
    @DisplayName("Controller casts blue spell, accepts may ability, gains 1 life")
    void controllerCastsBlueSpellAndAccepts() {
        harness.addToBattlefield(player1, new KrakensEye());

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new NeurokProdigy(), "{2}{U}");

        // Player1 should be prompted for may ability
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Kraken's Eye"));

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Controller casts blue spell, declines may ability, no life gain")
    void controllerCastsBlueSpellAndDeclines() {
        harness.addToBattlefield(player1, new KrakensEye());

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new NeurokProdigy(), "{2}{U}");
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Kraken's Eye"));

        // Resolve the creature spell
        harness.passBothPriorities();

        // No life gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Triggered ability: opponent casts blue spell =====

    @Test
    @DisplayName("Opponent casts blue spell, controller accepts may ability, gains 1 life")
    void opponentCastsBlueSpellControllerAccepts() {
        harness.addToBattlefield(player1, new KrakensEye());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new NeurokProdigy(), "{2}{U}");

        // Player1 (controller of Kraken's Eye) should be prompted
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Resolve the triggered ability and then the creature spell
        harness.passBothPriorities(); // resolve triggered ability
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Casting a blue instant also triggers Kraken's Eye")
    void blueInstantSpellTriggers() {
        harness.addToBattlefield(player1, new KrakensEye());
        harness.addToBattlefield(player2, new CrazedGoblin());
        UUID targetId = harness.getPermanentId(player2, "Crazed Goblin");
        harness.setHand(player1, List.of(new EchoingTruth()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0, targetId);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
        harness.assertInHand(player2, "Crazed Goblin");
    }

    // ===== Non-blue spell does NOT trigger =====

    @Test
    @DisplayName("Non-blue spell does not trigger Kraken's Eye")
    void nonBlueSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new KrakensEye());

        harness.castFromHand(player1, new CrazedGoblin(), "{R}");

        GameData gd = harness.getGameData();
        // Should not be awaiting may ability
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        // Stack should only have the creature spell
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Multiple eyes =====

    @Test
    @DisplayName("Multiple Kraken's Eyes each trigger independently")
    void multipleEyesTriggerIndependently() {
        harness.addToBattlefield(player1, new KrakensEye());
        harness.addToBattlefield(player1, new KrakensEye());

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new NeurokProdigy(), "{2}{U}");

        // First eye prompt
        harness.handleMayAbilityChosen(player1, true);
        // Second eye prompt
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        // Two triggered abilities on the stack (plus the creature spell)
        long triggeredCount = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredCount).isEqualTo(2);

        // Resolve all
        harness.passBothPriorities(); // resolve second triggered ability
        harness.passBothPriorities(); // resolve first triggered ability
        harness.passBothPriorities(); // resolve creature spell

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    // ===== No trigger when not on battlefield =====

    @Test
    @DisplayName("Kraken's Eye does not trigger when not on the battlefield")
    void doesNotTriggerWhenNotOnBattlefield() {
        // Kraken's Eye is not on the battlefield
        harness.castFromHand(player1, new NeurokProdigy(), "{2}{U}");

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
