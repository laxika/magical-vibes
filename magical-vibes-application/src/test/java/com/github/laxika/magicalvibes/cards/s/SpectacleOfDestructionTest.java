package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectacleOfDestruction.class, GrizzlyBears.class, WrathOfGod.class, Forest.class})
class SpectacleOfDestructionTest extends BaseCardTest {

    @Test
    void simultaneousCreatureDeathsPutOneWreckCounterOnSpectacle() {
        Permanent spectacle = harness.addToBattlefieldAndReturn(player1, new SpectacleOfDestruction());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castSorcery(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(spectacle.getCounterCount(CounterType.WRECK)).isEqualTo(1);
    }

    @Test
    void upkeepRemovesWreckCounterAndSeeksNonlandCard() {
        Permanent spectacle = harness.addToBattlefieldAndReturn(player1, new SpectacleOfDestruction());
        spectacle.setCounterCount(CounterType.WRECK, 1);
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(spectacle.getCounterCount(CounterType.WRECK)).isZero();
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(card -> card.getName())
                .containsExactly("Forest");
    }
}
