package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelicChorus;
import com.github.laxika.magicalvibes.cards.a.AngelsFeather;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MiglozMazeCrusher.class, AngelsFeather.class, AngelicChorus.class, GrizzlyBears.class})
class MiglozMazeCrusherTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with five oil counters")
    void entersWithFiveOilCounters() {
        harness.setHand(player1, List.of(new MiglozMazeCrusher()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent migloz = findPermanent(player1, "Migloz, Maze Crusher");
        assertThat(migloz.getCounterCount(CounterType.OIL)).isEqualTo(5);
    }

    @Test
    @DisplayName("Removes one oil counter to gain vigilance and menace until end of turn")
    void gainsVigilanceAndMenaceUntilEndOfTurn() {
        Permanent migloz = addReadyMigloz();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(migloz.getCounterCount(CounterType.OIL)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, migloz, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.hasKeyword(gd, migloz, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, migloz, Keyword.VIGILANCE)).isFalse();
        assertThat(gqs.hasKeyword(gd, migloz, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Removes two oil counters to get +2/+2 until end of turn")
    void getsPlusTwoPlusTwoUntilEndOfTurn() {
        Permanent migloz = addReadyMigloz();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(migloz.getCounterCount(CounterType.OIL)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, migloz)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, migloz)).isEqualTo(6);
    }

    @Test
    @DisplayName("Removes three oil counters to destroy an artifact")
    void destroysArtifact() {
        Permanent migloz = addReadyMigloz();
        Permanent target = addCreatureReady(player2, new AngelsFeather());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, target.getId());
        harness.passBothPriorities();

        assertThat(migloz.getCounterCount(CounterType.OIL)).isEqualTo(2);
        harness.assertNotOnBattlefield(player2, "Angel's Feather");
        harness.assertInGraveyard(player2, "Angel's Feather");
    }

    @Test
    @DisplayName("Can destroy an enchantment but cannot target a creature")
    void destroysEnchantmentAndRejectsCreature() {
        Permanent migloz = addReadyMigloz();
        Permanent enchantment = addCreatureReady(player2, new AngelicChorus());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, enchantment.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Angelic Chorus");

        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        migloz.setCounterCount(CounterType.OIL, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("artifact or enchantment");
    }

    @ParameterizedTest
    @CsvSource({"0, 1", "1, 2", "2, 3"})
    @DisplayName("Each ability requires enough oil counters and pays them before resolution")
    void oilCountersAreAnActivationCost(int abilityIndex, int requiredCounters) {
        Permanent migloz = addReadyMigloz();
        Permanent artifact = addCreatureReady(player2, new AngelsFeather());
        migloz.setCounterCount(CounterType.OIL, requiredCounters - 1);
        harness.addMana(player1, ManaColor.COLORLESS, requiredCounters);
        java.util.UUID targetId = abilityIndex == 2 ? artifact.getId() : null;

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, targetId))
                .isInstanceOf(IllegalStateException.class);
        assertThat(migloz.getCounterCount(CounterType.OIL)).isEqualTo(requiredCounters - 1);
        assertThat(gd.stack).isEmpty();

        migloz.setCounterCount(CounterType.OIL, requiredCounters);
        harness.activateAbility(player1, 0, abilityIndex, null, targetId);

        assertThat(migloz.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gqs.hasKeyword(gd, migloz, Keyword.MENACE)).isFalse();
        assertThat(gqs.getEffectivePower(gd, migloz)).isEqualTo(4);
        harness.assertOnBattlefield(player2, "Angel's Feather");

        harness.passBothPriorities();
        if (abilityIndex == 0) {
            assertThat(gqs.hasKeyword(gd, migloz, Keyword.VIGILANCE)).isTrue();
            assertThat(gqs.hasKeyword(gd, migloz, Keyword.MENACE)).isTrue();
        } else if (abilityIndex == 1) {
            assertThat(gqs.getEffectivePower(gd, migloz)).isEqualTo(6);
            assertThat(gqs.getEffectiveToughness(gd, migloz)).isEqualTo(6);
        } else {
            harness.assertInGraveyard(player2, "Angel's Feather");
        }
    }

    @Test
    @DisplayName("Repeated boosts stack and both expire at end of turn")
    void repeatedBoostsStackAndExpire() {
        Permanent migloz = addReadyMigloz();
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(migloz.getCounterCount(CounterType.OIL)).isEqualTo(1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, migloz)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, migloz)).isEqualTo(8);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, migloz)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, migloz)).isEqualTo(4);
        assertThat(migloz.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Migloz can destroy its controller's artifact")
    void destroysOwnArtifactWhileTappedAndSummoningSick() {
        Permanent migloz = addReadyMigloz();
        migloz.setSummoningSick(true);
        migloz.tap();
        Permanent artifact = addCreatureReady(player1, new AngelsFeather());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 2, null, artifact.getId());
        harness.passBothPriorities();

        assertThat(migloz.getCounterCount(CounterType.OIL)).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "Angel's Feather");
        harness.assertInGraveyard(player1, "Angel's Feather");
    }

    private Permanent addReadyMigloz() {
        Permanent permanent = addCreatureReady(player1, new MiglozMazeCrusher());
        permanent.setCounterCount(CounterType.OIL, 5);
        return permanent;
    }
}
