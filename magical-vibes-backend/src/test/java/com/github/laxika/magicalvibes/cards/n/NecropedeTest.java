package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutMinusOneMinusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NecropedeTest extends BaseCardTest {

    /**
     * Sets up combat where Necropede (player1, 1/1) attacks and is blocked by a 3/3 creature (player2).
     * Necropede will die from combat damage.
     */
    private void setupCombatWhereNecropedeDies() {
        Permanent necropedePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Necropede"))
                .findFirst().orElseThrow();
        necropedePerm.setSummoningSick(false);
        necropedePerm.setAttacking(true);

        GrizzlyBears bigBear = new GrizzlyBears();
        bigBear.setPower(3);
        bigBear.setToughness(3);
        Permanent blockerPerm = new Permanent(bigBear);
        blockerPerm.setSummoningSick(false);
        blockerPerm.setBlocking(true);
        blockerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blockerPerm);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Necropede has ON_DEATH MayEffect wrapping PutMinusOneMinusOneCounterOnTargetCreatureEffect")
    void hasCorrectProperties() {
        Necropede card = new Necropede();

        assertThat(card.getEffects(EffectSlot.ON_DEATH)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_DEATH).getFirst()).isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_DEATH).getFirst();
        assertThat(may.wrapped()).isInstanceOf(PutMinusOneMinusOneCounterOnTargetCreatureEffect.class);
        assertThat(may.prompt()).isEqualTo("Put a -1/-1 counter on target creature?");
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Necropede puts it on the battlefield")
    void castingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new Necropede()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Necropede"));
    }

    // ===== Death trigger: target selection then may =====

    @Test
    @DisplayName("When Necropede dies, controller is prompted to choose a target creature (CR 603.3d)")
    void deathTriggerPromptsTargetChoice() {
        harness.addToBattlefield(player1, new Necropede());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setupCombatWhereNecropedeDies();

        harness.passBothPriorities(); // Combat damage — Necropede dies, target selection prompt

        // Necropede should be dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Necropede"));

        // CR 603.3d: targets are chosen when the triggered ability is put on the stack
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.permanentChoice().playerId()).isEqualTo(player1.getId());
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
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
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
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getId().equals(bearsId))
                .findFirst().orElseThrow();
        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(1);
    }

    // ===== Death trigger: kills 1/1 creature =====

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
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Llanowar Elves"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    // ===== Decline may =====

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

        // The blocker got 1 -1/-1 counter from infect combat damage, but no additional counter from the declined trigger
        Permanent bears = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                .findFirst().orElseThrow();
        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    // ===== Can target own creature =====

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
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(ownBearsId))
                .findFirst().orElseThrow();
        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    // ===== No creatures on battlefield (Wrath scenario) =====

    @Test
    @DisplayName("Death trigger from Wrath of God: no valid creature targets, trigger is skipped")
    void deathTriggerFromWrathNoValidTargets() {
        harness.addToBattlefield(player1, new Necropede());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath — all creatures die

        // Necropede should be dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Necropede"));

        // No valid creature targets — trigger is skipped entirely (no stack entry, no may prompt)
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no valid targets"));
    }

    @Test
    @DisplayName("Death trigger from Wrath of God: decline path (no valid targets, trigger skipped)")
    void deathTriggerFromWrathDeclineMay() {
        harness.addToBattlefield(player1, new Necropede());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath — Necropede dies

        // No triggered ability placed (no valid targets for the targeted trigger)
        assertThat(gd.stack).noneMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Necropede"));
    }

    // ===== Fizzle =====

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
}
