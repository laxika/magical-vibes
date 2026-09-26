package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VedalkenOrrery;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrizzlyBears.class, OverpoweringAttack.class, VedalkenOrrery.class})
class OverpoweringAttackTest extends BaseCardTest {

    @Test
    @DisplayName("Untaps only attacked creatures controlled by its caster")
    void untapsOnlyAttackedControlledCreatures() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttackedThisTurn(true);
        attacker.tap();
        nonAttacker.tap();

        castNormallyFromMainPhase();

        assertThat(attacker.isTapped()).isFalse();
        assertThat(nonAttacker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Adds a combat and main phase when cast during a main phase")
    void addsCombatAndMainPhaseDuringMainPhase() {
        castNormallyFromMainPhase();

        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
    }

    @Test
    @DisplayName("Freerunning casts for {2}{R} after qualifying combat damage")
    void castsForFreerunningAfterAssassinDamage() {
        gd.combatDamageToPlayerControllerSubtypesThisTurn
                .computeIfAbsent(player1.getId(), ignored -> ConcurrentHashMap.newKeySet())
                .add(CardSubtype.ASSASSIN);
        harness.setHand(player1, List.of(new OverpoweringAttack()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
    }

    @Test
    @DisplayName("Freerunning requires qualifying combat damage")
    void freerunningRequiresQualifyingCombatDamage() {
        harness.setHand(player1, List.of(new OverpoweringAttack()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("condition is not met");
    }

    @Test
    @DisplayName("Does not add phases when cast outside a main phase")
    void doesNotAddPhasesOutsideMainPhase() {
        harness.addToBattlefield(player1, new VedalkenOrrery());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new OverpoweringAttack()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castFromHand(player1, new OverpoweringAttack(), "{3}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.additionalCombatMainPhasePairs).isZero();
    }

    private void castNormallyFromMainPhase() {
        harness.setHand(player1, List.of(new OverpoweringAttack()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
    }
}
