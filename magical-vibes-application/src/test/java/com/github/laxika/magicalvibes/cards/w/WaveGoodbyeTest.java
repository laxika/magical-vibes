package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WaveGoodbye.class, GrizzlyBears.class, Island.class})
class WaveGoodbyeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns creatures without +1/+1 counters and leaves other permanents alone")
    void returnsCreaturesWithoutPlusOneCounters() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent protectedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        protectedCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Island());

        harness.setHand(player1, List.of(new WaveGoodbye()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(protectedCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land).doesNotContain(opposingCreature);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .contains("Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .contains("Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .doesNotContain("Wave Goodbye");
    }
}
