package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IchorExplosion.class, AirElemental.class, GrizzlyBears.class, LlanowarElves.class})
class IchorExplosionTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing a 2-power creature gives all creatures -2/-2")
    void sacrificeTwoPowerCreatureGivesMinusTwoMinusTwo() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // 2/2
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new AirElemental()); // 4/4

        harness.setHand(player1, List.of(new IchorExplosion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        // Grizzly Bears was sacrificed as cost
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");

        // Air Elemental (4/4) gets -2/-2, becoming effectively 2/2
        assertThat(survivor.getPowerModifier()).isEqualTo(-2);
        assertThat(survivor.getToughnessModifier()).isEqualTo(-2);
    }

    @Test
    @DisplayName("Sacrificing a 1-power creature gives all creatures -1/-1")
    void sacrificeOnePowerCreatureGivesMinusOneMinusOne() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new LlanowarElves()); // 1/1
        Permanent target1 = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // 2/2
        Permanent target2 = harness.addToBattlefieldAndReturn(player2, new AirElemental()); // 4/4

        harness.setHand(player1, List.of(new IchorExplosion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        // Both surviving creatures get -1/-1
        assertThat(target1.getPowerModifier()).isEqualTo(-1);
        assertThat(target1.getToughnessModifier()).isEqualTo(-1);
        assertThat(target2.getPowerModifier()).isEqualTo(-1);
        assertThat(target2.getToughnessModifier()).isEqualTo(-1);
    }

    @Test
    @DisplayName("Sacrificed creature's power includes +1/+1 counters")
    void sacrificedCreaturePowerIncludesCounters() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // 2/2
        sacrifice.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3); // becomes 5/5
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new AirElemental()); // 4/4

        harness.setHand(player1, List.of(new IchorExplosion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        // Grizzly Bears had effective power 5, so all creatures get -5/-5
        assertThat(survivor.getPowerModifier()).isEqualTo(-5);
        assertThat(survivor.getToughnessModifier()).isEqualTo(-5);
    }

    @Test
    @DisplayName("Casting puts spell on stack with correct xValue")
    void castingPutsSpellOnStackWithCorrectXValue() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // 2/2

        harness.setHand(player1, List.of(new IchorExplosion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(gd.stack.getFirst().getXValue()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot cast without a creature to sacrifice")
    void cannotCastWithoutCreatureToSacrifice() {
        // Add a creature to opponent's battlefield so spell is considered playable by ValidTargetService,
        // but player1 still has no creature to sacrifice
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IchorExplosion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sacrifice");
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new IchorExplosion()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("you control");
    }

    @Test
    @DisplayName("Zero-power sacrifice still pays the cost but does not weaken creatures")
    void zeroPowerSacrificeDoesNotWeakenCreatures() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        sacrifice.setPowerModifier(-2);
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new IchorExplosion()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Air Elemental");
        assertThat(survivor.getPowerModifier()).isZero();
        assertThat(survivor.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Negative-power sacrifice does not give creatures a positive boost")
    void negativePowerSacrificeDoesNotBoostCreatures() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        sacrifice.setPowerModifier(-3);
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new IchorExplosion()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(survivor.getPowerModifier()).isZero();
        assertThat(survivor.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Creatures with zero toughness die on both sides")
    void creaturesWithZeroToughnessDieOnBothSides() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new IchorExplosion()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The effect expires at cleanup and does not affect later creatures")
    void effectExpiresAndDoesNotAffectLaterCreatures() {
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent survivor = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new IchorExplosion()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castSorceryWithSacrifice(player1, 0, sacrifice.getId());
        harness.passBothPriorities();
        Permanent newcomer = harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(survivor.getPowerModifier()).isEqualTo(-2);
        assertThat(survivor.getToughnessModifier()).isEqualTo(-2);
        assertThat(newcomer.getPowerModifier()).isZero();
        assertThat(newcomer.getToughnessModifier()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);

        assertThat(survivor.getPowerModifier()).isZero();
        assertThat(survivor.getToughnessModifier()).isZero();
    }
}
