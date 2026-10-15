package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AdamantWill;
import com.github.laxika.magicalvibes.cards.b.BalothGorger;
import com.github.laxika.magicalvibes.cards.c.ColdWaterSnapper;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HaphazardBombardment.class, BalothGorger.class, Forest.class, AdamantWill.class,
        ColdWaterSnapper.class})
class HaphazardBombardmentTest extends BaseCardTest {


    @Test
    @DisplayName("ETB puts aim counters on all opponent nonenchantment permanents when 4 or fewer")
    void etbPutsAimCountersOnAllWhenFourOrFewer() {
        Permanent bears1 = addCreature(player2, new BalothGorger());
        Permanent bears2 = addCreature(player2, new BalothGorger());

        castHaphazardBombardment(player1);

        // Aim counters should be placed on both opponent creatures
        assertThat(bears1.getCounterCount(CounterType.AIM)).isEqualTo(1);
        assertThat(bears2.getCounterCount(CounterType.AIM)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB puts aim counters on exactly 4 when opponent has exactly 4 nonenchantment permanents")
    void etbPutsAimCountersOnExactlyFour() {
        Permanent c1 = addCreature(player2, new BalothGorger());
        Permanent c2 = addCreature(player2, new BalothGorger());
        Permanent c3 = addCreature(player2, new BalothGorger());
        Permanent c4 = addCreature(player2, new BalothGorger());

        castHaphazardBombardment(player1);

        assertThat(c1.getCounterCount(CounterType.AIM)).isEqualTo(1);
        assertThat(c2.getCounterCount(CounterType.AIM)).isEqualTo(1);
        assertThat(c3.getCounterCount(CounterType.AIM)).isEqualTo(1);
        assertThat(c4.getCounterCount(CounterType.AIM)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB does not put aim counters on enchantments")
    void etbSkipsEnchantments() {
        Permanent bears = addCreature(player2, new BalothGorger());
        Permanent enchantment = addEnchantment(player2);

        castHaphazardBombardment(player1);

        assertThat(bears.getCounterCount(CounterType.AIM)).isEqualTo(1);
        assertThat(enchantment.getCounterCount(CounterType.AIM)).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB does nothing when opponent has no nonenchantment permanents")
    void etbDoesNothingWhenNoEligiblePermanents() {
        // Only enchantments on opponent's side
        addEnchantment(player2);

        castHaphazardBombardment(player1);

        // No counters should be placed — card still on battlefield
        harness.assertOnBattlefield(player1, "Haphazard Bombardment");
    }

    @Test
    @DisplayName("ETB does not put aim counters on own permanents")
    void etbDoesNotAffectOwnPermanents() {
        Permanent ownBears = addCreature(player1, new BalothGorger());
        Permanent oppBears = addCreature(player2, new BalothGorger());

        castHaphazardBombardment(player1);

        assertThat(ownBears.getCounterCount(CounterType.AIM)).isEqualTo(0);
        assertThat(oppBears.getCounterCount(CounterType.AIM)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB puts aim counters on opponent lands (nonenchantment permanents)")
    void etbPutsCountersOnLands() {
        Permanent land = addLand(player2);

        castHaphazardBombardment(player1);

        assertThat(land.getCounterCount(CounterType.AIM)).isEqualTo(1);
    }


    @Test
    @DisplayName("End step trigger destroys one permanent with aim counter when 2+ have counters")
    void endStepDestroysOneWhenTwoOrMoreHaveCounters() {
        Permanent bears1 = addCreature(player2, new BalothGorger());
        Permanent bears2 = addCreature(player2, new BalothGorger());
        bears1.setCounterCount(CounterType.AIM, 1);
        bears2.setCounterCount(CounterType.AIM, 1);

        addBombardmentToBattlefield(player1);
        advanceToEndStep(player1);

        // One trigger should be on the stack
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve trigger

        // Exactly one of the two should be destroyed
        int remaining = (int) countPermanents(player2, "Baloth Gorger");
        assertThat(remaining).isEqualTo(1);
    }

    @Test
    @DisplayName("End step trigger does not fire when fewer than 2 permanents have aim counters")
    void endStepDoesNotFireWhenFewerThanTwoCounters() {
        Permanent bears = addCreature(player2, new BalothGorger());
        bears.setCounterCount(CounterType.AIM, 1);

        addBombardmentToBattlefield(player1);
        advanceToEndStep(player1);

        // No trigger should be on the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("End step trigger does not fire when no permanents have aim counters")
    void endStepDoesNotFireWhenNoCounters() {
        addCreature(player2, new BalothGorger());

        addBombardmentToBattlefield(player1);
        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("End step trigger re-checks condition on resolution — fizzles if only 1 left")
    void endStepFizzlesIfConditionNoLongerMet() {
        Permanent bears1 = addCreature(player2, new BalothGorger());
        Permanent bears2 = addCreature(player2, new BalothGorger());
        bears1.setCounterCount(CounterType.AIM, 1);
        bears2.setCounterCount(CounterType.AIM, 1);

        addBombardmentToBattlefield(player1);
        advanceToEndStep(player1);

        assertThat(gd.stack).hasSize(1);

        // Remove one of the aim-countered permanents before resolution
        gd.playerBattlefields.get(player2.getId()).remove(bears2);

        int sizeBefore = gd.playerBattlefields.get(player2.getId()).size();
        harness.passBothPriorities(); // resolve trigger

        // Should fizzle — only 1 permanent with aim counter remains (need 2)
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(sizeBefore);
    }

    @Test
    @DisplayName("End step trigger only fires on controller's end step")
    void endStepOnlyFiresOnControllersEndStep() {
        Permanent bears1 = addCreature(player2, new BalothGorger());
        Permanent bears2 = addCreature(player2, new BalothGorger());
        bears1.setCounterCount(CounterType.AIM, 1);
        bears2.setCounterCount(CounterType.AIM, 1);

        addBombardmentToBattlefield(player1);

        // Advance to player2's end step (not the controller's)
        advanceToEndStep(player2);

        // Should not trigger on opponent's end step
        assertThat(gd.stack).isEmpty();
    }


    @Test
    void hexproofDoesNotPreventAimCountersOrDestruction() {
        Permanent first = addCreature(player2, new ColdWaterSnapper());
        Permanent second = addCreature(player2, new ColdWaterSnapper());
        castHaphazardBombardment(player1);

        assertThat(first.getCounterCount(CounterType.AIM)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.AIM)).isEqualTo(1);
        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Cold-Water Snapper")).isEqualTo(1);
    }

    @Test
    void enchantmentsWithExistingAimCountersCanBeDestroyed() {
        Permanent first = addEnchantment(player2);
        Permanent second = addEnchantment(player2);
        first.setCounterCount(CounterType.AIM, 1);
        second.setCounterCount(CounterType.AIM, 1);
        addBombardmentToBattlefield(player1);

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Haphazard Bombardment")).isEqualTo(1);
    }

    @Test
    void choosesExactlyFourOfFiveEligiblePermanents() {
        List<Permanent> creatures = java.util.stream.IntStream.range(0, 5)
                .mapToObj(i -> addCreature(player2, new BalothGorger())).toList();

        castHaphazardBombardment(player1);
        harness.handleMultiplePermanentsChosen(player1,
                creatures.subList(1, 5).stream().map(Permanent::getId).toList());

        assertThat(creatures.getFirst().getCounterCount(CounterType.AIM)).isZero();
        assertThat(creatures.subList(1, 5))
                .allSatisfy(p -> assertThat(p.getCounterCount(CounterType.AIM)).isEqualTo(1));
    }

    @Test
    void multipleAimCountersOnOnePermanentDoNotMeetThreshold() {
        Permanent creature = addCreature(player2, new BalothGorger());
        creature.setCounterCount(CounterType.AIM, 4);
        addBombardmentToBattlefield(player1);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
    }

    @Test
    void ownAimCountersDoNotMeetThreshold() {
        Permanent own = addCreature(player1, new BalothGorger());
        Permanent opponent = addCreature(player2, new BalothGorger());
        own.setCounterCount(CounterType.AIM, 1);
        opponent.setCounterCount(CounterType.AIM, 1);
        addBombardmentToBattlefield(player1);

        advanceToEndStep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void indestructiblePermanentCountsForThresholdButIsExcludedFromRandomDestruction() {
        Permanent protectedCreature = addCreature(player2, new BalothGorger());
        protectedCreature.setCounterCount(CounterType.AIM, 1);
        addBombardmentToBattlefield(player1);
        harness.setHand(player2, List.of(new AdamantWill()));
        harness.addMana(player2, ManaColor.WHITE, 2);
        harness.castAndResolveInstant(player2, 0, protectedCreature.getId());

        // Repeating within the same turn makes an incorrectly random choice overwhelmingly
        // unlikely to pass, while Adamant Will remains in effect throughout.
        for (int i = 0; i < 32; i++) {
            Permanent vulnerable = addCreature(player2, new BalothGorger());
            vulnerable.setCounterCount(CounterType.AIM, 1);
            advanceToEndStep(player1);
            assertThat(gd.stack).hasSize(1);
            harness.passBothPriorities();

            assertThat(gd.playerBattlefields.get(player2.getId()))
                    .contains(protectedCreature).doesNotContain(vulnerable);
        }
    }

    private void castHaphazardBombardment(Player player) {
        harness.setHand(player, List.of(new HaphazardBombardment()));
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.COLORLESS, 5);
        harness.castEnchantment(player, 0);
        harness.passBothPriorities(); // resolve enchantment spell — enters battlefield, ETB trigger goes on stack
        harness.passBothPriorities(); // resolve ETB trigger — places aim counters
    }

    private Permanent addBombardmentToBattlefield(Player player) {
        return harness.addToBattlefieldAndReturn(player, new HaphazardBombardment());
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addEnchantment(Player player) {
        return harness.addToBattlefieldAndReturn(player, new HaphazardBombardment());
    }

    private Permanent addLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Forest());
    }

    private void advanceToEndStep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance POSTCOMBAT_MAIN → END_STEP, triggers fire
    }
}
