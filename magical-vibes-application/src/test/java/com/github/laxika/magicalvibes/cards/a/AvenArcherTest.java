package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenArcher.class, Plains.class, WoodlandDruid.class})
class AvenArcherTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to an attacking creature")
    void dealsDamageToAttackingCreature() {
        Permanent archer = addReadyArcher(player1);
        Permanent attacker = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(archer.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player2, "Woodland Druid");
        harness.assertInGraveyard(player2, "Woodland Druid");
    }

    @Test
    @DisplayName("Deals 2 damage to a blocking creature")
    void dealsDamageToBlockingCreature() {
        addReadyArcher(player1);
        Permanent blocker = addBlockingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Woodland Druid");
        harness.assertInGraveyard(player2, "Woodland Druid");
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        addReadyArcher(player1);
        Permanent bystander = addCreatureReady(player2, new WoodlandDruid());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    @DisplayName("Can target an attacking creature controlled by its controller")
    void canTargetOwnAttackingCreature() {
        addCreatureReady(player1, new AvenArcher());
        Permanent attacker = addAttackingCreature(player1);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Woodland Druid");
    }

    @Test
    @DisplayName("Does not damage a target that stops attacking before resolution")
    void doesNotDamageTargetThatStopsAttackingBeforeResolution() {
        addCreatureReady(player1, new AvenArcher());
        Permanent attacker = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Woodland Druid");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        addCreatureReady(player1, new AvenArcher());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot activate while Aven Archer has summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefieldAndReturn(player1, new AvenArcher());
        Permanent attacker = addAttackingCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sick");
    }

    private Permanent addReadyArcher(Player player) {
        return addCreatureReady(player, new AvenArcher());
    }

    private Permanent addAttackingCreature(Player player) {
        Permanent creature = addCreatureReady(player, new WoodlandDruid());
        creature.setAttacking(true);
        return creature;
    }

    private Permanent addBlockingCreature(Player player) {
        Permanent creature = addCreatureReady(player, new WoodlandDruid());
        creature.setBlocking(true);
        return creature;
    }
}
