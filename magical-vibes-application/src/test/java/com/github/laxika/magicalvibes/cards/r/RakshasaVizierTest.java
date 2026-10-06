package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HootingMandrills;
import com.github.laxika.magicalvibes.cards.j.JundCharm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RakshasaVizier.class, Disentomb.class, GrizzlyBears.class, HillGiant.class,
        JundCharm.class, HootingMandrills.class})
class RakshasaVizierTest extends BaseCardTest {

    @Test
    void getsOneCounterPerCardExiledFromOwnGraveyard() {
        Permanent vizier = harness.addToBattlefieldAndReturn(player1, new RakshasaVizier());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new HillGiant()));
        harness.setHand(player1, List.of(new JundCharm()));
        addJundCharmMana();

        harness.castInstant(player1, 0, 0, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(vizier.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerWhenAnOpponentsGraveyardIsExiled() {
        Permanent vizier = harness.addToBattlefieldAndReturn(player1, new RakshasaVizier());
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new HillGiant()));
        harness.setHand(player1, List.of(new JundCharm()));
        addJundCharmMana();

        harness.castInstant(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(vizier.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
    }

    @Test
    void doesNotTriggerWhenCardsReturnFromOwnGraveyard() {
        Permanent vizier = harness.addToBattlefieldAndReturn(player1, new RakshasaVizier());
        var bear = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bear));
        harness.setHand(player1, List.of(new Disentomb()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveSorcery(player1, 0, bear.getId());

        assertThat(vizier.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
    }

    @Test
    void delveCreatesOneTriggerAboveTheSpellForAllExiledCards() {
        Permanent vizier = harness.addToBattlefieldAndReturn(player1, new RakshasaVizier());
        harness.setGraveyard(player1, List.of(new HootingMandrills(), new HootingMandrills(),
                new HootingMandrills(), new HootingMandrills(), new HootingMandrills()));
        harness.setHand(player1, List.of(new HootingMandrills()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4));

        assertThat(gd.stack).hasSize(2);
        assertThat(vizier.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
        harness.passBothPriorities();

        assertThat(vizier.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(5);
        harness.assertNotOnBattlefield(player1, "Hooting Mandrills");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Hooting Mandrills");
    }

    @Test
    void emptyGraveyardDoesNotCreateATrigger() {
        Permanent vizier = harness.addToBattlefieldAndReturn(player1, new RakshasaVizier());
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new JundCharm()));
        addJundCharmMana();

        harness.castInstant(player1, 0, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(vizier.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
    }

    @Test
    void opponentExilingOurGraveyardTriggersEveryVizierWeControl() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new RakshasaVizier());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new RakshasaVizier());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new RakshasaVizier());
        harness.setGraveyard(player1, List.of(new RakshasaVizier()));
        harness.setHand(player2, List.of(new JundCharm()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castInstant(player2, 0, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2);
        assertThat(first.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
        assertThat(second.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
        assertThat(second.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
        assertThat(opposing.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
    }

    private void addJundCharmMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
