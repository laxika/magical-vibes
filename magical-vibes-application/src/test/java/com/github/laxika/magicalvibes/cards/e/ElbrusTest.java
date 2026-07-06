package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WithengarUnbound;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ElbrusTest extends BaseCardTest {

    // ===== Card structure =====

    

    

    @Test
    @DisplayName("Has equip {1} ability")
    void hasEquipAbility() {
        Elbrus card = new Elbrus();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().getFirst().getManaCost()).isEqualTo("{1}");
        assertThat(card.getActivatedAbilities().getFirst().getEffects().getFirst())
                .isInstanceOf(EquipEffect.class);
    }

    

    // ===== Static effect =====

    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addReadyCreature(player1);
        Permanent elbrus = addElbrusReady(player1);
        elbrus.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);    // 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    // ===== Combat damage: transform trigger =====

    @Nested
    @DisplayName("Combat damage transform trigger")
    class CombatDamageTransform {

        @Test
        @DisplayName("Equipped creature dealing combat damage transforms Elbrus into Withengar Unbound")
        void combatDamageTransformsIntoWithengar() {
            Permanent creature = addReadyCreature(player1);
            Permanent elbrus = addElbrusReady(player1);
            elbrus.setAttachedTo(creature.getId());
            creature.setAttacking(true);

            resolveCombat();
            harness.passBothPriorities(); // resolve mandatory transform trigger

            assertThat(elbrus.isTransformed()).isTrue();
            assertThat(elbrus.getCard().getName()).isEqualTo("Withengar Unbound");
            assertThat(elbrus.getCard()).isInstanceOf(WithengarUnbound.class);
        }

        @Test
        @DisplayName("Transforming unattaches Elbrus from the creature")
        void transformUnattachesElbrus() {
            Permanent creature = addReadyCreature(player1);
            Permanent elbrus = addElbrusReady(player1);
            elbrus.setAttachedTo(creature.getId());
            creature.setAttacking(true);

            resolveCombat();
            harness.passBothPriorities(); // resolve mandatory transform trigger

            assertThat(elbrus.isAttached()).isFalse();
            // The creature no longer gets the +1/+0 boost.
            assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        }

        @Test
        @DisplayName("No transform when equipped creature is blocked and deals no player damage")
        void noTransformWhenBlocked() {
            Permanent creature = addReadyCreature(player1);
            creature.getCard().setToughness(10); // survives the blocker so the test isolates the "blocked" case
            Permanent elbrus = addElbrusReady(player1);
            elbrus.setAttachedTo(creature.getId());
            creature.setAttacking(true);

            Permanent blocker = addReadyCreature(player2);
            blocker.getCard().setToughness(10);
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            resolveCombat();

            assertThat(elbrus.isTransformed()).isFalse();
            assertThat(elbrus.getCard().getName()).isEqualTo("Elbrus, the Binding Blade");
            assertThat(elbrus.isAttached()).isTrue();
        }
    }

    // ===== Helpers =====

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addElbrusReady(Player player) {
        Permanent perm = new Permanent(new Elbrus());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
