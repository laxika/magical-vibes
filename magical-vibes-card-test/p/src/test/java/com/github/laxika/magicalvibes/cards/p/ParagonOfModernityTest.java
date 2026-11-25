package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.DoublingSeason;
import com.github.laxika.magicalvibes.cards.h.HardenedScales;
import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParagonOfModernity.class, HardenedScales.class, DoublingSeason.class})
class ParagonOfModernityTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 until end of turn when fewer than three colors are spent")
    void boostsWhenFewerThanThreeColorsAreSpent() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfModernity());
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        activateAndResolve();

        assertThat(PermanentGrants.powerBoostOf(gd, paragon)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, paragon)).isEqualTo(1);
        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when exactly three colors are spent")
    void putsCounterWhenExactlyThreeColorsAreSpent() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfModernity());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        activateAndResolve();

        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, paragon)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, paragon)).isZero();
    }

    @Test
    @DisplayName("Uses the colors spent by each individual activation")
    void snapshotsColorsPerActivation() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfModernity());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, paragon)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, paragon)).isEqualTo(1);
    }

    @Test
    @DisplayName("Colorless mana does not count as a third color")
    void colorlessManaDoesNotCountAsAColor() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfModernity());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        activateAndResolve();

        assertThat(PermanentGrants.powerBoostOf(gd, paragon)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, paragon)).isEqualTo(1);
        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Temporary boosts expire while three-color counters remain")
    void temporaryBoostExpiresButCounterRemains() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfModernity());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        activateAndResolve();
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        activateAndResolve();

        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, paragon)).isEqualTo(1);
        assertThat(PermanentGrants.toughnessBoostOf(gd, paragon)).isEqualTo(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(PermanentGrants.powerBoostOf(gd, paragon)).isZero();
        assertThat(PermanentGrants.toughnessBoostOf(gd, paragon)).isZero();
    }

    @Test
    @DisplayName("Its controller orders Hardened Scales and Doubling Season for the three-color counter")
    void controllerOrdersCounterReplacements() {
        Permanent paragon = addCreatureReady(player1, new ParagonOfModernity());
        harness.addToBattlefield(player1, new HardenedScales());
        harness.addToBattlefield(player1, new DoublingSeason());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        activateAndResolve();

        PendingInteraction.ColorChoice order = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(order).isNotNull();
        harness.handleListChoice(player1, order.options().stream()
                .filter(option -> option.contains("Hardened Scales")).findFirst().orElseThrow());

        assertThat(paragon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(PermanentGrants.powerBoostOf(gd, paragon)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    private void activateAndResolve() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
