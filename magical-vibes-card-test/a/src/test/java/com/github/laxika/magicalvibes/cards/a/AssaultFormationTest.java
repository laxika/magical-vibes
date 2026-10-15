package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfVines;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.cards.s.SongOfTheDryads;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AssaultFormation.class, GrizzlyBears.class, WallOfVines.class, SongOfTheDryads.class})
class AssaultFormationTest extends BaseCardTest {

    @Test
    @DisplayName("Your creatures assign combat damage equal to toughness")
    void yourCreaturesUseToughnessForCombatDamage() {
        addFormation();
        Permanent ownWall = addCreatureReady(player1, new WallOfVines());
        Permanent opponentWall = addCreatureReady(player2, new WallOfVines());

        assertThat(gqs.getEffectiveCombatDamage(gd, ownWall)).isEqualTo(3);
        assertThat(gqs.getEffectiveCombatDamage(gd, opponentWall)).isZero();
    }

    @Test
    @DisplayName("The defender ability lets a target defender attack this turn")
    void targetDefenderCanAttack() {
        Permanent formation = addFormation();
        Permanent wall = addCreatureReady(player1, new WallOfVines());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, formation), 0, null, wall.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(battlefieldIndex(player1, wall)));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The defender ability only targets creatures with defender")
    void defenderAbilityRejectsNonDefender() {
        Permanent formation = addFormation();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, formation), 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature with defender");
    }

    @Test
    @DisplayName("The pump boosts your creatures and wears off at end of turn")
    void pumpBoostsOwnCreaturesUntilEndOfTurn() {
        Permanent formation = addFormation();
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(player1, formation), 1, null, null);
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(opponentCreature.getEffectiveToughness()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat uses toughness even when power is greater")
    void combatUsesToughnessWhenPowerIsGreater() {
        addFormation();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setPowerModifier(3);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(battlefieldIndex(player1, bears))));

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
    }

    @Test
    @DisplayName("The defender permission can target an opponent's creature and expires")
    void opposingDefenderPermissionExpires() {
        Permanent formation = addFormation();
        Permanent wall = addCreatureReady(player2, new WallOfVines());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, formation), 0, null, wall.getId());
        harness.passBothPriorities();

        assertThat(harness.getAttackLegalityService().canAttack(gd, wall, player2.getId())).isTrue();
        assertThat(gqs.hasKeyword(gd, wall, com.github.laxika.magicalvibes.model.Keyword.DEFENDER)).isTrue();
        gd.playerBattlefields.get(player1.getId()).remove(formation);
        assertThat(harness.getAttackLegalityService().canAttack(gd, wall, player2.getId())).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(harness.getAttackLegalityService().canAttack(gd, wall, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("Repeated pumps stack and exclude creatures entering after resolution")
    void pumpsStackOnlyOnCreaturesPresentAtResolution() {
        Permanent formation = addFormation();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, battlefieldIndex(player1, formation), 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, battlefieldIndex(player1, formation), 1, null, null);
        harness.passBothPriorities();
        Permanent lateBears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveCombatDamage(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lateBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing Assault Formation restores power-based combat damage")
    void removingFormationRestoresPowerBasedDamage() {
        Permanent formation = addFormation();
        Permanent wall = addCreatureReady(player1, new WallOfVines());
        assertThat(gqs.getEffectiveCombatDamage(gd, wall)).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).remove(formation);

        assertThat(gqs.getEffectiveCombatDamage(gd, wall)).isZero();
    }

    @Test
    @DisplayName("Song of the Dryads removes the toughness-based combat ability")
    void becomingForestStopsToughnessBasedCombatDamage() {
        Permanent formation = addFormation();
        Permanent wall = addCreatureReady(player1, new WallOfVines());
        harness.setHand(player2, List.of(new SongOfTheDryads()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0, formation.getId());
        harness.passBothPriorities();

        assertThat(gqs.isLand(gd, formation)).isTrue();
        assertThat(gqs.getEffectiveCombatDamage(gd, wall)).isZero();
    }

    private Permanent addFormation() {
        return harness.addToBattlefieldAndReturn(player1, new AssaultFormation());
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

}
