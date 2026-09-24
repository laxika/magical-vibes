package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.k.KondaLordOfEiganjo;
import com.github.laxika.magicalvibes.cards.l.LivonyaSilone;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CleopatraExiledPharaoh.class, KondaLordOfEiganjo.class, LivonyaSilone.class,
        GrizzlyBears.class, DoomBlade.class})
class CleopatraExiledPharaohTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters on up to two other legendary creatures at the controller's end step")
    void putsCountersOnOtherLegendaryCreatures() {
        Permanent cleopatra = harness.addToBattlefieldAndReturn(player1, new CleopatraExiledPharaoh());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new KondaLordOfEiganjo());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LivonyaSilone());
        Permanent nonlegendary = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, cleopatra.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonlegendary.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonlegendary.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Draws for all counters and loses two life when a legendary creature with counters dies")
    void drawsAndLosesLifeForLegendaryCreatureWithCountersDying() {
        harness.addToBattlefield(player1, new CleopatraExiledPharaoh());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new LivonyaSilone());
        dying.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(new DoomBlade()));
        int handBeforeCast = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, dying.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeCast + 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("Ignores a dying nonlegendary creature with counters")
    void ignoresNonlegendaryCreatureWithCounters() {
        harness.addToBattlefield(player1, new CleopatraExiledPharaoh());
        Permanent dying = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        dying.setCounterCount(CounterType.CHARGE, 2);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        int lifeBefore = gd.getLife(player1.getId());

        harness.setHand(player1, List.of(new DoomBlade()));
        int handBeforeCast = gd.playerHands.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, dying.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBeforeCast - 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }
}
