package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CeriseSlayerOfFear.class, Forest.class, LlanowarElves.class, GrizzlyBears.class})
class CeriseSlayerOfFearTest extends BaseCardTest {

    @Test
    void seeksTheHighestManaValueCardWithinLifeGained() {
        harness.addToBattlefield(player1, new CeriseSlayerOfFear());
        harness.setLibrary(player1, List.of(new Forest(), new LlanowarElves(), new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 3);

        advanceToPostcombatMain(player1);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Llanowar Elves");
    }

    @Test
    void doesNotTriggerWithoutLifeGain() {
        harness.addToBattlefield(player1, new CeriseSlayerOfFear());
        harness.setLibrary(player1, List.of(new Forest(), new LlanowarElves(), new GrizzlyBears()));

        advanceToPostcombatMain(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Forest", "Llanowar Elves", "Grizzly Bears");
    }

    @Test
    void doesNotSeekCardAboveLifeGained() {
        harness.addToBattlefield(player1, new CeriseSlayerOfFear());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        gd.lifeGainedThisTurn.put(player1.getId(), 1);

        advanceToPostcombatMain(player1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    private void advanceToPostcombatMain(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
