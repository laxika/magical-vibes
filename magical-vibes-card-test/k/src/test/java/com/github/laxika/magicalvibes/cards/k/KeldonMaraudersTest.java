package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AvenRiftwatcher;
import com.github.laxika.magicalvibes.cards.d.Damnation;
import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.f.FuryCharm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KeldonMarauders.class, AvenRiftwatcher.class, Damnation.class, ElspethKnightErrant.class, FuryCharm.class})
class KeldonMaraudersTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two time counters and deals 1 damage to a player")
    void entersWithCountersAndDamagesPlayer() {
        harness.setHand(player1, List.of(new KeldonMarauders()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, player2.getId());
        resolveAllTriggers();

        Permanent marauders = findPermanent(player1, "Keldon Marauders");
        assertThat(marauders.getCounterCount(CounterType.TIME)).isEqualTo(2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Enters and deals 1 damage to a planeswalker")
    void entersAndDamagesPlaneswalker() {
        Permanent planeswalker = addCreatureReady(player2, new ElspethKnightErrant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        harness.setHand(player1, List.of(new KeldonMarauders()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, planeswalker.getId());
        resolveAllTriggers();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Leaves the battlefield and deals 1 damage to a planeswalker")
    void leavesBattlefieldDamagesPlaneswalker() {
        addCreatureReady(player1, new KeldonMarauders());
        Permanent planeswalker = addCreatureReady(player2, new ElspethKnightErrant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        harness.castFromHand(player1, new Damnation(), "{2}{B}{B}");
        resolveAllTriggers();

        harness.handlePermanentChosen(player1, planeswalker.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Keldon Marauders");
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Removes a time counter during its controller's upkeep")
    void upkeepRemovesTimeCounter() {
        Permanent marauders = addCreatureReady(player1, new KeldonMarauders());
        marauders.setCounterCount(CounterType.TIME, 2);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(marauders.getCounterCount(CounterType.TIME)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(marauders);
    }

    @Test
    @DisplayName("Does not remove a time counter during an opponent's upkeep")
    void opponentUpkeepDoesNotRemoveTimeCounter() {
        Permanent marauders = addCreatureReady(player1, new KeldonMarauders());
        marauders.setCounterCount(CounterType.TIME, 2);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(marauders.getCounterCount(CounterType.TIME)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = addCreatureReady(player2, new AvenRiftwatcher());
        harness.setHand(player1, List.of(new KeldonMarauders()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("player or planeswalker");
    }

    @Test
    @DisplayName("Sacrifices itself when its last time counter is removed")
    void lastTimeCounterCausesSacrifice() {
        Permanent marauders = addCreatureReady(player1, new KeldonMarauders());
        marauders.setCounterCount(CounterType.TIME, 1);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Keldon Marauders");
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("Removing the last time counter with Fury Charm causes sacrifice and leave damage")
    void removingLastTimeCounterOutsideUpkeepCausesSacrifice(int timeCounters) {
        Permanent marauders = addCreatureReady(player1, new KeldonMarauders());
        marauders.setCounterCount(CounterType.TIME, timeCounters);
        harness.setHand(player1, List.of(new FuryCharm()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 2, marauders.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Keldon Marauders");
        harness.assertInGraveyard(player1, "Keldon Marauders");
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Vanishing does not trigger during upkeep without a time counter")
    void noUpkeepTriggerWithoutTimeCounters() {
        addCreatureReady(player1, new KeldonMarauders());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Keldon Marauders");
    }

    @Test
    @DisplayName("Entry damage may target its own controller")
    void entryDamageMayTargetController() {
        harness.setHand(player1, List.of(new KeldonMarauders()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, player1.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }
}
