package com.github.laxika.magicalvibes.cards.j;

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

@CardUsed({JeskaiWindscout.class, Shock.class, GrizzlyBears.class})
class JeskaiWindscoutTest extends BaseCardTest {

    private Permanent addWindscout() {
        Permanent windscout = harness.addToBattlefieldAndReturn(player1, new JeskaiWindscout());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return windscout;
    }

    @Test
    @DisplayName("Prowess: casting a noncreature spell gives +1/+1 until end of turn")
    void noncreatureSpellPumps() {
        Permanent windscout = addWindscout();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        long triggeredOnStack = gd.stack.stream()
                .filter(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY)
                .count();
        assertThat(triggeredOnStack).isEqualTo(1);

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, windscout)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, windscout)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prowess: casting a creature spell does not pump")
    void creatureSpellDoesNotPump() {
        Permanent windscout = addWindscout();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gqs.getEffectivePower(gd, windscout)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, windscout)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prowess: the boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent windscout = addWindscout();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, windscout)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, windscout)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, windscout)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prowess resolves before the triggering spell and does not boost immediately")
    void boostResolvesBeforeSpell() {
        Permanent windscout = addWindscout();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gqs.getEffectivePower(gd, windscout)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, windscout)).isEqualTo(1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, windscout)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, windscout)).isEqualTo(2);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        resolveAllTriggers();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Each noncreature spell adds another prowess boost")
    void multipleSpellsStackBoosts() {
        Permanent windscout = addWindscout();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();
        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, windscout)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, windscout)).isEqualTo(3);
    }

    @Test
    @DisplayName("An opponent's noncreature spell does not trigger prowess")
    void opponentSpellDoesNotPump() {
        Permanent windscout = addWindscout();
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());

        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, windscout)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, windscout)).isEqualTo(1);
    }
}
