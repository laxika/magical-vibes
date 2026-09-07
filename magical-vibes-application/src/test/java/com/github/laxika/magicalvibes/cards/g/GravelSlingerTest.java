package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GravelSlinger.class, GrizzlyBears.class})
class GravelSlingerTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to an attacking creature")
    void dealsDamageToAttackingCreature() {
        Permanent slinger = addReadySlinger(player1);
        Permanent attacker = addCombatCreature(player2, true);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(slinger.isTapped()).isTrue();
        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Deals 1 damage to a blocking creature")
    void dealsDamageToBlockingCreature() {
        Permanent slinger = addReadySlinger(player1);
        Permanent blocker = addCombatCreature(player2, false);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(slinger.isTapped()).isTrue();
        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking or blocking")
    void cannotTargetNonCombatCreature() {
        addReadySlinger(player1);
        Permanent bystander = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking or blocking");
    }

    @Test
    void canBeTurnedFaceUpForItsMorphCost() {
        harness.setHand(player1, List.of(new GravelSlinger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent slinger = findPermanent(player1, "Gravel Slinger");
        assertThat(slinger.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(slinger));
        harness.passBothPriorities();

        assertThat(slinger.isFaceDown()).isFalse();
    }

    private Permanent addReadySlinger(Player player) {
        Permanent slinger = harness.addToBattlefieldAndReturn(player, new GravelSlinger());
        slinger.setSummoningSick(false);
        return slinger;
    }

    private Permanent addCombatCreature(Player player, boolean attacking) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new GrizzlyBears());
        creature.setSummoningSick(false);
        if (attacking) {
            creature.setAttacking(true);
        } else {
            creature.setBlocking(true);
        }
        return creature;
    }
}
