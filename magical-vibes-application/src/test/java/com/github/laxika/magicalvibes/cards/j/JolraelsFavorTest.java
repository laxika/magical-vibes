package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JolraelsFavor.class, GrizzlyBears.class, Mountain.class})
class JolraelsFavorTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Jolrael's Favor attaches it to the target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new JolraelsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> bears.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Jolrael's Favor can be cast during an opponent's turn because it has flash")
    void canBeCastDuringOpponentsTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new JolraelsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.passPriority(player2);
        harness.castEnchantment(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Activating Jolrael's Favor grants a regeneration shield to the enchanted creature")
    void activatingAbilityGrantsShieldToEnchantedCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new JolraelsFavor());
        aura.setAttachedTo(bears.getId());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
        assertThat(aura.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("Jolrael's Favor cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new JolraelsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void canEnchantAndRegenerateOpponentsCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new JolraelsFavor()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        Permanent aura = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
        assertThat(aura.getRegenerationShield()).isZero();
    }

    @Test
    void repeatedActivationsCreateIndependentShieldsWithoutTappingAura() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new JolraelsFavor());
        aura.setAttachedTo(bears.getId());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(2);
        assertThat(bears.isTapped()).isFalse();
        assertThat(aura.isTapped()).isFalse();
    }

    @Test
    void shieldPreventsLethalDamageOnceAndClearsDamage() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new JolraelsFavor());
        aura.setAttachedTo(bears.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        bears.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears, aura);
        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(bears.getRegenerationShield()).isZero();
        assertThat(bears.isTapped()).isTrue();
        assertThat(aura.getAttachedTo()).isEqualTo(bears.getId());

        bears.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears, aura);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears.getCard(), aura.getCard());
    }

    @Test
    void abilityStillRegeneratesCreatureAfterAuraLeavesBattlefield() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new JolraelsFavor());
        aura.setAttachedTo(bears.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, aura));
        harness.passBothPriorities();

        assertThat(bears.getRegenerationShield()).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).contains(aura.getCard());
    }
}
