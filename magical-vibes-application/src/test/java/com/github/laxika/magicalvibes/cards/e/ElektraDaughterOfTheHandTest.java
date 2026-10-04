package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ElektraDaughterOfTheHand.class, AirElemental.class, GrizzlyBears.class, HillGiant.class})
class ElektraDaughterOfTheHandTest extends BaseCardTest {

    @Test
    void destroysTargetCreatureAnOpponentControlsWithPowerThreeOrLess() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ElektraDaughterOfTheHand()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void cannotTargetCreatureWithPowerGreaterThanThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new ElektraDaughterOfTheHand()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3 or less");
    }

    @Test
    void cannotTargetCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ElektraDaughterOfTheHand()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    @Test
    void sneakReturnsUnblockedAttackerAndEntersTappedAndAttackingAtTheSamePlayer() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ElektraDaughterOfTheHand()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of(attacker.getId()));
        harness.passBothPriorities();

        Permanent elektra = findPermanent(player1, "Elektra, Daughter of the Hand");
        assertThat(elektra.isTapped()).isTrue();
        assertThat(elektra.isAttacking()).isTrue();
        assertThat(elektra.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerHands.get(player1.getId())).contains(attacker.getCard());
        resolveAllTriggers();
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void normalCastWithoutEligibleTargetsStillEntersUntappedAndNotAttacking() {
        harness.setHand(player1, List.of(new ElektraDaughterOfTheHand()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent elektra = findPermanent(player1, "Elektra, Daughter of the Hand");
        assertThat(elektra.isTapped()).isFalse();
        assertThat(elektra.isAttacking()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotDestroyTargetWhosePowerExceedsThreeBeforeTriggerResolves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ElektraDaughterOfTheHand()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        target.setPowerModifier(1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target.getCard());
    }

    @Test
    void canDestroyCreatureWhoseEffectivePowerIsReducedToThree() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        target.setPowerModifier(-1);
        harness.setHand(player1, List.of(new ElektraDaughterOfTheHand()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Air Elemental");
    }

    @Test
    void cannotPaySneakCostWithABlockedAttacker() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        blocker.setBlocking(true);
        blocker.getBlockingTargetIds().add(attacker.getId());
        harness.setHand(player1, List.of(new ElektraDaughterOfTheHand()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, blocker.getId(), List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(ElektraDaughterOfTheHand.class::isInstance);
    }

    @Test
    void cannotSneakDuringCombatDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new ElektraDaughterOfTheHand()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        assertThatThrownBy(() -> harness.castInstantWithAlternateCost(
                player1, 0, target.getId(), List.of(attacker.getId())))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(ElektraDaughterOfTheHand.class::isInstance);
    }
}
