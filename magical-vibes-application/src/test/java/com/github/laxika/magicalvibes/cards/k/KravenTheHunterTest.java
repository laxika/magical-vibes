package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KravenTheHunter.class, DoomBlade.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class KravenTheHunterTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and gets a counter when the opponent's greatest-power creature dies")
    void triggersForOpponentsGreatestPowerCreature() {
        Permanent kraven = harness.addToBattlefieldAndReturn(player1, new KravenTheHunter());
        Permanent hillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, hillGiant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(kraven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Triggers when the opponent's greatest-power creatures are tied")
    void triggersForTie() {
        Permanent kraven = harness.addToBattlefieldAndReturn(player1, new KravenTheHunter());
        Permanent firstHillGiant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        Forest drawnCard = new Forest();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, firstHillGiant.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(kraven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
    }

    @Test
    @DisplayName("Does not trigger when a smaller opponent creature dies")
    void doesNotTriggerForSmallerCreature() {
        Permanent kraven = harness.addToBattlefieldAndReturn(player1, new KravenTheHunter());
        Permanent grizzlyBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, grizzlyBears.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(kraven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
