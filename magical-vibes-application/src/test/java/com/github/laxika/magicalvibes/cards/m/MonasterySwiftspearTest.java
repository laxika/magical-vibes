package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MonasterySwiftspear.class, Shock.class, GrizzlyBears.class})
class MonasterySwiftspearTest extends BaseCardTest {

    private Permanent addSwiftspear() {
        Permanent swiftspear = harness.addToBattlefieldAndReturn(player1, new MonasterySwiftspear());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return swiftspear;
    }

    private void endTurn() {
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Casting a noncreature spell gives +1/+1 until end of turn (prowess)")
    void noncreatureSpellPumps() {
        Permanent swiftspear = addSwiftspear();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        long triggeredOnStack = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredOnStack).isEqualTo(1);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, swiftspear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, swiftspear)).isEqualTo(3);
    }

    @Test
    @DisplayName("Casting a creature spell does not trigger prowess")
    void creatureSpellDoesNotPump() {
        Permanent swiftspear = addSwiftspear();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, swiftspear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swiftspear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent casting a noncreature spell does not trigger prowess")
    void opponentNoncreatureSpellDoesNotPump() {
        Permanent swiftspear = addSwiftspear();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count()).isZero();
        assertThat(gqs.getEffectivePower(gd, swiftspear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swiftspear)).isEqualTo(2);
    }

    @Test
    @DisplayName("The prowess boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent swiftspear = addSwiftspear();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, swiftspear)).isEqualTo(2);

        endTurn();

        assertThat(gqs.getEffectivePower(gd, swiftspear)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, swiftspear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prowess resolves before the triggering spell and can prevent lethal damage")
    void prowessResolvesBeforeTriggeringSpell() {
        Permanent swiftspear = addSwiftspear();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, swiftspear.getId());
        assertThat(gqs.getEffectiveToughness(gd, swiftspear)).isEqualTo(2);

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, swiftspear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, swiftspear)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(swiftspear);
        assertThat(swiftspear.getMarkedDamage()).isEqualTo(2);

        endTurn();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(swiftspear);
        assertThat(swiftspear.getMarkedDamage()).isZero();
        assertThat(gqs.getEffectiveToughness(gd, swiftspear)).isEqualTo(2);
    }

    @Test
    @DisplayName("Each noncreature spell independently boosts every Swiftspear you control")
    void repeatedCastsBoostEachSwiftspear() {
        Permanent first = addSwiftspear();
        Permanent second = harness.addToBattlefieldAndReturn(player1, new MonasterySwiftspear());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        for (int cast = 0; cast < 2; cast++) {
            harness.castInstant(player1, 0, player2.getId());
            assertThat(gd.stack.stream()
                    .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                    .count()).isEqualTo(2);
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.passBothPriorities();
        }

        for (Permanent swiftspear : List.of(first, second)) {
            assertThat(gqs.getEffectivePower(gd, swiftspear)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, swiftspear)).isEqualTo(4);
        }

        endTurn();

        for (Permanent swiftspear : List.of(first, second)) {
            assertThat(gqs.getEffectivePower(gd, swiftspear)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, swiftspear)).isEqualTo(2);
        }
    }
}
