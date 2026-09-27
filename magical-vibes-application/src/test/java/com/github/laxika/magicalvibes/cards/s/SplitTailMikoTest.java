package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FirstVolley;
import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.cards.g.GodsEyeGateToTheReikai;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SplitTailMiko.class, FirstVolley.class, GnarledMass.class, GodsEyeGateToTheReikai.class})
class SplitTailMikoTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 2 damage dealt to a target creature")
    void preventsNextDamageToCreature() {
        Permanent miko = addCreatureReady(player1, new SplitTailMiko());
        Permanent target = addCreatureReady(player1, new GnarledMass());
        Permanent attacker = addCreatureReady(player2, new GnarledMass());
        activateMiko(miko, target.getId());

        int attackerIndex = indexOf(player2, attacker);
        declareAttackersAndPrepareBlockers(player2, List.of(attackerIndex));
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(indexOf(player1, target), attackerIndex)));
        resolveCombat(player2);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(target.getDamagePreventionShield()).isZero();
        harness.assertOnBattlefield(player1, "Gnarled Mass");
        harness.assertNotOnBattlefield(player2, "Gnarled Mass");
    }

    @Test
    @DisplayName("Prevents the next 2 damage dealt to a target player")
    void preventsNextDamageToPlayer() {
        Permanent miko = addCreatureReady(player1, new SplitTailMiko());
        Permanent attacker = addCreatureReady(player1, new GnarledMass());
        harness.setLife(player2, 20);
        activateMiko(miko, player2.getId());

        declareAttackers(List.of(indexOf(player1, attacker)));
        resolveCombat();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new SplitTailMiko());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new GodsEyeGateToTheReikai());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevents the next 2 damage from noncombat sources")
    void preventsNextTwoNoncombatDamage() {
        Permanent miko = addCreatureReady(player1, new SplitTailMiko());
        Permanent target = addCreatureReady(player2, new GnarledMass());
        harness.setLife(player2, 20);
        activateMiko(miko, target.getId());

        harness.setHand(player1, List.of(new FirstVolley(), new FirstVolley()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getDamagePreventionShield()).isEqualTo(1);
        harness.assertLife(player2, 19);

        harness.castAndResolveInstant(player1, 0, target.getId());
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getDamagePreventionShield()).isZero();
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Prevention applies only to the chosen target")
    void preventsDamageOnlyToChosenTarget() {
        Permanent miko = addCreatureReady(player1, new SplitTailMiko());
        Permanent chosen = addCreatureReady(player2, new GnarledMass());
        Permanent other = addCreatureReady(player2, new GnarledMass());
        harness.setLife(player2, 20);
        activateMiko(miko, chosen.getId());

        harness.setHand(player1, List.of(new FirstVolley()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, other.getId());

        assertThat(chosen.getDamagePreventionShield()).isEqualTo(2);
        assertThat(chosen.getMarkedDamage()).isZero();
        assertThat(other.getMarkedDamage()).isEqualTo(1);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Prevention shield expires at end of turn")
    void preventionExpiresAtEndOfTurn() {
        Permanent miko = addCreatureReady(player1, new SplitTailMiko());
        Permanent target = addCreatureReady(player1, new GnarledMass());
        activateMiko(miko, target.getId());

        assertThat(target.getDamagePreventionShield()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isZero();
    }

    private void activateMiko(Permanent miko, UUID targetId) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, indexOf(player1, miko), null, targetId);
        harness.passBothPriorities();

        assertThat(miko.isTapped()).isTrue();
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
