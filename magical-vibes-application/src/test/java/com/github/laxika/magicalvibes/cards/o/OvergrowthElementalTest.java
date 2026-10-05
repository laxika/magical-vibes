package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
import com.github.laxika.magicalvibes.cards.e.EyesOfTheWisent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.SparkElemental;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OvergrowthElemental.class, AirElemental.class, EliteVanguard.class,
        GrizzlyBears.class, Shock.class, SparkElemental.class})
class OvergrowthElementalTest extends BaseCardTest {

    @Test
    @CardUsed(EyesOfTheWisent.class)
    @DisplayName("ETB can put a counter on a noncreature Elemental enchantment")
    void etbCanTargetNoncreatureElemental() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new EyesOfTheWisent());
        harness.setHand(player1, List.of(new OvergrowthElemental()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, elemental.getId());
        resolveAllTriggers();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("ETB puts a +1/+1 counter on another Elemental you control")
    void etbPutsCounterOnAnotherElementalYouControl() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.setHand(player1, List.of(new OvergrowthElemental()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        gs.playCard(gd, player1, 0, 0, elemental.getId(), null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(elemental.getEffectivePower()).isEqualTo(5);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("ETB rejects a non-Elemental or opponent creature as target")
    void etbRejectsIllegalTargets() {
        UUID nonElementalId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new OvergrowthElemental()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, nonElementalId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Elemental creature you control");

        UUID opponentElementalId = harness.addToBattlefieldAndReturn(player2, new AirElemental()).getId();

        assertThatThrownBy(() -> gs.playCard(gd, player1, 0, 0, opponentElementalId, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("another Elemental creature you control");
    }

    @Test
    @DisplayName("Gains life and gets a counter when an Elemental you control dies")
    void gainsLifeAndGetsCounterWhenAllyElementalDies() {
        harness.setLife(player1, 20);
        Permanent overgrowth = harness.addToBattlefieldAndReturn(player1, new OvergrowthElemental());
        harness.addToBattlefield(player1, new SparkElemental());

        killWithShock("Spark Elemental");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(overgrowth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gains life but gets no counter when a non-Elemental creature dies")
    void gainsLifeWithoutCounterWhenNonElementalDies() {
        harness.setLife(player1, 20);
        Permanent overgrowth = harness.addToBattlefieldAndReturn(player1, new OvergrowthElemental());
        harness.addToBattlefield(player1, new EliteVanguard());

        killWithShock("Elite Vanguard");

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(overgrowth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's Elemental dying gives neither life nor counters")
    void opponentElementalDeathDoesNotTrigger() {
        harness.setLife(player1, 20);
        Permanent overgrowth = harness.addToBattlefieldAndReturn(player1, new OvergrowthElemental());
        Permanent opponentElemental = harness.addToBattlefieldAndReturn(player2, new SparkElemental());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, opponentElemental.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Spark Elemental");
        harness.assertLife(player1, 20);
        assertThat(overgrowth.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Overgrowth Elemental does not gain life for its own death")
    void ownDeathDoesNotTrigger() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new OvergrowthElemental());

        killWithShock("Overgrowth Elemental");

        harness.assertInGraveyard(player1, "Overgrowth Elemental");
        harness.assertLife(player1, 20);
    }

    private void killWithShock(String targetName) {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        UUID targetId = harness.getPermanentId(player1, targetName);
        harness.castAndResolveInstant(player1, 0, targetId);
        harness.passBothPriorities();
    }
}
