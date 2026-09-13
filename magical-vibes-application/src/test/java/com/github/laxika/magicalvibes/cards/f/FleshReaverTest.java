package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.h.HollowDogs;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.w.WindingWurm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FleshReaver.class, HollowDogs.class, LilianaVess.class,
        ProdigalPyromancer.class, WindingWurm.class})
class FleshReaverTest extends BaseCardTest {

    @Test
    @DisplayName("Deals the combat damage back to its controller when it damages an opponent")
    void dealsCombatDamageBackToController() {
        Permanent reaver = addCreatureReady(player1, new FleshReaver());
        reaver.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Deals the combat damage back to its controller when it damages a creature")
    void dealsCombatDamageBackToControllerWhenDamagingCreature() {
        Permanent reaver = addCreatureReady(player1, new FleshReaver());
        reaver.setAttacking(true);
        harness.addToBattlefield(player2, new HollowDogs());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Hollow Dogs");
    }

    @Test
    @DisplayName("Still deals damage to its controller when it dies after damaging a creature")
    void triggersWhenItDiesAfterDamagingCreature() {
        Permanent reaver = addCreatureReady(player1, new FleshReaver());
        reaver.setAttacking(true);
        addCreatureReady(player2, new WindingWurm());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.assertInGraveyard(player1, "Flesh Reaver");
    }

    @Test
    @DisplayName("Triggers for noncombat damage to an opponent")
    void triggersForNoncombatDamageToOpponent() {
        Permanent reaver = addCreatureReady(player1, new FleshReaver());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        DamageSupport damageSupport = GameTestEngineContext.get().getBean(DamageSupport.class);
        harness.inMutationScope(() -> damageSupport.dealDividedDamageToAnyTargets(
                gd, reaver.getCard(), player1.getId(), Map.of(player2.getId(), 3)));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when it damages a planeswalker")
    void doesNotTriggerWhenDamagingPlaneswalker() {
        Permanent reaver = addCreatureReady(player1, new FleshReaver());
        Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
        liliana.setCounterCount(CounterType.LOYALTY, 5);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        DamageSupport damageSupport = GameTestEngineContext.get().getBean(DamageSupport.class);
        harness.inMutationScope(() -> damageSupport.dealDividedDamageToAnyTargets(
                gd, reaver.getCard(), player1.getId(), Map.of(liliana.getId(), 4)));

        assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when another creature you control damages an opponent")
    void doesNotTriggerForAnotherCreature() {
        addCreatureReady(player1, new FleshReaver());
        addCreatureReady(player1, new ProdigalPyromancer());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.stack).isEmpty();
    }
}
