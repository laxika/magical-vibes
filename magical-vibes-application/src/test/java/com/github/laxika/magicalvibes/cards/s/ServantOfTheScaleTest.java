package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Assassinate;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ServantOfTheScale.class, Assassinate.class, GrizzlyBears.class, HillGiant.class})
class ServantOfTheScaleTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter")
    void entersWithCounter() {
        Permanent servant = castServant();

        assertThat(servant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("When it dies, puts its +1/+1 counters on a target creature you control")
    void deathTriggerPutsCountersOnControlledCreature() {
        Permanent servant = addCreatureReady(player1, new ServantOfTheScale());
        servant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        servant.tap();
        Permanent ally = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentCreature = addCreatureReady(player2, new HillGiant());

        destroyServant(servant);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).contains(ally.getId()).doesNotContain(opponentCreature.getId());

        harness.handlePermanentChosen(player1, ally.getId());
        harness.passBothPriorities();

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    private Permanent castServant() {
        harness.setHand(player1, List.of(new ServantOfTheScale()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Servant of the Scale");
    }

    private void destroyServant(Permanent servant) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Assassinate()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player2, 0, 0, servant.getId(), null);
        harness.passBothPriorities();
    }
}
