package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HornedTroll.class, GrizzlyBears.class, Shock.class})
class HornedTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Can activate regeneration while summoning sick without tapping")
    void canActivateWhileSummoningSick() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new HornedTroll());
        troll.setSummoningSick(true);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
        assertThat(troll.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Each regeneration shield replaces only one lethal damage event")
    void multipleShieldsAreConsumedOneAtATime() {
        Permanent troll = addCreatureReady(player1, new HornedTroll());
        harness.addMana(player1, ManaColor.GREEN, 2);
        for (int activation = 0; activation < 2; activation++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }
        assertThat(troll.getRegenerationShield()).isEqualTo(2);
        assertThat(troll.isTapped()).isFalse();

        harness.setHand(player2, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.RED, 3);
        for (int damageEvent = 0; damageEvent < 2; damageEvent++) {
            harness.castAndResolveInstant(player2, 0, troll.getId());

            harness.assertOnBattlefield(player1, "Horned Troll");
            assertThat(troll.getRegenerationShield()).isEqualTo(1 - damageEvent);
            assertThat(troll.getMarkedDamage()).isZero();
            assertThat(troll.isTapped()).isTrue();
        }

        harness.castAndResolveInstant(player2, 0, troll.getId());

        harness.assertNotOnBattlefield(player1, "Horned Troll");
        harness.assertInGraveyard(player1, "Horned Troll");
    }

    @Test
    @DisplayName("Paying {G} grants a regeneration shield")
    void payGreenGrantsRegenerationShield() {
        Permanent troll = addCreatureReady(player1, new HornedTroll());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Can activate the regeneration ability while tapped")
    void canActivateWhenTapped() {
        Permanent troll = addCreatureReady(player1, new HornedTroll());
        troll.tap();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield saves Horned Troll from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent troll = addCreatureReady(player1, new HornedTroll());
        troll.setRegenerationShield(1);
        troll.setBlocking(true);
        troll.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Horned Troll");
        assertThat(troll.isTapped()).isTrue();
        assertThat(troll.isBlocking()).isFalse();
        assertThat(troll.getMarkedDamage()).isZero();
        assertThat(troll.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Horned Troll dies in combat without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent troll = addCreatureReady(player1, new HornedTroll());
        troll.setBlocking(true);
        troll.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Horned Troll");
        harness.assertInGraveyard(player1, "Horned Troll");
    }
}
