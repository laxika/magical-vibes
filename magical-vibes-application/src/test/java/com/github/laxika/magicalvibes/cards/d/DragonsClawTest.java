package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.w.WoollyThoctar;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonsClaw.class, RagingGoblin.class, GrizzlyBears.class, Shock.class, WoollyThoctar.class})
class DragonsClawTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Dragon's Claw puts it on the stack as an artifact spell")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new DragonsClaw(), "{2}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(DragonsClaw.class);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Dragon's Claw resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.castFromHand(player1, new DragonsClaw(), "{2}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Dragon's Claw");
    }

    // ===== Triggered ability: controller casts red spell =====

    @Test
    @DisplayName("Controller casts red spell, accepts may ability, gains 1 life")
    void controllerCastsRedSpellAndAccepts() {
        harness.addToBattlefield(player1, new DragonsClaw());
        harness.castFromHand(player1, new RagingGoblin(), "{R}");

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        // Player1 should be prompted for may ability
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard() instanceof DragonsClaw);

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Controller casts red spell, declines may ability, no life gain")
    void controllerCastsRedSpellAndDeclines() {
        harness.addToBattlefield(player1, new DragonsClaw());
        harness.castFromHand(player1, new RagingGoblin(), "{R}");

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard() instanceof DragonsClaw);

        // Resolve the creature spell
        harness.passBothPriorities();

        // No life gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Triggered ability: opponent casts red spell =====

    @Test
    @DisplayName("Opponent casts red spell, controller accepts may ability, gains 1 life")
    void opponentCastsRedSpellControllerAccepts() {
        harness.addToBattlefield(player1, new DragonsClaw());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new RagingGoblin(), "{R}");

        // Player1 (controller of Dragon's Claw) should be prompted
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Resolve the triggered ability and then the creature spell
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Casting a red instant also triggers Dragon's Claw")
    void redInstantSpellTriggers() {
        harness.addToBattlefield(player1, new DragonsClaw());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 1);
    }

    @Test
    @DisplayName("Multicolored red spell also triggers Dragon's Claw")
    void multicoloredRedSpellTriggers() {
        harness.addToBattlefield(player1, new DragonsClaw());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new WoollyThoctar(), "{R}{G}{W}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertLife(player1, lifeBefore + 1);
    }

    // ===== Non-red spell does NOT trigger =====

    @Test
    @DisplayName("Non-red spell does not trigger Dragon's Claw")
    void nonRedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new DragonsClaw());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        GameData gd = harness.getGameData();
        // Should not be awaiting may ability
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        // Stack should only have the creature spell
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Multiple claws =====

    @Test
    @DisplayName("Multiple Dragon's Claws each trigger independently")
    void multipleClawsTriggerIndependently() {
        harness.addToBattlefield(player1, new DragonsClaw());
        harness.addToBattlefield(player1, new DragonsClaw());
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new RagingGoblin(), "{R}");

        // First claw prompt
        harness.handleMayAbilityChosen(player1, true);
        // Second claw prompt
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        // Two triggered abilities on the stack (plus the creature spell)
        long triggeredCount = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredCount).isEqualTo(2);

        // Resolve all
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    // ===== No trigger when not on battlefield =====

    @Test
    @DisplayName("Dragon's Claw does not trigger when not on the battlefield")
    void doesNotTriggerWhenNotOnBattlefield() {
        // Dragon's Claw is in the hand, not on the battlefield
        harness.setHand(player1, List.of(new RagingGoblin(), new DragonsClaw()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}

