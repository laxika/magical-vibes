package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Radstorm.class, Shock.class, GrizzlyBears.class})
class RadstormTest extends BaseCardTest {

    @Test
    @DisplayName("Proliferates and copies itself for each spell cast before it")
    void proliferatesAndStormCopies() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        gd.recordSpellCast(player1.getId(), new Shock());

        harness.setHand(player1, List.of(new Radstorm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);

        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("With no earlier spells, proliferates each counter kind on a chosen player once")
    void proliferatesAllPlayerCounterKindsWithoutStormCopies() {
        gd.playerPoisonCounters.put(player2.getId(), 1);
        gd.playerRadCounters.put(player2.getId(), 2);
        gd.playerEnergyCounters.put(player2.getId(), 3);
        gd.playerExperienceCounters.put(player2.getId(), 4);
        gd.playerRadCounters.put(player1.getId(), 1);

        harness.setHand(player1, List.of(new Radstorm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).isEmpty();

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));

        assertThat(gd.playerPoisonCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(3);
        assertThat(gd.playerEnergyCounters.get(player2.getId())).isEqualTo(4);
        assertThat(gd.playerExperienceCounters.get(player2.getId())).isEqualTo(5);
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Storm counts both players' earlier spells and fixes the count when cast")
    void stormCountsBothPlayersAndAllowsIndependentChoices() {
        gd.playerRadCounters.put(player1.getId(), 1);
        gd.playerRadCounters.put(player2.getId(), 1);
        gd.recordSpellCast(player1.getId(), new Radstorm());
        gd.recordSpellCast(player2.getId(), new Radstorm());

        harness.setHand(player1, List.of(new Radstorm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        gd.recordSpellCast(player2.getId(), new Radstorm());
        harness.passBothPriorities();
        assertThat(gd.stack.stream().filter(StackEntry::isCopy)).hasSize(2);

        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player1.getId()));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(player2.getId()));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getTotalSpellsCastThisTurnCount()).isEqualTo(4);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1).allMatch(card -> card instanceof Radstorm);
    }

    @Test
    @DisplayName("Proliferate can choose opposing permanents and adds every existing counter kind")
    void proliferatesOpposingPermanentAndPlayerTogether() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        bears.setCounterCount(CounterType.CHARGE, 3);
        gd.playerRadCounters.put(player2.getId(), 1);

        harness.setHand(player1, List.of(new Radstorm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId(), player2.getId()));

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(bears.getCounterCount(CounterType.CHARGE)).isEqualTo(4);
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Resolves without a choice when nothing has counters")
    void resolvesWithNoEligiblePermanentsOrPlayers() {
        harness.setHand(player1, List.of(new Radstorm()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .hasSize(1).allMatch(card -> card instanceof Radstorm);
    }
}
