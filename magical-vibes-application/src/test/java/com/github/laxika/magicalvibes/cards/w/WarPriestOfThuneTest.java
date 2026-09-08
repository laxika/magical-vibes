package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Telepathy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WarPriestOfThuneTest extends BaseCardTest {

    /**
     * Casts War Priest of Thune and resolves it onto the battlefield, then accepts the may ability
     * and chooses the target enchantment so the ETB triggered ability is placed on the stack.
     */
    private void castAndAcceptMay(UUID enchantmentId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WarPriestOfThune()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, enchantmentId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    // ===== ETB may ability =====

    @Test
    @DisplayName("Resolving War Priest of Thune triggers may ability prompt when enchantment exists")
    void resolvingTriggersMayPrompt() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.setHand(player1, List.of(new WarPriestOfThune()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Angelic Chorus"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Resolving War Priest prompts for enchantment target selection")
    void acceptingMayPromptsForTarget() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WarPriestOfThune()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Choosing enchantment target resolves destroy effect inline")
    void choosingTargetResolvesDestroyInline() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID enchantmentId = harness.getPermanentId(player2, "Angelic Chorus");
        castAndAcceptMay(enchantmentId);

        // CR 603.5 — inner effect resolves inline when target is chosen
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("ETB resolves and destroys target enchantment")
    void etbDestroysTargetEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID enchantmentId = harness.getPermanentId(player2, "Angelic Chorus");
        castAndAcceptMay(enchantmentId);

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("Declining may ability does not destroy enchantment")
    void decliningMaySkipsDestruction() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WarPriestOfThune()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Angelic Chorus"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "War Priest of Thune");
        harness.assertOnBattlefield(player2, "Angelic Chorus");
    }

    // ===== No enchantment scenarios =====

    @Test
    @DisplayName("May prompt does not fire when no enchantment on battlefield")
    void noMayPromptWithoutEnchantment() {
        // A "you may destroy target enchantment" trigger requires a legal target. With only a
        // non-enchantment permanent (Grizzly Bears) present the ability is never put on the stack
        // (CR 601.2c / 603.3b), so the controller is never prompted to make the "may" choice.
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new WarPriestOfThune()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell -> enters battlefield

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "War Priest of Thune");
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Accepting may with target removed before resolution results in no valid targets")
    void acceptingMayAfterTargetRemovedHasNoValidTargets() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new WarPriestOfThune()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        UUID enchantmentId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.handlePermanentChosen(player1, enchantmentId);

        // Remove the enchantment before the triggered ability resolves
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    // ===== Can target own enchantment =====

    @Test
    @DisplayName("Can target own enchantment")
    void canTargetOwnEnchantment() {
        harness.addToBattlefield(player1, new Telepathy());
        UUID enchantmentId = harness.getPermanentId(player1, "Telepathy");
        castAndAcceptMay(enchantmentId);

        // CR 603.5 — inner effect resolves inline
        harness.assertNotOnBattlefield(player1, "Telepathy");
        harness.assertInGraveyard(player1, "Telepathy");
    }

    // ===== War Priest stays on battlefield =====

    @Test
    @DisplayName("War Priest of Thune remains on battlefield after destroying enchantment")
    void priestRemainsOnBattlefield() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID enchantmentId = harness.getPermanentId(player2, "Angelic Chorus");
        castAndAcceptMay(enchantmentId);

        harness.assertOnBattlefield(player1, "War Priest of Thune");
    }
}
