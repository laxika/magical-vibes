package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.TymaretTheMurderKing;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GimliOfTheGlitteringCaves.class, TymaretTheMurderKing.class, GrizzlyBears.class})
class GimliOfTheGlitteringCavesTest extends BaseCardTest {

    @Test
    void putsCounterOnGimliForAnotherLegendaryCreatureYouControl() {
        Permanent gimli = harness.addToBattlefieldAndReturn(player1, new GimliOfTheGlitteringCaves());

        harness.enterBattlefieldAndReturn(player1, new TymaretTheMurderKing());
        harness.passBothPriorities();

        assertThat(gimli.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void ignoresNonlegendaryAndOpponentCreatureEntries() {
        Permanent gimli = harness.addToBattlefieldAndReturn(player1, new GimliOfTheGlitteringCaves());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player2, new TymaretTheMurderKing());
        harness.passBothPriorities();

        assertThat(gimli.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void createsTreasureWhenDealingCombatDamage() {
        Permanent gimli = addCreatureReady(player1, new GimliOfTheGlitteringCaves());
        gimli.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void doesNotPutCounterOnItselfWhenItEnters() {
        Permanent gimli = harness.enterBattlefieldAndReturn(player1, new GimliOfTheGlitteringCaves());
        resolveAllTriggers();

        assertThat(gimli.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void legendaryEntryStillAddsCounterAfterEnteringCreatureLeaves() {
        Permanent gimli = harness.addToBattlefieldAndReturn(player1, new GimliOfTheGlitteringCaves());
        Permanent legendary = harness.enterBattlefieldAndReturn(player1, new TymaretTheMurderKing());
        gd.playerBattlefields.get(player1.getId()).remove(legendary);
        gd.playerGraveyards.get(player1.getId()).add(legendary.getCard());

        resolveAllTriggers();

        assertThat(gimli.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(legendary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void eachLegendaryEntryAddsAnotherCounter() {
        Permanent gimli = harness.addToBattlefieldAndReturn(player1, new GimliOfTheGlitteringCaves());
        Permanent first = harness.enterBattlefieldAndReturn(player1, new TymaretTheMurderKing());
        resolveAllTriggers();
        gd.playerBattlefields.get(player1.getId()).remove(first);
        gd.playerGraveyards.get(player1.getId()).add(first.getCard());

        harness.enterBattlefieldAndReturn(player1, new TymaretTheMurderKing());
        resolveAllTriggers();

        assertThat(gimli.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void combatDamageToBlockerDoesNotCreateTreasure() {
        addCreatureReady(player1, new GimliOfTheGlitteringCaves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }
}
