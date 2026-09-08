package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.r.RavenousRats;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dodecapod.class, RavenousRats.class})
class DodecapodTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters when an opponent causes it to be discarded")
    void entersWithCountersWhenDiscardedByOpponent() {
        harness.setHand(player1, new ArrayList<>(List.of(new Dodecapod())));
        harness.setHand(player2, new ArrayList<>(List.of(new RavenousRats())));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.getGameService().playCard(gd, player2, 0, 0, player1.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent dodecapod = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(dodecapod.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card instanceof Dodecapod);
    }

    @Test
    @DisplayName("Does not enter with counters when cast normally")
    void normalCastHasNoCounters() {
        harness.setHand(player1, List.of(new Dodecapod()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent dodecapod = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(dodecapod.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
