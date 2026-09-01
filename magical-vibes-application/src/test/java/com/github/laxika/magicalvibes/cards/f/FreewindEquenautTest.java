package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FreewindEquenaut.class, GrizzlyBears.class, Pacifism.class})
class FreewindEquenautTest extends BaseCardTest {

    @Test
    @DisplayName("While enchanted, taps to deal 2 damage to an attacking creature")
    void whileEnchantedDealsDamageToAttacker() {
        Permanent equenaut = addEquenautWithAura();
        Permanent attacker = addCombatCreature(player2, true, false);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(equenaut.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("While enchanted, taps to deal 2 damage to a blocking creature")
    void whileEnchantedDealsDamageToBlocker() {
        Permanent equenaut = addEquenautWithAura();
        Permanent blocker = addCombatCreature(player2, false, true);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(equenaut.isTapped()).isTrue();
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate without an Aura attached")
    void cannotActivateWithoutAura() {
        Permanent equenaut = harness.addToBattlefieldAndReturn(player1, new FreewindEquenaut());
        Permanent attacker = addCombatCreature(player2, true, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        assertThat(equenaut.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        addEquenautWithAura();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    private Permanent addEquenautWithAura() {
        Permanent equenaut = harness.addToBattlefieldAndReturn(player1, new FreewindEquenaut());
        equenaut.setSummoningSick(false);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(equenaut.getId());
        return equenaut;
    }

    private Permanent addCombatCreature(com.github.laxika.magicalvibes.model.Player player,
                                        boolean attacking, boolean blocking) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        creature.setAttacking(attacking);
        creature.setBlocking(blocking);
        harness.forceStep(attacking ? TurnStep.DECLARE_ATTACKERS : TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        return creature;
    }
}
