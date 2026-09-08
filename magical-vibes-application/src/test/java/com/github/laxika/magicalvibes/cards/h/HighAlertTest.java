package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfVines;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HighAlertTest extends BaseCardTest {

    @Test
    @DisplayName("Your creatures assign combat damage equal to toughness")
    void yourCreaturesUseToughnessForCombatDamage() {
        harness.addToBattlefield(player1, new HighAlert());
        Permanent ownCreature = addCreatureReady(player1, new WallOfVines());
        Permanent opponentCreature = addCreatureReady(player2, new WallOfVines());

        assertThat(gqs.getEffectiveCombatDamage(gd, ownCreature)).isEqualTo(3);
        assertThat(gqs.getEffectiveCombatDamage(gd, opponentCreature)).isZero();
    }

    @Test
    @DisplayName("Your creatures can attack as though they don't have defender")
    void yourCreaturesCanAttackWithoutDefender() {
        harness.addToBattlefield(player1, new HighAlert());
        Permanent wall = addCreatureReady(player1, new AngelicWall());
        harness.addToBattlefield(player2, new GrizzlyBears());

        beginAttackers(player1);
        gs.declareAttackers(gd, player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(wall)));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("High Alert does not grant the defender permission to an opponent's creatures")
    void doesNotGrantOpponentDefenderPermission() {
        harness.addToBattlefield(player1, new HighAlert());
        Permanent wall = addCreatureReady(player2, new AngelicWall());
        harness.addToBattlefield(player1, new GrizzlyBears());

        beginAttackers(player2);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player2,
                List.of(gd.playerBattlefields.get(player2.getId()).indexOf(wall))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The activated ability untaps target creature")
    void untapsTargetCreature() {
        harness.addToBattlefield(player1, new HighAlert());
        Permanent target = addTappedCreature(player2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The activated ability only targets creatures")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new HighAlert());
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new HighAlert());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addTappedCreature(Player player) {
        Permanent permanent = addCreatureReady(player, new GrizzlyBears());
        permanent.tap();
        return permanent;
    }

    private void beginAttackers(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginInteraction(new PendingInteraction.AttackerDeclaration(activePlayer.getId()));
    }
}
