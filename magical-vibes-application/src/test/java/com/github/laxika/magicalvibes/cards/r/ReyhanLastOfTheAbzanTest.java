package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ReyhanLastOfTheAbzan.class, GrizzlyBears.class, FlameJavelin.class})
class ReyhanLastOfTheAbzanTest extends BaseCardTest {

    @Test
    void transfersOnlyPlusOnePlusOneCountersWhenAnotherCreatureDies() {
        Permanent reyhan = addCreatureReady(player1, new ReyhanLastOfTheAbzan());
        reyhan.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent dyingBears = addCreatureReady(player1, new GrizzlyBears());
        dyingBears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        dyingBears.setCounterCount(CounterType.CHARGE, 3);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        destroyWithFlameJavelin(dyingBears);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(target.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void doesNotTriggerWhenTheDyingCreatureHasNoPlusOnePlusOneCounters() {
        Permanent reyhan = addCreatureReady(player1, new ReyhanLastOfTheAbzan());
        reyhan.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent dyingBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        destroyWithFlameJavelin(dyingBears);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void transfersCountersWhenItsCommanderIsPutIntoTheCommandZone() {
        gd.format = DeckFormat.COMMANDER;
        ReyhanLastOfTheAbzan card = new ReyhanLastOfTheAbzan();
        card.setOwnerId(player1.getId());
        card.freeze();
        gd.makeCommander(player1.getId(), card);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>());

        Permanent reyhan = addCreatureReady(player1, card);
        reyhan.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.getPermanentRemovalService().removePermanentToCommandZone(gd, reyhan);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void destroyWithFlameJavelin(Permanent target) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
    }
}
