package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({LavacoreElemental.class, GrizzlyBears.class})
class LavacoreElementalTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with one time counter")
    void entersWithTimeCounter() {
        harness.setHand(player1, List.of(new LavacoreElemental()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent elemental = findPermanent(player1, "Lavacore Elemental");

        assertThat(elemental.getCounterCount(CounterType.TIME)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets a time counter when a creature you control deals combat damage to a player")
    void getsTimeCounterOnAllyCombatDamage() {
        Permanent elemental = addReadyElemental();
        Permanent bears = addReadyCreature();
        bears.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(elemental.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrifices itself when its last time counter is removed during upkeep")
    void sacrificesOnLastTimeCounter() {
        Permanent elemental = addReadyElemental();
        elemental.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Lavacore Elemental");
        harness.assertInGraveyard(player1, "Lavacore Elemental");
    }

    private Permanent addReadyElemental() {
        Permanent permanent = new Permanent(new LavacoreElemental());
        permanent.setCounterCount(CounterType.TIME, 1);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyCreature() {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
