package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({RotWolf.class, GrizzlyBears.class, CruelEdict.class, GoForTheThroat.class, TurnToFrog.class})
class RotWolfTest extends BaseCardTest {

    @Test
    @DisplayName("Rot Wolf kills a creature via infect in combat, accept may, draws a card")
    void killsCreatureWithInfectAcceptDraw() {
        harness.addToBattlefield(player1, new RotWolf());

        // Use a 1/1 so Rot Wolf survives combat (takes only 1 damage)
        GrizzlyBears smallCreature = new GrizzlyBears();
        smallCreature.setPower(1);
        smallCreature.setToughness(1);
        harness.addToBattlefield(player2, smallCreature);

        Permanent rotWolf = gd.playerBattlefields.get(player1.getId()).getFirst();
        rotWolf.setSummoningSick(false);
        rotWolf.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Pass 1: DECLARE_BLOCKERS -> COMBAT_DAMAGE -> damage resolves -> trigger on stack
        // Pass 2: resolve triggered ability -> MayEffect prompts player
        resolveCombat();
        harness.passBothPriorities();

        // Blocker should be dead (from -1/-1 counters via infect)
        harness.assertInGraveyard(player2, "Grizzly Bears");

        // Rot Wolf should still be alive
        harness.assertOnBattlefield(player1, "Rot Wolf");

        // May ability prompt for Rot Wolf's controller
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);

        // Accepting the optional draw completes the resolving ability.
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Rot Wolf kills a creature via infect in combat, decline may, no card drawn")
    void killsCreatureWithInfectDeclineDraw() {
        harness.addToBattlefield(player1, new RotWolf());

        // Use a 1/1 so Rot Wolf survives combat
        GrizzlyBears smallCreature = new GrizzlyBears();
        smallCreature.setPower(1);
        smallCreature.setToughness(1);
        harness.addToBattlefield(player2, smallCreature);

        Permanent rotWolf = gd.playerBattlefields.get(player1.getId()).getFirst();
        rotWolf.setSummoningSick(false);
        rotWolf.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Resolve combat and trigger
        resolveCombat();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");

        // Decline the may ability
        harness.handleMayAbilityChosen(player1, false);

        // No triggered ability on the stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Rot Wolf"));

        // No card drawn
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore);
    }

    @Test
    @DisplayName("Creature damaged by Rot Wolf via infect dies later the same turn, triggers may draw")
    void triggersWhenDamagedCreatureDiesLaterThisTurn() {
        harness.addToBattlefield(player1, new RotWolf());

        // Create a creature tough enough to survive 2 -1/-1 counters
        GrizzlyBears toughBlocker = new GrizzlyBears();
        toughBlocker.setPower(1);
        toughBlocker.setToughness(5);
        harness.addToBattlefield(player2, toughBlocker);

        Permanent rotWolf = gd.playerBattlefields.get(player1.getId()).getFirst();
        rotWolf.setSummoningSick(false);
        rotWolf.setAttacking(true);

        Permanent blocker = gd.playerBattlefields.get(player2.getId()).getFirst();
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        // Resolve combat - Rot Wolf puts 2 -1/-1 counters on blocker, blocker survives (3 toughness left)
        resolveCombat();

        // Blocker should still be alive with -1/-1 counters
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);

        // Now kill the blocker with a Cruel Edict later in the turn
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        // Pass 1: resolve Cruel Edict, creature dies, ON_DAMAGED_CREATURE_DIES trigger fires
        harness.passBothPriorities();
        // Pass 2: resolve triggered ability -> MayEffect prompts player
        harness.passBothPriorities();

        // Blocker should be dead
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");

        // May ability prompt for Rot Wolf's controller (creature dealt damage by Rot Wolf died)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        // Player1 should have drawn a card
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1 + 1);
    }

    @Test
    @DisplayName("Rot Wolf draws when both combatants die simultaneously")
    void drawsWhenBothCombatantsDie() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new RotWolf());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        wolf.setSummoningSick(false);
        wolf.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombat();
        harness.assertInGraveyard(player1, "Rot Wolf");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Rot Wolf does not trigger after losing its abilities")
    void noDrawAfterLosingAbilities() {
        Permanent wolf = harness.addToBattlefieldAndReturn(player1, new RotWolf());
        GrizzlyBears toughCreature = new GrizzlyBears();
        toughCreature.setPower(0);
        toughCreature.setToughness(5);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, toughCreature);
        wolf.setSummoningSick(false);
        wolf.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        resolveCombat();
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Rot Wolf");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new TurnToFrog(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, wolf.getId());
        harness.assertOnBattlefield(player1, "Rot Wolf");
        harness.castAndResolveInstant(player1, 0, blocker.getId());
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).noneMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Rot Wolf"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Rot Wolf does not trigger when a creature it did not damage dies")
    void noTriggerWhenUndamagedCreatureDies() {
        harness.addToBattlefield(player1, new RotWolf());
        harness.addToBattlefield(player2, new GrizzlyBears());

        // Do NOT attack/block, just kill the creature with a spell
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // Creature died but was not damaged by Rot Wolf - no trigger
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1);
    }
}
