package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SandbarSerpent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AirElemental.class, ArgothianSwine.class, EliteArchers.class, GrizzlyBears.class, SandbarSerpent.class})
class EliteArchersTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 damage to a target attacking creature, destroying a 3/3")
    void deals3DamageToAttacker() {
        Permanent archers = addCreatureReady(player1, new EliteArchers());
        Permanent attacker = addAttacker(player2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(archers.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player2, "Argothian Swine");
        harness.assertInGraveyard(player2, "Argothian Swine");
    }

    @Test
    @DisplayName("Deals 3 damage to a target blocking creature")
    void deals3DamageToBlocker() {
        addCreatureReady(player1, new EliteArchers());
        Permanent blocker = addBlocker(player2);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Deals exactly 3 damage to a 4/4 attacking creature")
    void dealsExactlyThreeDamage() {
        addReadyArchers(player1);
        Permanent attacker = addAttacker(player2, new AirElemental());

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        addCreatureReady(player1, new EliteArchers());
        Permanent target = addCreatureReady(player2, new SandbarSerpent());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if the target is no longer attacking or blocking at resolution")
    void fizzlesIfTargetLeavesCombatBeforeResolution() {
        addReadyArchers(player1);
        Permanent attacker = addAttacker(player2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attacker);
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    private Permanent addReadyArchers(Player player) {
        return addCreatureReady(player, new EliteArchers());
    }


    private Permanent addAttacker(Player owner) {
        return addAttacker(owner, new ArgothianSwine());
    }

    private Permanent addAttacker(Player owner, Card card) {
        Permanent attacker = addCreatureReady(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner) {
        Permanent blocker = addCreatureReady(owner, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        return blocker;
    }

    @Test
    @DisplayName("Does not damage a creature that stops attacking before resolution")
    void doesNotDamageCreatureThatStopsAttackingBeforeResolution() {
        addCreatureReady(player1, new EliteArchers());
        Permanent attacker = addAttacker(player2);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
