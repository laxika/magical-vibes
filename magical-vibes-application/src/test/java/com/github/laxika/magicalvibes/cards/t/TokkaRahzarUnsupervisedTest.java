package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaiseTheAlarm;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TokkaRahzarUnsupervised.class, GrizzlyBears.class, RaiseTheAlarm.class, Unsummon.class})
class TokkaRahzarUnsupervisedTest extends BaseCardTest {

    @Test
    void putsCounterAndCreatesTreasureWhenAnotherNontokenCreatureLeaves() {
        Permanent tokka = harness.addToBattlefieldAndReturn(player1, new TokkaRahzarUnsupervised());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        unsummon(player1, bears);

        assertThat(tokka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void doesNotTriggerWhenAnotherTokenCreatureLeaves() {
        Permanent tokka = harness.addToBattlefieldAndReturn(player1, new TokkaRahzarUnsupervised());
        createSoldierToken(player1);
        Permanent soldier = findPermanents(player1, "Soldier").getFirst();

        unsummon(player1, soldier);

        assertThat(tokka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void triggersOnlyOnceEachTurn() {
        Permanent tokka = harness.addToBattlefieldAndReturn(player1, new TokkaRahzarUnsupervised());
        Permanent firstBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent secondBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        unsummon(player1, firstBear);
        unsummon(player1, secondBear);

        assertThat(tokka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }

    @Test
    void doesNotTriggerForAnOpponentsCreature() {
        Permanent tokka = harness.addToBattlefieldAndReturn(player1, new TokkaRahzarUnsupervised());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        unsummon(player2, bears);

        assertThat(tokka.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    private void createSoldierToken(Player player) {
        harness.setHand(player, List.of(new RaiseTheAlarm()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.castInstant(player, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void unsummon(Player caster, Permanent target) {
        harness.setHand(caster, List.of(new Unsummon()));
        harness.addMana(caster, ManaColor.BLUE, 1);
        harness.castInstant(caster, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
