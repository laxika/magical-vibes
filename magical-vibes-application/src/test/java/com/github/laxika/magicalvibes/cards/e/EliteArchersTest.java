package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.ArgothianSwine;
import com.github.laxika.magicalvibes.cards.s.SandbarSerpent;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EliteArchers.class, ArgothianSwine.class, SandbarSerpent.class})
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
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        addCreatureReady(player1, new EliteArchers());
        Permanent target = addCreatureReady(player2, new SandbarSerpent());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
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

    private Permanent addAttacker(Player owner) {
        Permanent attacker = addCreatureReady(owner, new ArgothianSwine());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner) {
        Permanent blocker = addCreatureReady(owner, new SandbarSerpent());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(UUID.randomUUID());
        return blocker;
    }
}
