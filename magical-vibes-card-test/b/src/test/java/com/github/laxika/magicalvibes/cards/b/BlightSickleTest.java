package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BlightSickle.class, GrizzlyBears.class, GiantSpider.class, ProdigalPyromancer.class})
class BlightSickleTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0 and wither")
    void equippedCreatureGetsBoostAndWither() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sickle = addCreatureReady(player1, new BlightSickle());
        sickle.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.WITHER)).isTrue();
    }

    @Test
    @DisplayName("Creature loses the bonuses when the Sickle is removed")
    void creatureLosesBonusesWhenSickleRemoved() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent sickle = addCreatureReady(player1, new BlightSickle());
        sickle.setAttachedTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(sickle);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.WITHER)).isFalse();
    }

    @Test
    @DisplayName("Resolving equip attaches the Sickle to target creature")
    void resolvingEquipAttachesToCreature() {
        Permanent sickle = addCreatureReady(player1, new BlightSickle());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(sickle.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature deals combat damage to a blocker as -1/-1 counters")
    void witherDealsMinusCountersToBlocker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears()); // 2/2 → 3/2 with the Sickle
        Permanent sickle = addCreatureReady(player1, new BlightSickle());
        sickle.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GiantSpider()); // 2/4, survives
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // 3 power dealt as -1/-1 counters rather than marked damage.
        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        assertThat(blocker.getMarkedDamage()).isEqualTo(0);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(blocker);
    }

    @Test
    @DisplayName("Moving the Sickle transfers both bonuses only when equip resolves")
    void movingSickleTransfersBonusesOnResolution() {
        Permanent sickle = addCreatureReady(player1, new BlightSickle());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        sickle.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, second.getId());

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(sickle.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, second, Keyword.WITHER)).isFalse();

        harness.passBothPriorities();

        assertThat(sickle.getAttachedTo()).isEqualTo(second.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, first, Keyword.WITHER)).isFalse();
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, second, Keyword.WITHER)).isTrue();
    }

    @Test
    @DisplayName("An illegal equip target leaves the previous attachment intact")
    void equipTargetLeavingDoesNotDetachSickle() {
        Permanent sickle = addCreatureReady(player1, new BlightSickle());
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        sickle.setAttachedTo(first.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, second.getId());
        gd.playerBattlefields.get(player1.getId()).remove(second);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(sickle.getAttachedTo()).isEqualTo(first.getId());
        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, first, Keyword.WITHER)).isTrue();
    }

    @Test
    @DisplayName("Equip cannot target an opponent's creature")
    void cannotEquipOpponentsCreature() {
        Permanent sickle = addCreatureReady(player1, new BlightSickle());
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(sickle.getAttachedTo()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void cannotEquipDuringCombat() {
        addCreatureReady(player1, new BlightSickle());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    @Test
    @DisplayName("Wither damage to a player causes ordinary life loss")
    void witherDamageToPlayerCausesLifeLoss() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent sickle = addCreatureReady(player1, new BlightSickle());
        sickle.setAttachedTo(attacker.getId());
        attacker.setAttacking(true);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Granted wither also applies to noncombat damage")
    void witherAppliesToNoncombatDamage() {
        Permanent pyromancer = addCreatureReady(player1, new ProdigalPyromancer());
        Permanent sickle = addCreatureReady(player1, new BlightSickle());
        Permanent target = addCreatureReady(player2, new GiantSpider());
        sickle.setAttachedTo(pyromancer.getId());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }
}
