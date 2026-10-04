package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.ConcordiaPegasus;
import com.github.laxika.magicalvibes.cards.w.WallOfVines;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HighAlert.class, AngelicWall.class, GrizzlyBears.class, WallOfVines.class, ConcordiaPegasus.class})
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

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1,
                        List.of(gd.playerBattlefields.get(player1.getId()).indexOf(wall))));

        assertThat(wall.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("High Alert does not grant the defender permission to an opponent's creatures")
    void doesNotGrantOpponentDefenderPermission() {
        harness.addToBattlefield(player1, new HighAlert());
        Permanent wall = addCreatureReady(player2, new AngelicWall());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThatThrownBy(() -> declareAttackers(player2,
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

    @Test
    @DisplayName("An unblocked creature actually deals combat damage equal to toughness")
    void dealsToughnessInCombat() {
        harness.addToBattlefield(player1, new HighAlert());
        Permanent attacker = addCreatureReady(player1, new ConcordiaPegasus());
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Removing High Alert ends both static effects")
    void staticEffectsEndWhenHighAlertLeaves() {
        Permanent alert = harness.addToBattlefieldAndReturn(player1, new HighAlert());
        Permanent wall = addCreatureReady(player1, new AngelicWall());
        assertThat(gqs.getEffectiveCombatDamage(gd, wall)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(alert);

        assertThat(gqs.getEffectiveCombatDamage(gd, wall)).isZero();
        assertThatThrownBy(() -> declareAttackers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(wall))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("An already untapped creature is a legal target")
    void canTargetUntappedCreature() {
        harness.addToBattlefield(player1, new HighAlert());
        Permanent target = addCreatureReady(player1, new ConcordiaPegasus());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Defender permission does not override summoning sickness")
    void doesNotAllowSummoningSickDefenderToAttack() {
        harness.addToBattlefield(player1, new HighAlert());
        Permanent wall = harness.addToBattlefieldAndReturn(player1, new AngelicWall());
        wall.setSummoningSick(true);

        assertThatThrownBy(() -> declareAttackers(player1,
                List.of(gd.playerBattlefields.get(player1.getId()).indexOf(wall))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    private Permanent addTappedCreature(Player player) {
        Permanent permanent = addCreatureReady(player, new GrizzlyBears());
        permanent.tap();
        return permanent;
    }

}
