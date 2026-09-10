package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.InvasionOfInnistrad;
import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.p.PreyUpon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.effect.normalfx.DamageSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BellowingFiend.class, GrizzlyBears.class, HillGiant.class, InvasionOfInnistrad.class,
        LilianaVess.class, PreyUpon.class})
class BellowingFiendTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 3 to the damaged creature's controller and 3 to you on combat damage to a creature")
    void damagesBothControllersOnCombatDamageToCreature() {
        Permanent fiend = addCreatureReady(player1, new BellowingFiend());
        fiend.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 30);

        blockAttacker(player2, new GrizzlyBears(), 0);

        resolveCombat();
        resolveAllTriggers();

        // 3 damage to the blocker's controller (player2) and 3 damage to Bellowing Fiend's controller (player1).
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(27);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not trigger when it deals combat damage to a player")
    void doesNotTriggerOnDamageToPlayer() {
        Permanent fiend = addCreatureReady(player1, new BellowingFiend());
        fiend.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();

        // Only combat damage to the face; the trigger never fires, so player1 loses no life.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger when another creature you control deals damage to a creature")
    void doesNotTriggerForOtherCreatures() {
        addCreatureReady(player1, new BellowingFiend());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        blockAttacker(player2, new GrizzlyBears(), 1); // block the Grizzly Bears (attacker index 1)

        resolveCombat();

        // The Fiend is not the damage source, so neither player takes the punisher damage.
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Triggers on noncombat damage to a creature")
    void triggersOnNoncombatDamageToCreature() {
        Permanent fiend = addCreatureReady(player1, new BellowingFiend());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setLife(player2, 30);
        harness.setHand(player1, List.of(new PreyUpon()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castSorcery(player1, 0, List.of(fiend.getId(), target.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(27);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Resolves its trigger even when combat damage kills it")
    void triggerResolvesAfterSourceDies() {
        Permanent fiend = addCreatureReady(player1, new BellowingFiend());
        fiend.setAttacking(true);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        blockAttacker(player2, new HillGiant(), 0);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Bellowing Fiend"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Hill Giant"));
    }

    /**
     * "Whenever this creature deals damage to a creature" — a planeswalker or battle is a damage
     * recipient (CR 115.4) but not a creature, so it does not match the trigger event (CR 603.2).
     * Neither route has a card that can aim the damage there yet, so both drive the damage service
     * directly, the way {@code InvasionOfInnistradTest.DamageToBattle} does.
     */
    @Nested
    @DisplayName("Non-creature any-target permanents")
    class NonCreatureAnyTargets {

        @Test
        @DisplayName("Does not trigger when it deals divided damage to a planeswalker")
        void doesNotTriggerOnDamageToPlaneswalker() {
            Permanent fiend = addCreatureReady(player1, new BellowingFiend());
            Permanent liliana = harness.addToBattlefieldAndReturn(player2, new LilianaVess());
            liliana.setCounterCount(CounterType.LOYALTY, 5);
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            DamageSupport damageSupport = GameTestEngineContext.get().getBean(DamageSupport.class);
            harness.inMutationScope(() -> damageSupport.dealDividedDamageToAnyTargets(
                    gd, fiend.getCard(), player1.getId(), Map.of(liliana.getId(), 3)));

            assertThat(liliana.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Does not trigger when it deals any-target damage to a battle")
        void doesNotTriggerOnDamageToBattle() {
            Permanent fiend = addCreatureReady(player1, new BellowingFiend());
            Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfInnistrad());
            battle.setCounterCount(CounterType.DEFENSE, 5);
            harness.setLife(player1, 20);
            harness.setLife(player2, 20);

            DamageSupport damageSupport = GameTestEngineContext.get().getBean(DamageSupport.class);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, fiend.getCard(),
                    player1.getId(), "Bellowing Fiend's ability", List.of(), null, fiend.getId());

            harness.inMutationScope(() ->
                    damageSupport.resolveAnyTargetDamage(gd, entry, battle.getId(), 3, false));

            assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
            assertThat(gd.stack).isEmpty();
            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        }
    }

    /** Adds {@code blockerCard} to {@code blocker}'s battlefield blocking the attacker at {@code attackerIndex}. */
    private void blockAttacker(Player blocker, Card blockerCard, int attackerIndex) {
        Permanent perm = new Permanent(blockerCard);
        perm.setSummoningSick(false);
        perm.setBlocking(true);
        perm.addBlockingTarget(attackerIndex);
        gd.playerBattlefields.get(blocker.getId()).add(perm);
    }
}
