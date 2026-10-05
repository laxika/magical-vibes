package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.Battlegrowth;
import com.github.laxika.magicalvibes.cards.g.GoForTheThroat;
import com.github.laxika.magicalvibes.cards.q.QuilledSlagwurm;
import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.cards.v.VirulentWound;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MelirasKeepers.class, Skinrender.class, Battlegrowth.class,
        QuilledSlagwurm.class, VirulentWound.class, GoForTheThroat.class})
class MelirasKeepersTest extends BaseCardTest {

    @Test
    @DisplayName("Melira's Keepers can't have -1/-1 counters put on it by Skinrender ETB")
    void cantHaveMinusOneMinusOneCountersFromSkinrender() {
        Permanent keepers = harness.addToBattlefieldAndReturn(player1, new MelirasKeepers());
        harness.setHand(player2, List.of(new Skinrender()));
        harness.addMana(player2, ManaColor.BLACK, 4);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0, keepers.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Melira's Keepers");
        assertThat(keepers.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertOnBattlefield(player2, "Skinrender");
    }

    @Test
    @DisplayName("Melira's Keepers can't have +1/+1 counters put on it")
    void cantHavePlusOnePlusOneCounters() {
        Permanent keepers = harness.addToBattlefieldAndReturn(player1, new MelirasKeepers());
        harness.setHand(player1, List.of(new Battlegrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, keepers.getId());

        assertThat(keepers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertInGraveyard(player1, "Battlegrowth");
    }

    @Test
    @DisplayName("Normal creature can still receive -1/-1 counters")
    void normalCreatureStillReceivesCounters() {
        Permanent keepers = harness.addToBattlefieldAndReturn(player1, new MelirasKeepers());
        Permanent wurm = harness.addToBattlefieldAndReturn(player1, new QuilledSlagwurm());
        harness.setHand(player1, List.of(new VirulentWound()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, wurm.getId());

        assertThat(wurm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(keepers.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertOnBattlefield(player1, "Quilled Slagwurm");
    }

    @Test
    @DisplayName("Preventing a counter does not prevent Virulent Wound's delayed poison trigger")
    void preventedCounterDoesNotStopRemainingSpellEffects() {
        Permanent keepers = harness.addToBattlefieldAndReturn(player2, new MelirasKeepers());
        UUID keepersId = keepers.getId();
        harness.setHand(player1, List.of(new VirulentWound(), new GoForTheThroat()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player1, 0, keepersId);

        assertThat(keepers.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        harness.assertInGraveyard(player1, "Virulent Wound");

        harness.castAndResolveInstant(player1, 0, keepersId);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Melira's Keepers");
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
    }
}
