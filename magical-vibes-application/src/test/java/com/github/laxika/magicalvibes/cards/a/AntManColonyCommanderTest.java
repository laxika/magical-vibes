package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AntManColonyCommander.class, BurstOfStrength.class, GrizzlyBears.class})
class AntManColonyCommanderTest extends BaseCardTest {

    @Test
    void payingAttackTriggerCountersAnOpponentCreatureAndCreatesAnInsect() {
        addCreatureReady(player1, new AntManColonyCommander());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        resolveAttackTrigger(target, true);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
    }

    @Test
    void payingAttackTriggerCanCounterAntManAndCreateAnInsect() {
        Permanent antMan = addCreatureReady(player1, new AntManColonyCommander());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        resolveAttackTrigger(antMan, true);

        assertThat(antMan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
    }

    @Test
    void decliningAttackTriggerDoesNotCounterOrCreateAnInsect() {
        addCreatureReady(player1, new AntManColonyCommander());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        resolveAttackTrigger(target, false);

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Insect")).isZero();
    }

    @Test
    void createsOnlyOneInsectFromMultipleCounterPlacementsInOneTurn() {
        addCreatureReady(player1, new AntManColonyCommander());
        Permanent firstTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player1, new GrizzlyBears());

        putCounterOn(firstTarget);
        putCounterOn(secondTarget);

        assertThat(firstTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Insect")).isEqualTo(1);
    }

    private void resolveAttackTrigger(Permanent target, boolean pay) {
        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, pay);
        resolveAllTriggers();
    }

    private void putCounterOn(Permanent target) {
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();
    }
}
