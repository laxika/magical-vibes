package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.r.Rile;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SiegehornCeratopsTest extends BaseCardTest {

    @Test
    void noncombatDamagePutsTwoPlusOnePlusOneCountersOnIt() {
        harness.addToBattlefield(player1, new SiegehornCeratops());
        harness.setHand(player1, List.of(new Rile()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID ceratopsId = harness.getPermanentId(player1, "Siegehorn Ceratops");
        harness.castSorcery(player1, 0, ceratopsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent ceratops = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(ceratops.getMarkedDamage()).isEqualTo(1);
        assertThat(ceratops.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void combatDamagePutsTwoPlusOnePlusOneCountersOnIt() {
        harness.addToBattlefield(player2, new SiegehornCeratops());
        harness.addToBattlefield(player1, new FugitiveWizard());

        Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent ceratops = gd.playerBattlefields.get(player2.getId()).getFirst();
        ceratops.setSummoningSick(false);
        ceratops.setBlocking(true);
        ceratops.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        ceratops = gd.playerBattlefields.get(player2.getId()).getFirst();
        assertThat(ceratops.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void lethalDamageDoesNotPutCountersOnIt() {
        harness.addToBattlefield(player2, new SiegehornCeratops());
        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.s.Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        UUID ceratopsId = harness.getPermanentId(player2, "Siegehorn Ceratops");
        harness.castInstant(player1, 0, ceratopsId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Siegehorn Ceratops");
        harness.assertInGraveyard(player2, "Siegehorn Ceratops");
    }
}
