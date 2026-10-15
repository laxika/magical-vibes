package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.DeadWeight;
import com.github.laxika.magicalvibes.cards.t.TyphoidRats;
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

@CardUsed({FesterhideBoar.class, DeadWeight.class, TyphoidRats.class})
class FesterhideBoarTest extends BaseCardTest {

    @Test
    @DisplayName("Enters without counters when no creature died this turn")
    void entersWithoutMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new FesterhideBoar(), "{3}{G}");
        harness.passBothPriorities();

        Permanent boar = findPermanent(player1, "Festerhide Boar");
        assertThat(boar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Morbid counters are present immediately when the creature spell resolves")
    void entersWithMorbidCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.castFromHand(player1, new FesterhideBoar(), "{3}{G}");
        harness.passBothPriorities();

        Permanent boar = findPermanent(player1, "Festerhide Boar");
        assertThat(boar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(boar.getEffectivePower()).isEqualTo(5);
        assertThat(boar.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's actual creature death enables morbid")
    void actualCreatureDeathEnablesMorbid() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DeadWeight(), new FesterhideBoar()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        Permanent rats = harness.addToBattlefieldAndReturn(player2, new TyphoidRats());

        harness.castEnchantment(player1, 0, rats.getId());
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Typhoid Rats");

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent boar = findPermanent(player1, "Festerhide Boar");
        assertThat(boar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(boar.getEffectivePower()).isEqualTo(5);
        assertThat(boar.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Morbid applies when entering without being cast and a controller's creature died")
    void entersWithoutCastingWithMorbid() {
        gd.creatureDeathCountThisTurn.merge(player1.getId(), 1, Integer::sum);

        Permanent boar = harness.enterBattlefieldAndReturn(player1, new FesterhideBoar());

        assertThat(boar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Multiple creature deaths still give exactly two counters")
    void multipleDeathsDoNotIncreaseCounters() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.creatureDeathCountThisTurn.put(player1.getId(), 2);
        gd.creatureDeathCountThisTurn.put(player2.getId(), 3);

        harness.castFromHand(player1, new FesterhideBoar(), "{3}{G}");
        harness.passBothPriorities();

        Permanent boar = findPermanent(player1, "Festerhide Boar");
        assertThat(boar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
