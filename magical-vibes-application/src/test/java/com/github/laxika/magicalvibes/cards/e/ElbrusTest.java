package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DawntreaderElk;
import com.github.laxika.magicalvibes.cards.h.HeadlessSkaab;
import com.github.laxika.magicalvibes.cards.s.StrionicResonator;
import com.github.laxika.magicalvibes.cards.w.WithengarUnbound;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Elbrus.class, DawntreaderElk.class, HeadlessSkaab.class})
class ElbrusTest extends BaseCardTest {

    @Test
    @DisplayName("Has equip {1} ability")
    void hasEquipAbility() {
        Permanent elbrus = harness.addToBattlefieldAndReturn(player1, new Elbrus());
        Permanent creature = addCreatureReady(player1, new DawntreaderElk());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(elbrus.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Equipped creature gets +1/+0")
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new DawntreaderElk());
        Permanent elbrus = harness.addToBattlefieldAndReturn(player1, new Elbrus());
        elbrus.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);    // 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2); // 2 + 0
    }

    @Nested
    @CardUsed({Elbrus.class, DawntreaderElk.class, HeadlessSkaab.class})
    @DisplayName("Combat damage transform trigger")
    class CombatDamageTransform {

        @Test
        @DisplayName("Equipped creature dealing combat damage transforms Elbrus into Withengar Unbound")
        void combatDamageTransformsIntoWithengar() {
            Permanent creature = addCreatureReady(player1, new DawntreaderElk());
            Permanent elbrus = harness.addToBattlefieldAndReturn(player1, new Elbrus());
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
            Permanent creature = addCreatureReady(player1, new DawntreaderElk());
            Permanent elbrus = harness.addToBattlefieldAndReturn(player1, new Elbrus());
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
            Permanent creature = addCreatureReady(player1, new HeadlessSkaab());
            Permanent elbrus = harness.addToBattlefieldAndReturn(player1, new Elbrus());
            elbrus.setAttachedTo(creature.getId());
            creature.setAttacking(true);

            Permanent blocker = addCreatureReady(player2, new HeadlessSkaab());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            resolveCombat();

            assertThat(elbrus.isTransformed()).isFalse();
            assertThat(elbrus.getCard().getName()).isEqualTo("Elbrus, the Binding Blade");
            assertThat(elbrus.isAttached()).isTrue();
        }
    }

    @Test
    @CardUsed({Elbrus.class, DawntreaderElk.class, StrionicResonator.class})
    @DisplayName("A copied transform trigger does not transform Withengar back into Elbrus")
    void copiedTriggerDoesNotTransformBack() {
        Permanent creature = addCreatureReady(player1, new DawntreaderElk());
        Permanent elbrus = harness.addToBattlefieldAndReturn(player1, new Elbrus());
        harness.addToBattlefield(player1, new StrionicResonator());
        elbrus.setAttachedTo(creature.getId());
        creature.setAttacking(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        resolveCombat();
        harness.activateAbility(player1, 2, null, gd.stack.getLast().getTargetableId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(elbrus.isTransformed()).isTrue();
        harness.passBothPriorities();

        assertThat(elbrus.isTransformed()).isTrue();
        assertThat(elbrus.getCard()).isInstanceOf(WithengarUnbound.class);
        assertThat(elbrus.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Elbrus still transforms if it becomes unattached after its ability triggers")
    void transformsAfterBecomingUnattached() {
        Permanent creature = addCreatureReady(player1, new DawntreaderElk());
        Permanent elbrus = harness.addToBattlefieldAndReturn(player1, new Elbrus());
        elbrus.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        elbrus.setAttachedTo(null);
        harness.passBothPriorities();

        assertThat(elbrus.isTransformed()).isTrue();
        assertThat(elbrus.isAttached()).isFalse();
    }

    @Test
    @DisplayName("Elbrus triggers when a creature controlled by its opponent deals combat damage")
    void triggersForOpponentsEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new DawntreaderElk());
        Permanent elbrus = harness.addToBattlefieldAndReturn(player2, new Elbrus());
        elbrus.setAttachedTo(creature.getId());
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(elbrus.isTransformed()).isTrue();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(elbrus);
    }
}
