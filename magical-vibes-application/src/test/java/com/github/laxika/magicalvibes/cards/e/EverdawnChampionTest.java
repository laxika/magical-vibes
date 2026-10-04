package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.Bombard;
import com.github.laxika.magicalvibes.cards.s.StampedingHorncrest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EverdawnChampion.class, StampedingHorncrest.class, Bombard.class})
class EverdawnChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage dealt to Everdawn Champion is prevented")
    void combatDamageToEverdawnChampionIsPrevented() {
        Permanent champion = addCreatureReady(player1, new EverdawnChampion());
        champion.setBlocking(true);
        champion.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new StampedingHorncrest());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Everdawn Champion");
        assertThat(champion.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Everdawn Champion still deals its own combat damage")
    void everdawnChampionStillDealsCombatDamage() {
        Permanent champion = addCreatureReady(player1, new EverdawnChampion());
        champion.setBlocking(true);
        champion.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new StampedingHorncrest());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Noncombat damage to Everdawn Champion is not prevented")
    void noncombatDamageToEverdawnChampionIsNotPrevented() {
        addCreatureReady(player2, new EverdawnChampion());
        UUID championId = harness.getPermanentId(player2, "Everdawn Champion");
        harness.setHand(player1, List.of(new Bombard()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, championId);

        harness.assertNotOnBattlefield(player2, "Everdawn Champion");
        harness.assertInGraveyard(player2, "Everdawn Champion");
    }

    @Test
    @DisplayName("An attacking Everdawn Champion prevents blocker damage and damages the blocker")
    void attackingChampionPreventsBlockerDamage() {
        Permanent champion = addCreatureReady(player1, new EverdawnChampion());
        champion.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new StampedingHorncrest());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1);

        harness.assertOnBattlefield(player1, "Everdawn Champion");
        assertThat(champion.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
        harness.assertLife(player2, 20);
    }
}
