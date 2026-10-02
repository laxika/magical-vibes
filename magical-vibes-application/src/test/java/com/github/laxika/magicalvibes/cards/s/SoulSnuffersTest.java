package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuskdaleWurm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulSnuffers.class, DuskdaleWurm.class, SpringjackPasture.class})
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

    private Permanent findSnuffers(Player player) {
        return findPermanent(player, "Soul Snuffers");
    }
}
