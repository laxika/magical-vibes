package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.p.PortentOfBetrayal;
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

@CardUsed({ShipbreakerKraken.class, BronzeSable.class, PortentOfBetrayal.class})
class ShipbreakerKrakenTest extends BaseCardTest {

    @Test
    @DisplayName("Monstrosity puts four +1/+1 counters on Shipbreaker Kraken")
    void monstrosityAddsCountersAndMarksItMonstrous() {
        Permanent kraken = addReadyKraken();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kraken.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(kraken.isMonstrous()).isTrue();
        assertThat(kraken.getEffectivePower()).isEqualTo(10);
        assertThat(kraken.getEffectiveToughness()).isEqualTo(10);
    }

    @Test
    @DisplayName("Becoming monstrous taps up to four creatures and locks them while the Kraken remains")
    void becomingMonstrousTapsAndLocksFourCreatures() {
        addReadyKraken();
        List<Permanent> targets = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            targets.add(harness.addToBattlefieldAndReturn(player2, new BronzeSable()));
        }
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        for (Permanent target : targets) {
            harness.handlePermanentChosen(player1, target.getId());
        }
        harness.passBothPriorities();

        assertThat(targets).allMatch(Permanent::isTapped);

        harness.performUntapStep(player2);

        assertThat(targets).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Monstrosity can be activated again but does nothing if already monstrous")
    void monstrosityOnlyResolvesOnce() {
        Permanent kraken = addReadyKraken();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(kraken.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(kraken.isMonstrous()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canChooseZeroCreatures() {
        Permanent kraken = addReadyKraken();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(kraken.isMonstrous()).isTrue();
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void alreadyTappedCreatureIsLockedAndUntapsAfterSourceLeaves() {
        Permanent kraken = addReadyKraken();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        creature.tap();
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(kraken);
        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void losingControlPermanentlyEndsUntapLock() {
        Permanent kraken = addReadyKraken();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        addMonstrosityMana();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isTrue();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player2, List.of());
        harness.setHand(player2, List.of(new PortentOfBetrayal()));
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player2, 0, kraken.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(kraken);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(kraken);

        harness.performUntapStep(player2);
        assertThat(creature.isTapped()).isFalse();
    }

    private Permanent addReadyKraken() {
        Permanent kraken = harness.addToBattlefieldAndReturn(player1, new ShipbreakerKraken());
        kraken.setSummoningSick(false);
        return kraken;
    }

    private void addMonstrosityMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.BLUE, 2);
    }
}
