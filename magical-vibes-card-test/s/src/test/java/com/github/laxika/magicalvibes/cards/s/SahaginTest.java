package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Hurricane;
import com.github.laxika.magicalvibes.cards.h.HillGigas;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Sahagin.class, Shock.class, Hurricane.class, GrizzlyBears.class, HillGigas.class})
class SahaginTest extends BaseCardTest {

    @Test
    @DisplayName("A noncreature spell with less than four mana spent does not trigger Sahagin")
    void cheapNoncreatureSpellDoesNotTrigger() {
        Permanent sahagin = addCreatureReady(player1, new Sahagin());

        setUpMainPhase();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(sahagin.getEffectivePower()).isEqualTo(1);
        assertThat(sahagin.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("A noncreature spell with at least four mana spent adds a counter and makes Sahagin unblockable")
    void expensiveNoncreatureSpellTriggers() {
        Permanent sahagin = addCreatureReady(player1, new Sahagin());

        setUpMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 3);
        harness.passBothPriorities();

        assertThat(sahagin.getEffectivePower()).isEqualTo(2);
        assertThat(sahagin.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(sahagin.getEffectivePower()).isEqualTo(2);
        assertThat(sahagin.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("A creature spell does not trigger Sahagin")
    void creatureSpellDoesNotTrigger() {
        Permanent sahagin = addCreatureReady(player1, new Sahagin());

        setUpMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(sahagin.getEffectivePower()).isEqualTo(1);
        assertThat(sahagin.isCantBeBlocked()).isFalse();
    }

    @Test
    void threeManaSpentDoesNotTrigger() {
        Permanent sahagin = addCreatureReady(player1, new Sahagin());
        setUpMainPhase();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.setHand(player1, List.of(new Hurricane()));
        harness.castSorcery(player1, 0, 2);
        resolveAllTriggers();

        assertThat(sahagin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(sahagin.isCantBeBlocked()).isFalse();
    }

    @Test
    void eachQualifyingSpellAddsAnotherCounter() {
        Permanent sahagin = addCreatureReady(player1, new Sahagin());
        setUpMainPhase();
        harness.setHand(player1, List.of(new Hurricane(), new Hurricane()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castSorcery(player1, 0, 3);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castSorcery(player1, 0, 4);
        resolveAllTriggers();

        assertThat(sahagin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(sahagin.isCantBeBlocked()).isTrue();
    }

    @Test
    void opponentsQualifyingSpellDoesNotTrigger() {
        Permanent sahagin = addCreatureReady(player1, new Sahagin());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new Hurricane()));
        harness.castSorcery(player2, 0, 3);
        resolveAllTriggers();

        assertThat(sahagin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(sahagin.isCantBeBlocked()).isFalse();
    }

    @Test
    void creatureSpellWithSixManaSpentDoesNotTrigger() {
        Permanent sahagin = addCreatureReady(player1, new Sahagin());
        setUpMainPhase();
        harness.addMana(player1, ManaColor.RED, 6);
        harness.setHand(player1, List.of(new HillGigas()));
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(sahagin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(sahagin.isCantBeBlocked()).isFalse();
    }

    private void setUpMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
    }
}
