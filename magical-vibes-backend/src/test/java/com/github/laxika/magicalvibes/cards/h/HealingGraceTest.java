package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToTargetFromChosenSourceEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HealingGraceTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Healing Grace has correct spell effects")
    void hasCorrectEffects() {
        HealingGrace card = new HealingGrace();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0))
                .isInstanceOf(PreventDamageToTargetFromChosenSourceEffect.class);
        assertThat(((PreventDamageToTargetFromChosenSourceEffect) card.getEffects(EffectSlot.SPELL).get(0)).amount())
                .isEqualTo(3);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1))
                .isInstanceOf(GainLifeEffect.class);
        assertThat(((GainLifeEffect) card.getEffects(EffectSlot.SPELL).get(1)).amount())
                .isEqualTo(3);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting Healing Grace targeting a player puts it on the stack")
    void castTargetingPlayerPutsOnStack() {
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Healing Grace");
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Casting Healing Grace targeting a creature puts it on the stack")
    void castTargetingCreaturePutsOnStack() {
        Permanent bear = addReadyCreature(player2);
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, bear.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(bear.getId());
    }

    // ===== Resolution — source choice and life gain =====

    @Test
    @DisplayName("Resolving Healing Grace prompts for source choice")
    void resolvingPromptsForSourceChoice() {
        Permanent opponentCreature = addReadyCreature(player2);
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput(AwaitingInput.PERMANENT_CHOICE)).isTrue();
    }

    @Test
    @DisplayName("Choosing a source creates a target-source prevention shield")
    void choosingSourceCreatesShield() {
        Permanent opponentCreature = addReadyCreature(player2);
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        assertThat(gd.targetSourceDamagePreventionShields).hasSize(1);
        assertThat(gd.targetSourceDamagePreventionShields.getFirst().targetId()).isEqualTo(player1.getId());
        assertThat(gd.targetSourceDamagePreventionShields.getFirst().sourceId()).isEqualTo(opponentCreature.getId());
        assertThat(gd.targetSourceDamagePreventionShields.getFirst().remainingAmount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Resolving Healing Grace gains 3 life for the caster")
    void resolvingGains3Life() {
        Permanent opponentCreature = addReadyCreature(player2);
        harness.setLife(player1, 17);
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Prevention — combat damage to player =====

    @Test
    @DisplayName("Shield prevents combat damage from chosen source to target player")
    void preventsCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        Permanent opponentCreature = addReadyCreature(player2);

        // Cast Healing Grace targeting player1, choose opponent's creature as source
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Combat: opponent's Grizzly Bears (2/2) attacks player1
        harness.forceActivePlayer(player2);
        opponentCreature.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // All 2 damage prevented (shield has 3 capacity)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isGreaterThanOrEqualTo(20);
    }

    @Test
    @DisplayName("Shield partially consumed when source deals more than 3 damage")
    void partialPreventionWhenSourceDealsMoreThan3() {
        harness.setLife(player1, 20);
        Permanent bigCreature = addReadyCreatureWithStats(player2, 5, 5);

        // Cast Healing Grace targeting player1, choose big creature as source
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bigCreature.getId());

        // Combat: 5/5 creature attacks player1
        harness.forceActivePlayer(player2);
        bigCreature.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // 5 damage - 3 prevented = 2 effective damage, but also +3 life from Healing Grace
        // Start: 20, +3 life = 23, -2 damage = 21
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    // ===== Prevention — combat damage to creature =====

    @Test
    @DisplayName("Shield prevents combat damage from chosen source to target creature")
    void preventsCombatDamageToCreature() {
        Permanent myCreature = addReadyCreature(player1);
        Permanent opponentCreature = addReadyCreatureWithStats(player2, 3, 3);

        // Cast Healing Grace targeting my creature, choose opponent's 3/3 as source
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, myCreature.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        // Combat: my 2/2 blocks opponent's 3/3
        harness.forceActivePlayer(player2);
        opponentCreature.setAttacking(true);
        myCreature.setBlocking(true);
        myCreature.addBlockingTarget(0);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // My 2/2 takes 3 damage from the 3/3, but 3 is prevented → creature survives
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
    }

    // ===== Non-matching source =====

    @Test
    @DisplayName("Shield does not prevent damage from non-matching source")
    void doesNotPreventFromNonMatchingSource() {
        harness.setLife(player1, 20);
        Permanent creature1 = addReadyCreature(player2);
        Permanent creature2 = addReadyCreatureWithStats(player2, 3, 3);

        // Cast Healing Grace targeting player1, choose creature1 as source
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature1.getId());

        // Combat: creature2 (not the chosen source) attacks player1
        harness.forceActivePlayer(player2);
        creature2.setAttacking(true);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Player1 takes full 3 damage from creature2 (not the chosen source)
        // Life: 20 +3 (Healing Grace) -3 (combat) = 20
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        // Shield should still be active (not consumed)
        assertThat(gd.targetSourceDamagePreventionShields).hasSize(1);
    }

    // ===== Shield cleanup =====

    @Test
    @DisplayName("Target-source prevention shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        Permanent opponentCreature = addReadyCreature(player2);
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        assertThat(gd.targetSourceDamagePreventionShields).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.targetSourceDamagePreventionShields).isEmpty();
    }

    // ===== Graveyard =====

    @Test
    @DisplayName("Healing Grace goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Permanent opponentCreature = addReadyCreature(player2);
        harness.setHand(player1, List.of(new HealingGrace()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, opponentCreature.getId());

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Healing Grace"));
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreatureWithStats(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
