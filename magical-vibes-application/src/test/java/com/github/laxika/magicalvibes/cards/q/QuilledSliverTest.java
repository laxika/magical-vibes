package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.b.BonescytheSliver;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({QuilledSliver.class, BonescytheSliver.class, GiantSpider.class, GrizzlyBears.class})
class QuilledSliverTest extends BaseCardTest {

    @Test
    @DisplayName("A Quilled Sliver deals 1 damage to an attacking creature")
    void damagesAttackingCreature() {
        Permanent quilledSliver = addCreatureReady(player1, new QuilledSliver());
        Permanent attacker = addCreatureReady(player2, new GiantSpider());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        prepareDeclareBlockers(player2);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(quilledSliver.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("All Slivers gain the ability and can damage a blocking creature")
    void grantsAbilityToAllSlivers() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        Permanent bonescytheSliver = addCreatureReady(player2, new BonescytheSliver());
        addCreatureReady(player1, new QuilledSliver());

        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        blocker.setBlocking(true);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        blocker.addBlockingTarget(attackerIndex);
        blocker.addBlockingTargetId(attacker.getId());

        prepareDeclareBlockers(player1);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(bonescytheSliver), null, blocker.getId());
        harness.passBothPriorities();

        assertThat(bonescytheSliver.isTapped()).isTrue();
        assertThat(blocker.getMarkedDamage()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void rejectsNonCombatCreature() {
        addCreatureReady(player1, new QuilledSliver());
        Permanent target = addCreatureReady(player2, new GiantSpider());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking or blocking creature");
    }
}
