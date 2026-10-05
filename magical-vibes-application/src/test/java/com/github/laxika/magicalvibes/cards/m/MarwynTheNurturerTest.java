package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.g.GiftOfGrowth;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.p.ProwessOfTheFair;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MarwynTheNurturer.class, LlanowarElves.class, BalothGorger.class, GiftOfGrowth.class})
class MarwynTheNurturerTest extends BaseCardTest {

    @Test
    @DisplayName("Gets a +1/+1 counter when another Elf enters the battlefield")
    void getsCounterWhenElfEnters() {
        Permanent marwyn = harness.addToBattlefieldAndReturn(player1, new MarwynTheNurturer());
        assertThat(marwyn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        // Cast Llanowar Elves (Elf Druid)
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell (triggers Marwyn)
        harness.passBothPriorities(); // resolve Marwyn's +1/+1 counter triggered ability

        assertThat(marwyn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, marwyn)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, marwyn)).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not get a counter when a non-Elf creature enters")
    void noCounterWhenNonElfEnters() {
        Permanent marwyn = harness.addToBattlefieldAndReturn(player1, new MarwynTheNurturer());

        // Cast a non-Elf creature without kicker.
        harness.setHand(player1, List.of(new BalothGorger()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(marwyn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger when opponent casts an Elf")
    void noCounterWhenOpponentCastsElf() {
        Permanent marwyn = harness.addToBattlefieldAndReturn(player1, new MarwynTheNurturer());

        // Opponent casts an Elf
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new LlanowarElves()));
        harness.addMana(player2, ManaColor.GREEN, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities(); // resolve creature spell

        assertThat(marwyn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Gets multiple counters from multiple Elf entries")
    void getsMultipleCounters() {
        Permanent marwyn = harness.addToBattlefieldAndReturn(player1, new MarwynTheNurturer());

        // Cast first Elf
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve Marwyn's triggered ability

        assertThat(marwyn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        // Cast second Elf
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve Marwyn's triggered ability

        assertThat(marwyn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, marwyn)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, marwyn)).isEqualTo(3);
    }

    @Test
    @DisplayName("Tap ability produces green mana equal to power (base 1)")
    void tapProducesGreenManaEqualToBasePower() {
        Permanent marwyn = harness.addToBattlefieldAndReturn(player1, new MarwynTheNurturer());
        marwyn.setSummoningSick(false);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tap ability produces green mana reflecting +1/+1 counters")
    void tapProducesMoreManaWithCounters() {
        Permanent marwyn = harness.addToBattlefieldAndReturn(player1, new MarwynTheNurturer());
        marwyn.setSummoningSick(false);

        // Add two +1/+1 counters (simulating two Elf ETBs)
        marwyn.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);

        // Base power 1 + 2 counters = 3 green mana
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Integration: Elf ETB gives counter, then tap produces increased mana")
    void elfEtbThenTapProducesCorrectMana() {
        Permanent marwyn = harness.addToBattlefieldAndReturn(player1, new MarwynTheNurturer());
        marwyn.setSummoningSick(false);

        // Cast Llanowar Elves to trigger +1/+1 counter
        harness.setHand(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell
        harness.passBothPriorities(); // resolve Marwyn's triggered ability

        assertThat(marwyn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, marwyn)).isEqualTo(2);

        // Tap Marwyn for mana — should produce 2 green (1 base + 1 counter)
        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(2);
    }

    @Test
    @DisplayName("Marwyn does not trigger for her own entry")
    void noCounterForOwnEntry() {
        harness.setHand(player1, List.of(new MarwynTheNurturer()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent marwyn = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(marwyn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Summoning-sick Marwyn cannot activate her tap ability")
    void summoningSickCannotActivate() {
        Permanent marwyn = harness.addToBattlefieldAndReturn(player1, new MarwynTheNurturer());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(marwyn.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("A temporary power boost increases mana immediately without using the stack")
    void temporaryBoostIncreasesMana() {
        Permanent marwyn = harness.addToBattlefieldAndReturn(player1, new MarwynTheNurturer());
        marwyn.setSummoningSick(false);
        harness.setHand(player1, List.of(new GiftOfGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, marwyn.getId());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(marwyn.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Marwyn with zero or negative power produces no mana but still taps")
    void nonpositivePowerProducesNoMana() {
        Permanent marwyn = harness.addToBattlefieldAndReturn(player1, new MarwynTheNurturer());
        marwyn.setSummoningSick(false);
        marwyn.setPowerModifier(-1);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(marwyn.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();

        marwyn.setTapped(false);
        marwyn.setPowerModifier(-2);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(marwyn.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({ProwessOfTheFair.class})
    @DisplayName("A noncreature Elf entering also gives Marwyn a counter")
    void noncreatureElfEntryGivesCounter() {
        Permanent marwyn = harness.addToBattlefieldAndReturn(player1, new MarwynTheNurturer());
        harness.setHand(player1, List.of(new ProwessOfTheFair()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(marwyn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
