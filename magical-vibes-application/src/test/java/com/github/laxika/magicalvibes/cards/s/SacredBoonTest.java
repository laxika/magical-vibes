package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ControlMagic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImprisonedInTheMoon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SacredBoon.class, GrizzlyBears.class, Shock.class, ImprisonedInTheMoon.class, ControlMagic.class})
class SacredBoonTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Sacred Boon targets a creature and goes on the stack")
    void castingPutsItOnStack() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, targetId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("Resolving Sacred Boon adds a 3-damage prevention shield to the target creature")
    void resolvingAddsShield() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        Permanent bears = bears(player1);
        assertThat(bears.getDamageToCounterPreventionShield()).isEqualTo(3);
    }

    @Test
    @DisplayName("Prevented noncombat damage becomes +0/+1 counters at the next end step")
    void preventedDamageBecomesCountersAtEndStep() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        // Shock deals 2 damage to the shielded creature — fully prevented.
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, targetId);

        Permanent bears = bears(player1);
        // Prevented, so still alive; 1 of the 3 shield remains; no counters until end step.
        assertThat(bears.getDamageToCounterPreventionShield()).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isZero();

        advanceToEndStep(player1);
        resolveAllTriggers();

        Permanent afterEnd = bears(player1);
        assertThat(afterEnd.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(2);
        // +0/+1 counters add toughness only.
        assertThat(gqs.getEffectiveToughness(gd, afterEnd)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, afterEnd)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevented combat damage becomes +0/+1 counters at the next end step")
    void preventedCombatDamageBecomesCounters() {
        Permanent defender = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, defender.getId());

        defender.setBlocking(true);
        defender.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat();

        // Defender took 2 prevented combat damage → survives, 1 shield remaining.
        Permanent survivor = bears(player2);
        assertThat(survivor.getDamageToCounterPreventionShield()).isEqualTo(1);

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(bears(player2).getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("No counters are added when no damage is prevented")
    void noCountersWhenNoDamage() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        resolveAllTriggers();

        assertThat(bears(player1).getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Only the first three damage are prevented and counted")
    void shieldPreventsOnlyThreeDamage() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.setHand(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.castAndResolveInstant(player2, 0, bear.getId());

        assertThat(bear.getDamageToCounterPreventionShield()).isZero();
        assertThat(bear.getMarkedDamage()).isEqualTo(1);

        advanceToEndStep(player1);
        resolveAllTriggers();

        assertThat(bears(player1).getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("The delayed counter trigger keeps Sacred Boon's controller after the creature changes control")
    void delayedTriggerKeepsOriginalControllerAfterControlChange() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bear.getId());

        assertThat(bear.getDamageToCounterPreventionShield()).isEqualTo(1);

        harness.setHand(player2, List.of(new ControlMagic()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.castEnchantment(player2, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(bear.getId()));

        advanceToEndStep(player2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Separate Sacred Boons create separate delayed counter triggers")
    void separateBoonsCreateSeparateDelayedTriggers() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.setHand(player2, List.of(new SacredBoon()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player2, 0, bear.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bear.getId());

        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Sacred Boon shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.castAndResolveInstant(player1, 0, targetId);
        assertThat(bears(player1).getDamageToCounterPreventionShield()).isEqualTo(3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears(player1).getDamageToCounterPreventionShield()).isZero();
    }

    @Test
    @DisplayName("Sacred Boon cannot target a player")
    void cannotTargetPlayer() {
        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sacred Boon fizzles if its target is no longer a creature")
    void fizzlesWhenTargetStopsBeingCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        UUID targetId = bears.getId();

        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, targetId);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ImprisonedInTheMoon());
        aura.setAttachedTo(targetId);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, bears)).isFalse();
        assertThat(bears.getDamageToCounterPreventionShield()).isZero();
    }

    @Test
    @DisplayName("Unused prevention remains active after the delayed counter trigger resolves")
    void preventionRemainsActiveDuringEndStep() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        advanceToEndStep(player1);
        resolveAllTriggers();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bear.getId());

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bear.getMarkedDamage()).isZero();
        assertThat(bear.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The delayed trigger counts damage prevented in response to it")
    void delayedTriggerCountsDamagePreventedWhileOnStack() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        advanceToEndStep(player1);
        assertThat(gd.stack).hasSize(1);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bear.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bear.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(2);
        assertThat(bear.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Sacred Boon does not create a shield or delayed trigger when its target has died")
    void fizzlesWhenTargetLeavesBattlefield() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, bear.getId());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, bear.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Sacred Boon");
        advanceToEndStep(player1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A Boon cast during the end step keeps its delayed trigger but its shield expires at cleanup")
    void expiredShieldDoesNotConsumeNextTurnsDamage() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SacredBoon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(bear.getDamageToCounterPreventionShield()).isZero();
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new SacredBoon(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.castAndResolveInstant(player2, 0, bear.getId());

        assertThat(bear.getMarkedDamage()).isEqualTo(1);
        advanceToEndStep(player2);
        assertThat(gd.stack).hasSize(2);
        resolveAllTriggers();
        assertThat(bear.getCounterCount(CounterType.PLUS_ZERO_PLUS_ONE)).isEqualTo(3);
    }

    private Permanent bears(Player player) {
        return findPermanent(player, "Grizzly Bears");
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(activePlayer, TurnStep.END_STEP);
    }

}
