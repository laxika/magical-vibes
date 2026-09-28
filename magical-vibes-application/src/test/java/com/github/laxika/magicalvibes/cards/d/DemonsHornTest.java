package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.a.Anaconda;
import com.github.laxika.magicalvibes.cards.b.BogImp;
import com.github.laxika.magicalvibes.cards.u.UnderworldDreams;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DemonsHorn.class, BogImp.class, Anaconda.class, UnderworldDreams.class})
class DemonsHornTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Demon's Horn puts it on the stack as an artifact spell")
    void castingPutsItOnStack() {
        harness.castFromHand(player1, new DemonsHorn(), "{2}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ARTIFACT_SPELL);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Demon's Horn resolves onto the battlefield")
    void resolvesOntoBattlefield() {
        harness.castFromHand(player1, new DemonsHorn(), "{2}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Controller casts black spell, accepts may ability, gains 1 life")
    void controllerCastsBlackSpellAndAccepts() {
        harness.addToBattlefield(player1, new DemonsHorn());
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new BogImp(), "{1}{B}");

        // Player1 should be prompted for may ability
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Triggered ability should be on the stack
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard() instanceof DemonsHorn);

        // Resolve the triggered ability
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Controller casts black spell, declines may ability, no life gain")
    void controllerCastsBlackSpellAndDeclines() {
        harness.addToBattlefield(player1, new DemonsHorn());
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new BogImp(), "{1}{B}");

        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        // No triggered ability on stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard() instanceof DemonsHorn);

        // Resolve the creature spell
        harness.passBothPriorities();

        // No life gained
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Opponent casts black spell, controller accepts may ability, gains 1 life")
    void opponentCastsBlackSpellControllerAccepts() {
        harness.addToBattlefield(player1, new DemonsHorn());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player2, new BogImp(), "{1}{B}");

        // Player1 (controller of Demon's Horn) should be prompted
        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Resolve the triggered ability and then the creature spell
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Casting a black noncreature spell triggers Demon's Horn")
    void blackNoncreatureSpellTriggers() {
        harness.addToBattlefield(player1, new DemonsHorn());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new UnderworldDreams(), "{B}{B}{B}");

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 1);
    }

    @Test
    @DisplayName("Non-black spell does not trigger Demon's Horn")
    void nonBlackSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new DemonsHorn());
        harness.castFromHand(player1, new Anaconda(), "{3}{G}");

        GameData gd = harness.getGameData();
        // Should not be awaiting may ability
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        // Stack should only have the creature spell
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Multiple Demon's Horns each trigger independently")
    void multipleHornsTriggerIndependently() {
        harness.addToBattlefield(player1, new DemonsHorn());
        harness.addToBattlefield(player1, new DemonsHorn());
        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new BogImp(), "{1}{B}");

        // First horn prompt
        harness.handleMayAbilityChosen(player1, true);
        // Second horn prompt
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

    @Test
    @DisplayName("Demon's Horn does not trigger when not on the battlefield")
    void doesNotTriggerWhenNotOnBattlefield() {
        harness.setHand(player1, List.of(new BogImp(), new DemonsHorn()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }
}

