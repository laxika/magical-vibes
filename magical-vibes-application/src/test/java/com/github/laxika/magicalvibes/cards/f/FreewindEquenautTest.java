package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AssaultZeppelid;
import com.github.laxika.magicalvibes.cards.s.ShieldingPlax;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FreewindEquenaut.class, AssaultZeppelid.class, ShieldingPlax.class})
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
        Permanent creature = addCreatureReady(player2, new AssaultZeppelid());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    @DisplayName("Can target an attacking creature controlled by its controller")
    void canTargetOwnAttackingCreature() {
        Permanent equenaut = addEquenautWithAura();
        Permanent attacker = addCombatCreature(player1, true, false);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(equenaut.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not gain the ability when the Aura enchants another creature")
    void cannotActivateWhenAuraIsAttachedElsewhere() {
        Permanent equenaut = addCreatureReady(player1, new FreewindEquenaut());
        Permanent otherCreature = addCreatureReady(player1, new AssaultZeppelid());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ShieldingPlax());
        aura.setAttachedTo(otherCreature.getId());
        Permanent attacker = addCombatCreature(player2, true, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        assertThat(equenaut.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Does not deal damage if the target stops attacking before resolution")
    void targetMustStillBeAttackingWhenAbilityResolves() {
        Permanent equenaut = addEquenautWithAura();
        Permanent attacker = addCombatCreature(player2, true, false);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(equenaut.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Loses the ability when its Aura moves to another creature")
    void losesAbilityWhenAuraMovesAway() {
        Permanent equenaut = addEquenautWithAura();
        Permanent otherCreature = addCreatureReady(player1, new AssaultZeppelid());
        findPermanent(player1, "Shielding Plax").setAttachedTo(otherCreature.getId());
        Permanent attacker = addCombatCreature(player2, true, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
        assertThat(equenaut.isTapped()).isFalse();
    }

    @Test
    @DisplayName("An activated ability still resolves after its Aura moves away")
    void activatedAbilitySurvivesLosingAura() {
        Permanent equenaut = addEquenautWithAura();
        Permanent otherCreature = addCreatureReady(player1, new AssaultZeppelid());
        Permanent attacker = addCombatCreature(player2, true, false);

        harness.activateAbility(player1, 0, null, attacker.getId());
        findPermanent(player1, "Shielding Plax").setAttachedTo(otherCreature.getId());
        harness.passBothPriorities();

        assertThat(equenaut.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot activate the granted tap ability while summoning sick")
    void cannotActivateWhileSummoningSick() {
        Permanent equenaut = addEquenautWithAura();
        equenaut.setSummoningSick(true);
        Permanent attacker = addCombatCreature(player2, true, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(equenaut.isTapped()).isFalse();
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot activate the granted tap ability while already tapped")
    void cannotActivateWhileTapped() {
        Permanent equenaut = addEquenautWithAura();
        equenaut.tap();
        Permanent attacker = addCombatCreature(player2, true, false);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    private Permanent addEquenautWithAura() {
        Permanent equenaut = addCreatureReady(player1, new FreewindEquenaut());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ShieldingPlax());
        aura.setAttachedTo(equenaut.getId());
        return equenaut;
    }

    private Permanent addCombatCreature(com.github.laxika.magicalvibes.model.Player player,
                                        boolean attacking, boolean blocking) {
        Permanent creature = addCreatureReady(player, new AssaultZeppelid());
        creature.setAttacking(attacking);
        creature.setBlocking(blocking);
        harness.forceStep(attacking ? TurnStep.DECLARE_ATTACKERS : TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        return creature;
    }
}
