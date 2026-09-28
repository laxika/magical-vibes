package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LumberingMegasloth.class, GrizzlyBears.class})
class LumberingMegaslothTest extends BaseCardTest {

    @Test
    void costsFullAmountWithNoCounters() {
        harness.setHand(player1, java.util.List.of(new LumberingMegasloth()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void countsCountersOnPlayersAndPermanents() {
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        ownPermanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent opposingPermanent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opposingPermanent.setCounterCount(CounterType.CHARGE, 1);
        gd.playerPoisonCounters.put(player1.getId(), 1);
        gd.playerRadCounters.put(player2.getId(), 1);
        gd.playerEnergyCounters.put(player1.getId(), 1);
        gd.playerSparkCounters.put(player2.getId(), 1);
        gd.playerExperienceCounters.put(player1.getId(), 1);

        harness.setHand(player1, java.util.List.of(new LumberingMegasloth()));
        // Eight counters reduce {10}{G}{G} to {2}{G}{G}.
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void cannotCastWithInsufficientManaAfterReduction() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.setHand(player1, java.util.List.of(new LumberingMegasloth()));
        harness.addMana(player1, ManaColor.COLORLESS, 7);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void entersTapped() {
        harness.setHand(player1, java.util.List.of(new LumberingMegasloth()));
        harness.addMana(player1, ManaColor.COLORLESS, 10);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent sloth = harness.getGameData().playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Lumbering Megasloth"))
                .findFirst()
                .orElseThrow();
        assertThat(sloth.isTapped()).isTrue();
    }
}
