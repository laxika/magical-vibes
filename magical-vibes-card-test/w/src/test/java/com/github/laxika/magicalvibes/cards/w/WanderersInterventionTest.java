package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GreaterTanuki;
import com.github.laxika.magicalvibes.cards.s.SunbladeSamurai;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WanderersIntervention.class, GreaterTanuki.class, SunbladeSamurai.class})
class WanderersInterventionTest extends BaseCardTest {

    @Test
    void dealsFourDamageToAnAttackingCreature() {
        Permanent target = addAttacker(player2, new GreaterTanuki());

        castAndResolve(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void dealsFourDamageToABlockingCreature() {
        Permanent target = addBlocker(player2, new GreaterTanuki());

        castAndResolve(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void cannotTargetANonCombatCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GreaterTanuki());
        prepareCast();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking creature");
    }

    @Test
    void destroysACreatureWithFourToughness() {
        Permanent target = addAttacker(player2, new SunbladeSamurai());

        castAndResolve(target);

        harness.assertNotOnBattlefield(player2, "Sunblade Samurai");
        harness.assertInGraveyard(player2, "Sunblade Samurai");
    }

    @Test
    void canTargetAnAttackingCreatureYouControl() {
        Permanent target = addAttacker(player1, new GreaterTanuki());
        target.setAttackTarget(player2.getId());

        castAndResolve(target);

        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void doesNotDealDamageIfTargetStopsAttackingBeforeResolution() {
        Permanent target = addAttacker(player2, new GreaterTanuki());
        prepareCast();
        harness.castInstant(player1, 0, target.getId());
        target.clearCombatState();

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Wanderer's Intervention");
    }

    @Test
    void doesNotDealDamageIfTargetStopsBlockingBeforeResolution() {
        Permanent target = addBlocker(player2, new GreaterTanuki());
        prepareCast();
        harness.castInstant(player1, 0, target.getId());
        target.clearCombatState();

        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Wanderer's Intervention");
    }

    private void castAndResolve(Permanent target) {
        prepareCast();
        harness.castAndResolveInstant(player1, 0, target.getId());
    }

    private void prepareCast() {
        harness.setHand(player1, List.of(new WanderersIntervention()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private Permanent addAttacker(Player owner, com.github.laxika.magicalvibes.model.Card card) {
        Permanent target = harness.addToBattlefieldAndReturn(owner, card);
        target.setSummoningSick(false);
        target.setAttacking(true);
        target.setAttackTarget(player1.getId());
        return target;
    }

    private Permanent addBlocker(Player owner, com.github.laxika.magicalvibes.model.Card card) {
        Permanent target = harness.addToBattlefieldAndReturn(owner, card);
        target.setSummoningSick(false);
        target.setBlocking(true);
        target.addBlockingTargetId(UUID.randomUUID());
        return target;
    }
}
