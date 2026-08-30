package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EverdawnChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage dealt to Everdawn Champion is prevented")
    void combatDamageToEverdawnChampionIsPrevented() {
        Permanent champion = addCreatureReady(player1, new EverdawnChampion());
        champion.setBlocking(true);
        champion.addBlockingTarget(0);

        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Everdawn Champion");
        assertThat(champion.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Everdawn Champion still deals its own combat damage")
    void everdawnChampionStillDealsCombatDamage() {
        Permanent champion = addCreatureReady(player1, new EverdawnChampion());
        champion.setBlocking(true);
        champion.addBlockingTarget(0);

        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncombat damage to Everdawn Champion is not prevented")
    void noncombatDamageToEverdawnChampionIsNotPrevented() {
        Permanent champion = addCreatureReady(player2, new EverdawnChampion());
        UUID championId = harness.getPermanentId(player2, "Everdawn Champion");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, championId);
        harness.passBothPriorities();

        assertThat(champion.getMarkedDamage()).isEqualTo(2);
    }
}
