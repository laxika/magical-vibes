package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.r.RuinationWurm;
import com.github.laxika.magicalvibes.cards.z.ZhurTaaSwine;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArrowsOfJustice.class, ZhurTaaSwine.class, RuinationWurm.class})
class ArrowsOfJusticeTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 4 damage to an attacking creature, killing it")
    void killsAttacker() {
        harness.forceActivePlayer(player2);
        Permanent attacker = addCreatureReady(player2, new ZhurTaaSwine());
        attacker.setAttacking(true);
        castArrows(attacker);

        harness.assertInGraveyard(player2, "Zhur-Taa Swine");
    }

    @Test
    @DisplayName("A blocking creature is a legal target")
    void killsBlocker() {
        harness.forceActivePlayer(player1);
        Permanent blocker = addCreatureReady(player2, new ZhurTaaSwine());
        blocker.setBlocking(true);
        castArrows(blocker);

        harness.assertInGraveyard(player2, "Zhur-Taa Swine");
    }

    @Test
    @DisplayName("Cannot target a creature that is neither attacking nor blocking")
    void cannotTargetIdleCreature() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new ZhurTaaSwine());
        attacker.setAttacking(true);
        Permanent idle = addCreatureReady(player2, new ZhurTaaSwine());
        harness.setHand(player1, List.of(new ArrowsOfJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, idle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking or blocking creature");
    }

    @Test
    @DisplayName("Deals exactly four damage to a creature that survives")
    void dealsExactlyFourDamage() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new RuinationWurm());
        attacker.setAttacking(true);

        castArrows(attacker);

        harness.assertOnBattlefield(player1, "Ruination Wurm");
        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("White mana can pay the hybrid cost when targeting your own attacker")
    void whiteManaCanPayHybridCost() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new ZhurTaaSwine());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new ArrowsOfJustice()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertInGraveyard(player1, "Zhur-Taa Swine");
    }

    @Test
    @DisplayName("Deals no damage if the target stops attacking before resolution")
    void targetLeavingCombatIsIllegalOnResolution() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addCreatureReady(player1, new ZhurTaaSwine());
        attacker.setAttacking(true);
        harness.setHand(player1, List.of(new ArrowsOfJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.castInstant(player1, 0, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zhur-Taa Swine");
        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player1, "Arrows of Justice");
        assertThat(gd.stack).isEmpty();
    }

    private void castArrows(final Permanent target) {
        harness.setHand(player1, List.of(new ArrowsOfJustice()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
