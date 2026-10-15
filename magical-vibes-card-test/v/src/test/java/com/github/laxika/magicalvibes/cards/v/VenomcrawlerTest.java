package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Venomcrawler.class, GrizzlyBears.class, Shock.class})
class VenomcrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when an opponent's creature dies")
    void putsCounterWhenOpponentCreatureDies() {
        Permanent venomcrawler = harness.addToBattlefieldAndReturn(player1, new Venomcrawler());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        killWithShock(player1, bears);

        assertThat(venomcrawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when an ally creature dies")
    void putsCounterWhenAllyCreatureDies() {
        Permanent venomcrawler = harness.addToBattlefieldAndReturn(player1, new Venomcrawler());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        killWithShock(player1, bears);

        assertThat(venomcrawler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger for its own death")
    void doesNotTriggerForOwnDeath() {
        Permanent venomcrawler = harness.addToBattlefieldAndReturn(player1, new Venomcrawler());
        venomcrawler.setMarkedDamage(2);

        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Venomcrawler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Gets one counter for each other creature dying simultaneously")
    void getsCounterForEachSimultaneousDeath() {
        Permanent survivor = harness.addToBattlefieldAndReturn(player1, new Venomcrawler());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new Venomcrawler());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new Venomcrawler());
        ally.setMarkedDamage(2);
        opponent.setMarkedDamage(2);

        harness.runStateBasedActions();
        assertThat(gd.stack).hasSize(4);
        for (int i = 0; i < 4; i++) {
            harness.passBothPriorities();
        }

        assertThat(survivor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(survivor);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage gains life through lifelink")
    void combatDamageGainsLife() {
        Permanent venomcrawler = addCreatureReady(player1, new Venomcrawler());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        venomcrawler.setAttacking(true);
        venomcrawler.setAttackTarget(player2.getId());

        harness.resolveCombatDamage();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A death counter increases combat damage and lifelink life gain")
    void deathCounterIncreasesDamageAndLifeGain() {
        Permanent venomcrawler = addCreatureReady(player1, new Venomcrawler());
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new Venomcrawler());
        victim.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        venomcrawler.setAttacking(true);
        venomcrawler.setAttackTarget(player2.getId());

        harness.resolveCombatDamage();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 17);
    }

    private void killWithShock(com.github.laxika.magicalvibes.model.Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Shock()));
        harness.addMana(caster, ManaColor.RED, 1);
        harness.castAndResolveInstant(caster, 0, target.getId());
        harness.passBothPriorities();
    }
}
