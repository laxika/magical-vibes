package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WalkingBulwark.class})
class WalkingBulwarkTest extends BaseCardTest {

    @Test
    @DisplayName("The ability grants haste, attack permission, and toughness-based combat damage")
    @CardUsed({WallOfVines.class})
    void grantsAllAbilitiesAndAllowsImmediateAttack() {
        Permanent bulwark = addCreatureReady(player1, new WalkingBulwark());
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new WallOfVines());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, bulwark), 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectiveCombatDamage(gd, wall)).isEqualTo(3);

        declareAttackers(List.of(battlefieldIndex(player1, wall)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The granted abilities expire at end of turn")
    @CardUsed({WallOfVines.class})
    void grantedAbilitiesExpireAtEndOfTurn() {
        Permanent bulwark = addCreatureReady(player1, new WalkingBulwark());
        Permanent wall = addCreatureReady(player1, new WallOfVines());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, bulwark), 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectiveCombatDamage(gd, wall)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wall, Keyword.HASTE)).isFalse();
        assertThat(gqs.getEffectiveCombatDamage(gd, wall)).isZero();
    }

    @Test
    @DisplayName("The ability only targets creatures with defender")
    @CardUsed({GrizzlyBears.class})
    void onlyTargetsDefenderCreatures() {
        Permanent bulwark = addCreatureReady(player1, new WalkingBulwark());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, bulwark), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with defender");
    }

    @Test
    @DisplayName("The ability can only be activated at sorcery speed")
    @CardUsed({WallOfVines.class})
    void onlyActivatesAtSorcerySpeed() {
        Permanent bulwark = addCreatureReady(player1, new WalkingBulwark());
        Permanent wall = addCreatureReady(player1, new WallOfVines());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, bulwark), 0, null, wall.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("A summoning-sick Bulwark can activate targeting itself and attack")
    void canActivateAndTargetItselfImmediately() {
        Permanent bulwark = harness.addToBattlefieldAndReturn(player1, new WalkingBulwark());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, bulwark.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bulwark, Keyword.DEFENDER)).isTrue();
        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("An opposing defender can be targeted and uses toughness when blocking")
    void canTargetOpposingDefender() {
        Permanent bulwark = addCreatureReady(player1, new WalkingBulwark());
        Permanent opposing = addCreatureReady(player2, new WalkingBulwark());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, opposing.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, 0, null, bulwark.getId());
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Walking Bulwark");
        harness.assertInGraveyard(player2, "Walking Bulwark");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Removing the source before resolution does not stop its ability")
    void abilityResolvesAfterSourceLeaves() {
        Permanent bulwark = addCreatureReady(player1, new WalkingBulwark());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new WalkingBulwark());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bulwark));
        harness.passBothPriorities();

        declareAttackers(List.of(battlefieldIndex(player1, target)));
        resolveCombat();

        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("The ability cannot be activated while another activation is on the stack")
    void cannotActivateWithNonemptyStack() {
        Permanent bulwark = addCreatureReady(player1, new WalkingBulwark());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, bulwark.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bulwark.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("stack is empty");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Attack permission expires at end of turn along with the other grants")
    void attackPermissionExpires() {
        Permanent bulwark = addCreatureReady(player1, new WalkingBulwark());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 0, null, bulwark.getId());
        harness.passBothPriorities();

        assertThat(als.canAttack(gd, bulwark, player1.getId())).isTrue();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        assertThat(als.canAttack(gd, bulwark, player1.getId())).isFalse();
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
