package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({HoardersOverflow.class, Forest.class, GrizzlyBears.class, Shock.class})
class HoardersOverflowTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a stash counter on entry and when its controller expends four")
    void getsStashCountersOnEntryAndExpend() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new HoardersOverflow(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castEnchantment(player1, 0);
        resolveAllTriggers();

        Permanent overflow = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(overflow.getCounterCount(CounterType.STASH)).isEqualTo(1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(overflow.getCounterCount(CounterType.STASH)).isEqualTo(1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(overflow.getCounterCount(CounterType.STASH)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing it discards the hand and draws its stash-counter count")
    void sacrificesDiscardsAndDrawsPerStashCounter() {
        Permanent overflow = harness.addToBattlefieldAndReturn(player1, new HoardersOverflow());
        overflow.setCounterCount(CounterType.STASH, 2);
        harness.setHand(player1, List.of(new Shock(), new GrizzlyBears(), new Forest()));
        harness.setLibrary(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(overflow);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(4)
                .contains(overflow.getCard());
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
