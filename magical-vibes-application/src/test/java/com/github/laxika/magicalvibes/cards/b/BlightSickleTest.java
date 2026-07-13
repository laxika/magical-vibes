package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BlightSickleTest extends BaseCardTest {

    // ===== Static: +1/+0 and wither =====

    @Test
    @DisplayName("Equipped creature gets +1/+0 and wither")
    void equippedCreatureGetsBoostAndWither() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent sickle = addReady(player1, new BlightSickle());
        sickle.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.WITHER)).isTrue();
    }

    @Test
    @DisplayName("Creature loses the bonuses when the Sickle is removed")
    void creatureLosesBonusesWhenSickleRemoved() {
        Permanent creature = addReady(player1, new GrizzlyBears());
        Permanent sickle = addReady(player1, new BlightSickle());
        sickle.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(sickle);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.WITHER)).isFalse();
    }

    // ===== Equip {2} =====

    @Test
    @DisplayName("Resolving equip attaches the Sickle to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent sickle = addReady(player1, new BlightSickle());
        Permanent creature = addReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sickle.getAttachedTo()).isEqualTo(creature.getId());
    }

    // ===== Behavior: granted wither deals combat damage as -1/-1 counters =====

    @Test
    @DisplayName("Equipped creature deals combat damage to a blocker as -1/-1 counters")
    void witherDealsMinusCountersToBlocker() {
        Permanent attacker = addReady(player1, new GrizzlyBears()); // 2/2 → 3/2 with the Sickle
        Permanent sickle = addReady(player1, new BlightSickle());
        sickle.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        Permanent blocker = addReady(player2, new GiantSpider()); // 2/4, survives
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // 3 power dealt as -1/-1 counters rather than marked damage.
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    // ===== Helpers =====

    private Permanent addReady(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
