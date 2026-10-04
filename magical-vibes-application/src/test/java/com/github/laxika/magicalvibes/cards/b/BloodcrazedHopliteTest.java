package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.cards.s.SolidarityOfHeroes;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodcrazedHoplite.class, GiantGrowth.class, GrizzlyBears.class,
        GoldenHind.class, SolidarityOfHeroes.class})
class BloodcrazedHopliteTest extends BaseCardTest {

    @Test
    @DisplayName("Heroic puts a counter on Bloodcrazed Hoplite and removes one from a target opponent creature")
    void heroicRemovesCounterFromTargetOpponentCreature() {
        Permanent hoplite = harness.addToBattlefieldAndReturn(player1, new BloodcrazedHoplite());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, hoplite.getId());
        harness.passBothPriorities();

        assertThat(hoplite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("A spell targeting another creature does not trigger Bloodcrazed Hoplite")
    void spellTargetingAnotherCreatureDoesNotTriggerHeroic() {
        Permanent hoplite = harness.addToBattlefieldAndReturn(player1, new BloodcrazedHoplite());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, opponentCreature.getId());

        assertThat(hoplite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's spell targeting Bloodcrazed Hoplite does not trigger heroic")
    void opponentSpellDoesNotTriggerHeroic() {
        Permanent hoplite = harness.addToBattlefieldAndReturn(player1, new BloodcrazedHoplite());
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, hoplite.getId());

        assertThat(hoplite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2})
    @DisplayName("Counters placed by an opponent's spell trigger once per counter")
    void nonHeroicPlacementTriggersForEachCounter(int counters) {
        Permanent hoplite = harness.addToBattlefieldAndReturn(player1, new BloodcrazedHoplite());
        hoplite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        opponentCreature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        harness.setHand(player2, List.of(new SolidarityOfHeroes()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, List.of(hoplite.getId()));

        assertThat(hoplite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2 * counters);
        for (int i = 0; i < counters; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
            harness.handlePermanentChosen(player1, opponentCreature.getId());
            harness.passBothPriorities();
        }
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Removal can target an opponent's counterless creature but not a friendly creature")
    void counterlessOpponentCreatureIsLegalTarget() {
        Permanent hoplite = harness.addToBattlefieldAndReturn(player1, new BloodcrazedHoplite());
        hoplite.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent friendlyCreature = harness.addToBattlefieldAndReturn(player1, new GoldenHind());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GoldenHind());
        harness.setHand(player2, List.of(new SolidarityOfHeroes()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castAndResolveInstant(player2, 0, List.of(hoplite.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        PendingInteraction.PermanentChoice choice = (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(opponentCreature.getId())
                .doesNotContain(hoplite.getId(), friendlyCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(hoplite.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponentCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
