package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Telepathy;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarPriestOfThune.class, AngelicChorus.class, GrizzlyBears.class, Telepathy.class})
class WarPriestOfThuneTest extends BaseCardTest {

    /**
     * Casts War Priest of Thune and resolves it onto the battlefield, then accepts the may ability
     * after choosing its enchantment target as the trigger is put on the stack.
     */
    private void castAndAcceptMay(UUID enchantmentId) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new WarPriestOfThune(), "{1}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, enchantmentId);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("Resolving War Priest of Thune triggers may ability prompt when enchantment exists")
    void resolvingTriggersMayPrompt() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.castFromHand(player1, new WarPriestOfThune(), "{1}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Angelic Chorus"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Resolving War Priest prompts for enchantment target selection")
    void enteringPromptsForTargetBeforeMayChoice() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new WarPriestOfThune(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Accepting the optional destruction destroys the previously chosen enchantment")
    void acceptingMayDestroysPreviouslyChosenTarget() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID enchantmentId = harness.getPermanentId(player2, "Angelic Chorus");
        castAndAcceptMay(enchantmentId);

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
        harness.castFromHand(player1, new WarPriestOfThune(), "{1}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Angelic Chorus"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "War Priest of Thune");
        harness.assertOnBattlefield(player2, "Angelic Chorus");
    }

    @Test
    @DisplayName("May prompt does not fire when no enchantment on battlefield")
    void noMayPromptWithoutEnchantment() {
        // A "you may destroy target enchantment" trigger requires a legal target. With only a
        // non-enchantment permanent (Grizzly Bears) present the ability is removed from the stack
        // without a resolution-time optional choice.
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.castFromHand(player1, new WarPriestOfThune(), "{1}{W}");
        harness.passBothPriorities(); // resolve creature spell -> enters battlefield

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "War Priest of Thune");
    }

    @Test
    @DisplayName("A target removed before resolution prevents the optional destruction choice")
    void acceptingMayAfterTargetRemovedHasNoValidTargets() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new WarPriestOfThune(), "{1}{W}");
        harness.passBothPriorities();
        UUID enchantmentId = harness.getPermanentId(player2, "Angelic Chorus");
        harness.handlePermanentChosen(player1, enchantmentId);

        // Remove the enchantment before the triggered ability resolves
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can target own enchantment")
    void canTargetOwnEnchantment() {
        harness.addToBattlefield(player1, new Telepathy());
        UUID enchantmentId = harness.getPermanentId(player1, "Telepathy");
        castAndAcceptMay(enchantmentId);

        harness.assertNotOnBattlefield(player1, "Telepathy");
        harness.assertInGraveyard(player1, "Telepathy");
    }

    @Test
    @DisplayName("War Priest of Thune remains on battlefield after destroying enchantment")
    void priestRemainsOnBattlefield() {
        harness.addToBattlefield(player2, new AngelicChorus());
        UUID enchantmentId = harness.getPermanentId(player2, "Angelic Chorus");
        castAndAcceptMay(enchantmentId);

        harness.assertOnBattlefield(player1, "War Priest of Thune");
    }

    @Test
    @DisplayName("Only the selected enchantment is destroyed when multiple targets are legal")
    void destroysOnlySelectedEnchantment() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.addToBattlefield(player2, new Telepathy());

        castAndAcceptMay(harness.getPermanentId(player2, "Telepathy"));

        harness.assertInGraveyard(player2, "Telepathy");
        harness.assertNotOnBattlefield(player2, "Telepathy");
        harness.assertOnBattlefield(player2, "Angelic Chorus");
        harness.assertNotInGraveyard(player2, "Angelic Chorus");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The destruction trigger still resolves after War Priest leaves the battlefield")
    void triggerResolvesAfterPriestLeavesBattlefield() {
        harness.addToBattlefield(player2, new AngelicChorus());
        harness.castFromHand(player1, new WarPriestOfThune(), "{1}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Angelic Chorus"));

        UUID priestId = harness.getPermanentId(player1, "War Priest of Thune");
        gd.playerBattlefields.get(player1.getId()).removeIf(permanent -> permanent.getId().equals(priestId));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player1, "War Priest of Thune");
        harness.assertNotOnBattlefield(player2, "Angelic Chorus");
        harness.assertInGraveyard(player2, "Angelic Chorus");
        assertThat(gd.stack).isEmpty();
    }
}
