package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.Gristleback;
import com.github.laxika.magicalvibes.cards.i.IzzetSignet;
import com.github.laxika.magicalvibes.cards.w.Wildsize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LivingInferno.class, Gristleback.class, IzzetSignet.class, Wildsize.class})
class LivingInfernoTest extends BaseCardTest {

    @Test
    @DisplayName("Divides its power among target creatures and receives damage from each")
    void dividesDamageAndReceivesDamage() {
        Permanent inferno = addCreatureReady(player1, new LivingInferno());
        Permanent firstGristleback = addCreatureReady(player2, new Gristleback());
        Permanent secondGristleback = addCreatureReady(player2, new Gristleback());
        prepareForActivation();

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(firstGristleback.getId(), 4, secondGristleback.getId(), 4));

        assertThat(inferno.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(inferno.getMarkedDamage()).isEqualTo(4);
        harness.assertNotOnBattlefield(player2, "Gristleback");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        addCreatureReady(player1, new LivingInferno());
        Permanent signet = harness.addToBattlefieldAndReturn(player2, new IzzetSignet());
        prepareForActivation();

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(signet.getId(), 8)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Requires a target when dividing nonzero damage")
    void requiresATargetWhenDividingNonzeroDamage() {
        Permanent inferno = addCreatureReady(player1, new LivingInferno());
        prepareForActivation();

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(inferno.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Requires assigning all of its power as damage")
    void requiresAssigningAllPowerAsDamage() {
        Permanent inferno = addCreatureReady(player1, new LivingInferno());
        Permanent target = addCreatureReady(player2, new Gristleback());
        prepareForActivation();

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(target.getId(), 7)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(inferno.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Damage division remains fixed when Living Inferno's power increases in response")
    void damageDivisionIsFixedAtActivation() {
        Permanent inferno = addCreatureReady(player1, new LivingInferno());
        Permanent target = addCreatureReady(player2, new LivingInferno());
        prepareForActivation();

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of(target.getId(), 8));
        castAndResolveWildsize(player1, inferno);
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(8);
        harness.assertInGraveyard(player2, "Living Inferno");
        harness.assertInGraveyard(player1, "Living Inferno");
    }

    @Test
    @DisplayName("Return damage uses the target's power at resolution even after lethal damage")
    void returnDamageUsesCurrentTargetPower() {
        Permanent inferno = addCreatureReady(player1, new LivingInferno());
        Permanent target = addCreatureReady(player2, new Gristleback());
        prepareForActivation();

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of(target.getId(), 8));
        castAndResolveWildsize(player2, target);
        harness.passBothPriorities();

        assertThat(inferno.getMarkedDamage()).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Living Inferno");
        harness.assertInGraveyard(player2, "Gristleback");
    }

    @Test
    @DisplayName("Damage assigned to a sacrificed target is not redistributed")
    void doesNotRedistributeDamageFromIllegalTarget() {
        Permanent inferno = addCreatureReady(player1, new LivingInferno());
        Permanent sacrificed = addCreatureReady(player2, new Gristleback());
        Permanent remaining = addCreatureReady(player2, new Gristleback());
        prepareForActivation();

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(sacrificed.getId(), 7, remaining.getId(), 1));
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(remaining.getMarkedDamage()).isEqualTo(1);
        assertThat(inferno.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("Living Inferno can target its controller's creatures")
    void canTargetOwnCreature() {
        Permanent inferno = addCreatureReady(player1, new LivingInferno());
        Permanent target = addCreatureReady(player1, new Gristleback());
        prepareForActivation();

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of(target.getId(), 8));
        harness.passBothPriorities();

        assertThat(inferno.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Gristleback");
    }

    @Test
    @DisplayName("The ability does not resolve when its only target is sacrificed")
    void doesNotResolveWhenAllTargetsAreIllegal() {
        Permanent inferno = addCreatureReady(player1, new LivingInferno());
        Permanent target = addCreatureReady(player2, new Gristleback());
        prepareForActivation();

        harness.activateAbilityWithDamageAssignments(player1, 0, 0, null, Map.of(target.getId(), 8));
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(inferno.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Living Inferno");
        harness.assertInGraveyard(player2, "Gristleback");
    }

    @Test
    @DisplayName("Each chosen target must be assigned at least one damage")
    void cannotAssignZeroDamageToATarget() {
        Permanent inferno = addCreatureReady(player1, new LivingInferno());
        Permanent first = addCreatureReady(player2, new Gristleback());
        Permanent second = addCreatureReady(player2, new Gristleback());
        prepareForActivation();

        assertThatThrownBy(() -> harness.activateAbilityWithDamageAssignments(player1, 0, 0, null,
                Map.of(first.getId(), 8, second.getId(), 0)))
                .isInstanceOf(IllegalStateException.class);

        assertThat(inferno.isTapped()).isFalse();
    }

    private void castAndResolveWildsize(Player player, Permanent target) {
        harness.setHand(player, List.of(new Wildsize()));
        harness.setLibrary(player, List.of(new Gristleback()));
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player, 0, target.getId());
    }

    private void prepareForActivation() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
