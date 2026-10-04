package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HealTheScars.class, GrizzlyBears.class, HillGiant.class})
class HealTheScarsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Heal the Scars targeting a creature puts it on the stack")
    void castingPutsOnStack() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new HealTheScars()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getTargetId()).isEqualTo(bears.getId());
    }

    @Test
    @DisplayName("Resolving grants a regeneration shield and gains life equal to the creature's toughness")
    void resolvingGrantsShieldAndGainsLife() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new HealTheScars()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0, bears.getId());

        GameData gd = harness.getGameData();
        assertThat(bears.getRegenerationShield()).isEqualTo(1);
        // Grizzly Bears is 2/2 -> gain 2 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Life gained scales with the target creature's toughness")
    void lifeGainScalesWithToughness() {
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        harness.setHand(player1, List.of(new HealTheScars()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        int lifeBefore = harness.getGameData().playerLifeTotals.get(player1.getId());

        harness.castAndResolveInstant(player1, 0, giant.getId());

        GameData gd = harness.getGameData();
        // Hill Giant is 3/3 -> gain 3 life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("Targeting an opposing creature gains life for the spell controller without tapping or healing the creature")
    void opposingCreatureGrantsLifeToCaster() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        bears.setMarkedDamage(1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new HealTheScars()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(bears.getRegenerationShield()).isEqualTo(1);
        assertThat(bears.isTapped()).isFalse();
        assertThat(bears.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Life gain uses toughness at resolution, including counters added after casting")
    void usesToughnessAtResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new HealTheScars()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, bears.getId());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        harness.assertLife(player1, 24);
        assertThat(bears.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("An absent target prevents both regeneration and life gain")
    void absentTargetPreventsLifeGain() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new HealTheScars()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castInstant(player1, 0, bears.getId());
        harness.getGameData().playerBattlefields.get(player1.getId()).remove(bears);
        harness.setGraveyard(player1, List.of(bears.getCard()));
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(bears.getRegenerationShield()).isZero();
        harness.assertInGraveyard(player1, "Heal the Scars");
    }

    @Test
    @DisplayName("Regeneration shield saves the target from lethal combat damage")
    void shieldSavesFromLethalCombatDamage() {
        // Grizzly Bears (2/2) blocks a Hill Giant (3/3) -> would die, but is regenerated first
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setSummoningSick(false);
        bears.setBlocking(true);
        bears.addBlockingTarget(0);

        harness.setHand(player1, List.of(new HealTheScars()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castAndResolveInstant(player1, 0, bears.getId());
        assertThat(bears.getRegenerationShield()).isEqualTo(1);

        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(bears.getRegenerationShield()).isEqualTo(0);
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.isBlocking()).isFalse();
    }
}
