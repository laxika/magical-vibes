package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfVines;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AssaultFormation.class, GrizzlyBears.class, WallOfVines.class})
class AssaultFormationTest extends BaseCardTest {

    @Test
    @DisplayName("Your creatures assign combat damage equal to toughness")
    void yourCreaturesUseToughnessForCombatDamage() {
        addFormation();
        Permanent ownWall = addReadyCreature(player1, new WallOfVines());
        Permanent opponentWall = addReadyCreature(player2, new WallOfVines());

        assertThat(gqs.getEffectiveCombatDamage(gd, ownWall)).isEqualTo(3);
        assertThat(gqs.getEffectiveCombatDamage(gd, opponentWall)).isZero();
    }

    @Test
    @DisplayName("The defender ability lets a target defender attack this turn")
    void targetDefenderCanAttack() {
        Permanent formation = addFormation();
        Permanent wall = addReadyCreature(player1, new WallOfVines());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, formation), 0, null, wall.getId());
        harness.passBothPriorities();

        beginAttackers();
        gs.declareAttackers(gd, player1, List.of(battlefieldIndex(player1, wall)));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The defender ability only targets creatures with defender")
    void defenderAbilityRejectsNonDefender() {
        Permanent formation = addFormation();
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
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
        Permanent ownCreature = addReadyCreature(player1, new GrizzlyBears());
        Permanent opponentCreature = addReadyCreature(player2, new GrizzlyBears());
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

    private Permanent addFormation() {
        return harness.addToBattlefieldAndReturn(player1, new AssaultFormation());
    }

    private Permanent addReadyCreature(com.github.laxika.magicalvibes.model.Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private int battlefieldIndex(com.github.laxika.magicalvibes.model.Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void beginAttackers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(player1.getId()));
    }
}
