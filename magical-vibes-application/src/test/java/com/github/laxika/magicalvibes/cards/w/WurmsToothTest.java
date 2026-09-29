package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WurmsTooth.class, FugitiveWizard.class, LlanowarElves.class})
class WurmsToothTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Wurm's Tooth puts it on the stack as an artifact spell")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new WurmsTooth(), "{2}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Wurm's Tooth resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.castFromHand(player1, new WurmsTooth(), "{2}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Wurm's Tooth");
    }

    // ===== Triggered ability: controller casts green spell =====

    @Test
    @DisplayName("Controller casts green spell, accepts may ability, gains 1 life")
    void controllerCastsGreenSpellAndAccepts() {
        harness.addToBattlefield(player1, new WurmsTooth());

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new LlanowarElves(), "{G}");

        // Player1 should be prompted for may ability
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard() instanceof WurmsTooth);

        // Resolve the triggered ability
        harness.passBothPriorities();

        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    @DisplayName("Controller casts green spell, declines may ability, no life gain")
    void controllerCastsGreenSpellAndDeclines() {
        harness.addToBattlefield(player1, new WurmsTooth());

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new LlanowarElves(), "{G}");
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard() instanceof WurmsTooth);

        // Resolve the creature spell
        harness.passBothPriorities();

        // No life gained
        harness.assertLife(player1, lifeBefore);
    }

    // ===== Triggered ability: opponent casts green spell =====

    @Test
    @DisplayName("Opponent casts green spell, controller accepts may ability, gains 1 life")
    void opponentCastsGreenSpellControllerAccepts() {
        harness.addToBattlefield(player1, new WurmsTooth());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new LlanowarElves(), "{G}");

        // Player1 (controller of Wurm's Tooth) should be prompted
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Resolve the triggered ability and then the creature spell
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 1);
    }

    // ===== Non-green spell does NOT trigger =====

    @Test
    @DisplayName("Non-green spell does not trigger Wurm's Tooth")
    void nonGreenSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new WurmsTooth());

        harness.castFromHand(player1, new FugitiveWizard(), "{U}");

        GameData gd = harness.getGameData();
        // Should not be awaiting may ability
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        // Stack should only have the creature spell
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Fugitive Wizard");
        harness.assertLife(player1, 20);
    }

    // ===== Multiple teeth =====

    @Test
    @DisplayName("Multiple Wurm's Teeth each trigger independently")
    void multipleTeethTriggerIndependently() {
        harness.addToBattlefield(player1, new WurmsTooth());
        harness.addToBattlefield(player1, new WurmsTooth());

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new LlanowarElves(), "{G}");

        // First tooth prompt
        harness.handleMayAbilityChosen(player1, true);
        // Second tooth prompt
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        // Two triggered abilities on the stack (plus the creature spell)
        long triggeredCount = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredCount).isEqualTo(2);

        // Resolve all
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 2);
    }

    // ===== No trigger when not on battlefield =====

    @Test
    @DisplayName("Wurm's Tooth does not trigger when not on the battlefield")
    void doesNotTriggerWhenNotOnBattlefield() {
        // Wurm's Tooth is in the hand, not on the battlefield
        harness.setHand(player1, List.of(new WurmsTooth(), new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 1);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}
