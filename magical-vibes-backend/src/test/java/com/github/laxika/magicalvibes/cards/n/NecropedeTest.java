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

    // ===== Death trigger: accept may, put counter on 2/2 =====

    @Test
    @DisplayName("When Necropede dies, controller is prompted with may ability")
    void deathTriggerPromptsMayChoice() {
        harness.addToBattlefield(player1, new Necropede());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setupCombatWhereNecropedeDies();

        harness.passBothPriorities(); // Combat damage — Necropede dies

        // Necropede should be dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Necropede"));

        // Player1 should be prompted for the may ability
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting may prompts for target creature selection")
    void acceptingMayPromptsForTarget() {
        harness.addToBattlefield(player1, new Necropede());
        harness.addToBattlefield(player2, new GrizzlyBears());
        setupCombatWhereNecropedeDies();

        harness.passBothPriorities(); // Necropede dies
        harness.handleMayAbilityChosen(player1, true); // Accept may

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);
        assertThat(gd.interaction.awaitingPermanentChoicePlayerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Death trigger puts -1/-1 counter on target 2/2 creature, reducing it to 1/1")
    void deathTriggerPutsCounterOnTarget() {
        harness.addToBattlefield(player1, new Necropede());
        harness.addToBattlefield(player2, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        setupCombatWhereNecropedeDies();
        harness.passBothPriorities(); // Necropede dies

        harness.handleMayAbilityChosen(player1, true); // Accept may
        harness.handlePermanentChosen(player1, bearsId); // Choose target

        // Triggered ability should be on the stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Necropede");
        assertThat(gd.stack.getFirst().getTargetPermanentId()).isEqualTo(bearsId);

        // Resolve the triggered ability
        harness.passBothPriorities();

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
        harness.passBothPriorities(); // Necropede dies

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, elvesId);
        harness.passBothPriorities(); // Resolve triggered ability

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
        // setupCombatWhereNecropedeDies adds a 3/3 blocker internally
        setupCombatWhereNecropedeDies();
        harness.passBothPriorities(); // Necropede dies

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
        harness.passBothPriorities(); // Necropede dies

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, ownBearsId);
        harness.passBothPriorities(); // Resolve triggered ability

        // Own Grizzly Bears should have a -1/-1 counter
        Permanent bears = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(ownBearsId))
                .findFirst().orElseThrow();
        assertThat(bears.getMinusOneMinusOneCounters()).isEqualTo(1);
    }

    // ===== No creatures on battlefield (Wrath scenario) =====

    @Test
    @DisplayName("Death trigger from Wrath of God: accept may but no valid creature targets")
    void deathTriggerFromWrathAcceptMayNoTargets() {
        harness.addToBattlefield(player1, new Necropede());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath — all creatures die

        // Necropede should be dead
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Necropede"));

        // Player1 should be prompted for may ability
        assertThat(gd.interaction.awaitingMayAbilityPlayerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true); // Accept may

        // No valid creature targets — stack should be empty
        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no valid targets"));
    }

    @Test
    @DisplayName("Death trigger from Wrath of God: decline may")
    void deathTriggerFromWrathDeclineMay() {
        harness.addToBattlefield(player1, new Necropede());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // Resolve Wrath

        harness.handleMayAbilityChosen(player1, false); // Decline may

        // No triggered ability placed
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
        harness.passBothPriorities(); // Necropede dies

        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bearsId);

        // Remove the target before the ability resolves
        gd.playerBattlefields.get(player2.getId()).removeIf(p -> p.getId().equals(bearsId));

        // Resolve — should fizzle
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }
}
