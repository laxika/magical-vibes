package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuskdaleWurm;
import com.github.laxika.magicalvibes.cards.n.NipGwyllion;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulSnuffers.class, DuskdaleWurm.class, SpringjackPasture.class, NipGwyllion.class})
class SoulSnuffersTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a -1/-1 counter on each creature across all players")
    void etbCountersEveryCreature() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DuskdaleWurm());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new DuskdaleWurm());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SoulSnuffers(), "{2}{B}{B}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB effect

        assertThat(ownCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB counter is placed on Soul Snuffers itself too")
    void etbCountersSelf() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SoulSnuffers(), "{2}{B}{B}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB effect

        Permanent snuffers = findSnuffers(player1);
        assertThat(snuffers.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(snuffers.getEffectivePower()).isEqualTo(2);
        assertThat(snuffers.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB does not put a -1/-1 counter on a noncreature permanent")
    void etbSkipsNoncreatures() {
        Permanent pasture = harness.addToBattlefieldAndReturn(player2, new SpringjackPasture());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SoulSnuffers(), "{2}{B}{B}");
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve ETB effect

        assertThat(pasture.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Creatures entering before the ETB resolves also receive a counter")
    void etbUsesCreaturesPresentAtResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SoulSnuffers(), "{2}{B}{B}");
        harness.passBothPriorities();

        Permanent snuffers = findSnuffers(player1);
        assertThat(snuffers.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        Permanent lateCreature = harness.addToBattlefieldAndReturn(player2, new DuskdaleWurm());
        harness.passBothPriorities();

        assertThat(lateCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(snuffers.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The counters send one-toughness creatures on both sides to the graveyard")
    void etbKillsOneToughnessCreaturesWithoutDealingDamage() {
        harness.addToBattlefield(player1, new NipGwyllion());
        harness.addToBattlefield(player2, new NipGwyllion());
        int ownLife = gd.playerLifeTotals.get(player1.getId());
        int opponentLife = gd.playerLifeTotals.get(player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new SoulSnuffers(), "{2}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Nip Gwyllion");
        harness.assertNotOnBattlefield(player2, "Nip Gwyllion");
        harness.assertInGraveyard(player1, "Nip Gwyllion");
        harness.assertInGraveyard(player2, "Nip Gwyllion");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(ownLife);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife);
        harness.assertOnBattlefield(player1, "Soul Snuffers");
    }
    private Permanent findSnuffers(Player player) {
        return findPermanent(player, "Soul Snuffers");
    }
}
