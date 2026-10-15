package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelsFeather.class, GrizzlyBears.class, SuntailHawk.class})
class AngelsFeatherTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Angel's Feather puts it on the stack as an artifact spell")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new AngelsFeather(), "{2}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(AngelsFeather.class);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Angel's Feather resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.castFromHand(player1, new AngelsFeather(), "{2}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Angel's Feather");
    }

    @Test
    @DisplayName("Controller casts white spell, accepts may ability, gains 1 life")
    void controllerCastsWhiteSpellAndAccepts() {
        harness.addToBattlefield(player1, new AngelsFeather());
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        harness.castFromHand(player1, new SuntailHawk(), "{W}");

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Controller casts white spell, declines may ability, no life gain")
    void controllerCastsWhiteSpellAndDeclines() {
        harness.addToBattlefield(player1, new AngelsFeather());
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        harness.castFromHand(player1, new SuntailHawk(), "{W}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Angel's Feather"));

        // Resolve the creature spell
        harness.passBothPriorities();

        // No life gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Opponent casts white spell, controller accepts may ability, gains 1 life")
    void opponentCastsWhiteSpellControllerAccepts() {
        harness.addToBattlefield(player1, new AngelsFeather());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = harness.getGameData().playerLifeTotals.get(player2.getId());
        harness.castFromHand(player2, new SuntailHawk(), "{W}");

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Resolve the triggered ability and then the creature spell
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Non-white spell does not trigger Angel's Feather")
    void nonWhiteSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        GameData gd = harness.getGameData();
        // Should not be awaiting may ability
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        // Stack should only have the creature spell
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Multiple Angel's Feathers each trigger independently")
    void multipleFeathersTriggerIndependently() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.addToBattlefield(player1, new AngelsFeather());
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());
        harness.castFromHand(player1, new SuntailHawk(), "{W}");

        GameData gd = harness.getGameData();
        // Two triggered abilities on the stack (plus the creature spell)
        long triggeredCount = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredCount).isEqualTo(2);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Angel's Feather does not trigger when not on the battlefield")
    void doesNotTriggerWhenNotOnBattlefield() {
        // Angel's Feather is in the hand, not on the battlefield
        harness.setHand(player1, List.of(new AngelsFeather(), new SuntailHawk()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 1);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("White spell trigger goes on the stack before the life-gain choice")
    void lifeGainChoiceWaitsForTriggerResolution() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.setLife(player1, 20);
        harness.castFromHand(player1, new SuntailHawk(), "{W}");

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.assertLife(player1, 21);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("A white creature entering without being cast does not trigger")
    void enteringWithoutCastingDoesNotTrigger() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.setLife(player1, 20);
        harness.enterBattlefieldAndReturn(player1, new SuntailHawk());

        assertThat(harness.getGameData().stack).isEmpty();
        assertThat(harness.getGameData().interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Casting a colorless artifact does not trigger an existing Feather")
    void colorlessSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new AngelsFeather());
        harness.castFromHand(player1, new AngelsFeather(), "{2}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
    }

}

