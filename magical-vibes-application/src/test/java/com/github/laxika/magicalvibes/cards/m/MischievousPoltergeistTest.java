package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MischievousPoltergeist.class, GrizzlyBears.class, Shock.class})
class MischievousPoltergeistTest extends BaseCardTest {
    @Test
    void canStackMultipleRegenerationShields() {
        Permanent poltergeist = addCreatureReady(player1, new MischievousPoltergeist());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        poltergeist.tap();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(poltergeist.getRegenerationShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Flying prevents non-flying creatures from blocking")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent poltergeist = addCreatureReady(player1, new MischievousPoltergeist());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(poltergeist);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Paying 1 life grants a regeneration shield")
    void payLifeGrantsRegenerationShield() {
        Permanent poltergeist = addCreatureReady(player1, new MischievousPoltergeist());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(poltergeist.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot activate ability with no life to pay")
    void cannotActivateWithInsufficientLife() {
        addCreatureReady(player1, new MischievousPoltergeist());
        harness.setLife(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough life");
    }

    @Test
    @DisplayName("Regeneration shield saves Mischievous Poltergeist from lethal combat damage")
    void regenerationSavesFromLethalCombatDamage() {
        Permanent poltergeist = addCreatureReady(player1, new MischievousPoltergeist());
        poltergeist.setRegenerationShield(1);
        poltergeist.setBlocking(true);
        poltergeist.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Mischievous Poltergeist");
        assertThat(poltergeist.isTapped()).isTrue();
        assertThat(poltergeist.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    @DisplayName("Mischievous Poltergeist dies in combat without a regeneration shield")
    void diesWithoutRegenerationShield() {
        Permanent poltergeist = addCreatureReady(player1, new MischievousPoltergeist());
        poltergeist.setBlocking(true);
        poltergeist.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertNotOnBattlefield(player1, "Mischievous Poltergeist");
        harness.assertInGraveyard(player1, "Mischievous Poltergeist");
    }

    @Test
    @DisplayName("Activated regeneration shield saves Mischievous Poltergeist from lethal combat damage")
    void activatedRegenerationSavesFromLethalCombatDamage() {
        Permanent poltergeist = addCreatureReady(player1, new MischievousPoltergeist());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        poltergeist.setBlocking(true);
        poltergeist.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertOnBattlefield(player1, "Mischievous Poltergeist");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(poltergeist.isTapped()).isTrue();
        assertThat(poltergeist.getRegenerationShield()).isEqualTo(0);
    }

    @Test
    void lifeIsPaidBeforeTheRegenerationAbilityResolves() {
        Permanent poltergeist = addCreatureReady(player1, new MischievousPoltergeist());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);

        harness.assertLife(player1, 19);
        assertThat(poltergeist.getRegenerationShield()).isZero();
        assertThat(poltergeist.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(poltergeist.getRegenerationShield()).isEqualTo(1);
        assertThat(poltergeist.isTapped()).isFalse();
    }

    @Test
    void canActivateWhileSummoningSickWithoutMana() {
        Permanent poltergeist = harness.addToBattlefieldAndReturn(player1, new MischievousPoltergeist());
        poltergeist.setSummoningSick(true);
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        assertThat(poltergeist.getRegenerationShield()).isEqualTo(1);
        assertThat(poltergeist.isTapped()).isFalse();
    }

    @Test
    void regenerationInResponseToShockPreventsLethalDamageAndClearsIt() {
        Permanent poltergeist = addCreatureReady(player1, new MischievousPoltergeist());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, poltergeist.getId());
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Mischievous Poltergeist");
        harness.assertNotInGraveyard(player1, "Mischievous Poltergeist");
        harness.assertLife(player1, 19);
        assertThat(poltergeist.isTapped()).isTrue();
        assertThat(poltergeist.getRegenerationShield()).isZero();
        assertThat(poltergeist.getMarkedDamage()).isZero();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, poltergeist.getId());

        harness.assertNotOnBattlefield(player1, "Mischievous Poltergeist");
        harness.assertInGraveyard(player1, "Mischievous Poltergeist");
    }

    @Test
    void shockInResponseToRegenerationKillsBeforeShieldExists() {
        Permanent poltergeist = addCreatureReady(player1, new MischievousPoltergeist());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.castInstant(player2, 0, poltergeist.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertNotOnBattlefield(player1, "Mischievous Poltergeist");
        harness.assertInGraveyard(player1, "Mischievous Poltergeist");
        assertThat(gd.stack).isEmpty();
    }
}
