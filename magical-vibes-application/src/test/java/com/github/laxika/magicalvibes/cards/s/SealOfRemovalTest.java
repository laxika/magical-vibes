package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.MoggToady;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SealOfRemoval.class, MoggToady.class})
class SealOfRemovalTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an opponent's target creature to its owner's hand")
    void returnsOpponentsCreatureToItsOwnersHand() {
        harness.addToBattlefield(player1, new SealOfRemoval());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoggToady());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mogg Toady");
        harness.assertInHand(player2, "Mogg Toady");
        harness.assertInGraveyard(player1, "Seal of Removal");
    }

    @Test
    @DisplayName("Returns your target creature to its owner's hand")
    void returnsOwnCreatureToItsOwnersHand() {
        harness.addToBattlefield(player1, new SealOfRemoval());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new MoggToady());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mogg Toady");
        harness.assertInHand(player1, "Mogg Toady");
    }

    @Test
    @DisplayName("Sacrifices itself as the activation cost")
    void sacrificesItselfAsActivationCost() {
        harness.addToBattlefield(player1, new SealOfRemoval());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoggToady());

        harness.activateAbility(player1, 0, null, target.getId());

        harness.assertNotOnBattlefield(player1, "Seal of Removal");
        harness.assertInGraveyard(player1, "Seal of Removal");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        harness.addToBattlefield(player1, new SealOfRemoval());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new SealOfRemoval());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Returns a target to its owner's hand when its owner differs from the battlefield holder")
    void returnsTargetToItsOwnersHandWhenOwnerDiffersFromBattlefieldHolder() {
        harness.addToBattlefield(player1, new SealOfRemoval());
        MoggToady targetCard = new MoggToady();
        targetCard.setOwnerId(player1.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, targetCard);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Mogg Toady");
        harness.assertInHand(player1, "Mogg Toady");
        harness.assertNotInHand(player2, "Mogg Toady");
    }

    @Test
    @DisplayName("Ability fizzles if its target leaves before resolution")
    void abilityFizzlesIfTargetLeavesBeforeResolution() {
        harness.addToBattlefield(player1, new SealOfRemoval());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoggToady());

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
        harness.assertNotInHand(player2, "Mogg Toady");
        harness.assertInGraveyard(player1, "Seal of Removal");
    }
}
