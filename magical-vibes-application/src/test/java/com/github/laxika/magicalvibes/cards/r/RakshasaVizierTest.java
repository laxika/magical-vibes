package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Disentomb;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.j.JundCharm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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

        harness.castSorcery(player1, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(vizier.getCounters().getOrDefault(CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
    }

    private void addJundCharmMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
