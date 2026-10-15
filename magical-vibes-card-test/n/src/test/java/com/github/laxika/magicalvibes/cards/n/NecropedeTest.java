package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.cards.m.MoriokReaver;
import com.github.laxika.magicalvibes.cards.c.ContagionClasp;
import com.github.laxika.magicalvibes.testutil.CardUsed;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import com.github.laxika.magicalvibes.model.CounterType;

@CardUsed({Necropede.class, GrizzlyBears.class, LlanowarElves.class, WrathOfGod.class,
        MoriokReaver.class, ContagionClasp.class})
class NecropedeTest extends BaseCardTest {

    /**
     * Sets up combat where Necropede (player1, 1/1) attacks and is blocked by a 3/2 creature (player2).
     * Necropede will die from combat damage.
     */
    private void setupCombatWhereNecropedeDies() {
        Permanent necropedePerm = findPermanent(player1, "Necropede");
        necropedePerm.setSummoningSick(false);
        necropedePerm.setAttacking(true);

        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new MoriokReaver());
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Casting Necropede puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.castFromHand(player1, new Necropede(), "{2}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Necropede");
    }

    @Test
    @DisplayName("When Necropede dies, controller is prompted to choose a target creature (CR 603.3d)")
    void deathTriggerPromptsTargetChoice() {
        harness.addToBattlefield(player1, new Necropede());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setupCombatWhereNecropedeDies();

        harness.passBothPriorities(); // Combat damage — Necropede dies, target selection prompt

        // Necropede should be dead
        harness.assertInGraveyard(player1, "Necropede");

        // CR 603.3d: targets are chosen when the triggered ability is put on the stack
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Choosing a target puts ability on stack, resolving prompts may choice")
    void choosingTargetPutsAbilityOnStackThenMayPrompt() {
        harness.addToBattlefield(player1, new Necropede());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereNecropedeDies();
        harness.passBothPriorities(); // Necropede dies, target selection

        harness.handlePermanentChosen(player1, bearsId); // Choose target -> ability on stack
        harness.passBothPriorities(); // Resolve -> may prompt

        // Player1 should be prompted for the may ability
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Death trigger puts -1/-1 counter on target 2/2 creature, reducing it to 1/1")
    void deathTriggerPutsCounterOnTarget() {
        harness.addToBattlefield(player1, new Necropede());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereNecropedeDies();
        harness.passBothPriorities(); // Necropede dies, target selection

        harness.handlePermanentChosen(player1, bearsId); // Choose target -> ability on stack
        harness.passBothPriorities(); // Resolve -> may prompt

        harness.handleMayAbilityChosen(player1, true); // Accept may -> effect resolves

        // Grizzly Bears should have a -1/-1 counter
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Death trigger kills a 1/1 creature with -1/-1 counter")
    void deathTriggerKillsOneOneCreature() {
        harness.addToBattlefield(player1, new Necropede());
        harness.addToBattlefield(player2, new LlanowarElves());

        UUID elvesId = harness.getPermanentId(player2, "Llanowar Elves");

        setupCombatWhereNecropedeDies();
        harness.passBothPriorities(); // Necropede dies, target selection

        harness.handlePermanentChosen(player1, elvesId); // Choose target -> ability on stack
        harness.passBothPriorities(); // Resolve -> may prompt

        harness.handleMayAbilityChosen(player1, true); // Accept may -> effect resolves

        // Llanowar Elves (1/1) should be dead from 0 toughness
        harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        harness.assertInGraveyard(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Declining may ability does not put counter on any creature")
    void decliningMayDoesNotPutCounter() {
        harness.addToBattlefield(player1, new Necropede());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereNecropedeDies();
        harness.passBothPriorities(); // Necropede dies, target selection

        harness.handlePermanentChosen(player1, bearsId); // Choose target -> ability on stack
        harness.passBothPriorities(); // Resolve -> may prompt

        harness.handleMayAbilityChosen(player1, false); // Decline may

        // No triggered ability on the stack
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Necropede"));

        // The targeted creature (regular 2/2 Grizzly Bears) should have 0 counters since the may was declined
        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(0);
    }

    @Test
    @DisplayName("Death trigger can target own creature")
    void deathTriggerCanTargetOwnCreature() {
        harness.addToBattlefield(player1, new Necropede());
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID ownBearsId = harness.getPermanentId(player1, "Grizzly Bears");

        setupCombatWhereNecropedeDies();
        harness.passBothPriorities(); // Necropede dies, target selection

        harness.handlePermanentChosen(player1, ownBearsId); // Choose own creature -> ability on stack
        harness.passBothPriorities(); // Resolve -> may prompt

        harness.handleMayAbilityChosen(player1, true); // Accept may -> effect resolves

        // Own Grizzly Bears should have a -1/-1 counter
        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Death trigger from Wrath of God: no valid creature targets, trigger is skipped")
    void deathTriggerFromWrathNoValidTargets() {
        harness.addToBattlefield(player1, new Necropede());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities(); // Resolve Wrath — all creatures die

        // Necropede should be dead
        harness.assertInGraveyard(player1, "Necropede");

        // No valid creature targets — trigger is skipped entirely (no stack entry, no may prompt)
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no valid targets"));
    }

    @Test
    @DisplayName("Death trigger from Wrath of God leaves no triggered ability when no targets exist")
    void deathTriggerFromWrathDeclineMay() {
        harness.addToBattlefield(player1, new Necropede());

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities(); // Resolve Wrath — Necropede dies

        // No triggered ability placed (no valid targets for the targeted trigger)
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Necropede"));
    }

    @Test
    @DisplayName("Triggered ability fizzles when target creature is removed before resolution")
    void abilityFizzlesWhenTargetRemoved() {
        harness.addToBattlefield(player1, new Necropede());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereNecropedeDies();
        harness.passBothPriorities(); // Necropede dies, target selection

        harness.handlePermanentChosen(player1, bearsId); // Choose target -> ability on stack

        // Remove the target before resolution
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(bearsId));

        harness.passBothPriorities(); // Resolve — target gone, fizzles

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Unblocked Necropede gives poison counters without reducing life")
    void infectDealsPoisonToPlayer() {
        Permanent necropede = harness.addToBattlefieldAndReturn(player1, new Necropede());
        necropede.setSummoningSick(false);
        necropede.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Necropede combat damage puts a counter on its blocker before its death ability resolves")
    void infectPutsCounterOnBlocker() {
        harness.addToBattlefield(player1, new Necropede());
        setupCombatWhereNecropedeDies();

        harness.passBothPriorities();

        Permanent blocker = findPermanent(player2, "Moriok Reaver");
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, blocker)).isEqualTo(1);
        harness.assertInGraveyard(player1, "Necropede");
    }

    @Test
    @DisplayName("Death ability cannot target a noncreature artifact")
    void deathTriggerExcludesNoncreatures() {
        harness.addToBattlefield(player1, new Necropede());
        Permanent clasp = harness.addToBattlefieldAndReturn(player2, new ContagionClasp());
        setupCombatWhereNecropedeDies();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validPermanentIds())
                .doesNotContain(clasp.getId());
    }

    @Test
    @DisplayName("Death ability does not resolve when its target stops being a creature")
    void deathTriggerFizzlesWhenTargetStopsBeingCreature() {
        harness.addToBattlefield(player1, new Necropede());
        Permanent clasp = harness.addToBattlefieldAndReturn(player2, new ContagionClasp());
        clasp.setAnimatedUntilEndOfTurn(true);
        clasp.setAnimatedPower(2);
        clasp.setAnimatedToughness(2);
        setupCombatWhereNecropedeDies();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, clasp.getId());

        clasp.setAnimatedUntilEndOfTurn(false);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(clasp.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }
}
