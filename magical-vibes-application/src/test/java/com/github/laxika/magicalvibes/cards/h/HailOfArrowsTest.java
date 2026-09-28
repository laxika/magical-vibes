package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.i.InnerChamberGuard;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HailOfArrows.class, HandOfHonor.class, InnerChamberGuard.class})
class HailOfArrowsTest extends BaseCardTest {

    @Test
    @DisplayName("Divides X damage among attacking creatures as chosen")
    void dividesDamageAmongAttackingCreatures() {
        Permanent first = addAttacker(new HandOfHonor());
        Permanent second = addAttacker(new InnerChamberGuard());
        prepareHail(2);

        harness.castInstantForX(player1, 0, 2, Map.of(first.getId(), 1, second.getId(), 1));
        harness.passBothPriorities();

        assertThat(first.getMarkedDamage()).isEqualTo(1);
        assertThat(second.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Hand of Honor");
        harness.assertOnBattlefield(player2, "Inner-Chamber Guard");
        harness.assertInGraveyard(player1, "Hail of Arrows");
    }

    @Test
    @DisplayName("X=0 can be cast without targets")
    void zeroDamageRequiresNoTargets() {
        prepareHail(0);

        harness.castInstantForX(player1, 0, 0, Map.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Hail of Arrows");
    }

    @Test
    @DisplayName("Rejects a nonattacking creature as a target")
    void rejectsNonattackingCreature() {
        Permanent idle = addCreatureReady(player2, new HandOfHonor());
        prepareHail(1);

        assertThatThrownBy(() ->
                harness.castInstantForX(player1, 0, 1, Map.of(idle.getId(), 1))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a player as a target")
    void rejectsPlayerTarget() {
        prepareHail(1);

        assertThatThrownBy(() ->
                harness.castInstantForX(player1, 0, 1, Map.of(player2.getId(), 1))
        ).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An attacker that stops attacking before resolution is no longer legal")
    void ignoresAttackerThatStopsAttacking() {
        Permanent attacker = addAttacker(new HandOfHonor());
        prepareHail(2);

        harness.castInstantForX(player1, 0, 2, Map.of(attacker.getId(), 2));
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Hand of Honor");
    }

    @Test
    @DisplayName("Does not reassign damage from an attacker that becomes illegal")
    void doesNotReassignDamageFromIllegalAttacker() {
        Permanent legalAttacker = addAttacker(new InnerChamberGuard());
        Permanent illegalAttacker = addAttacker(new HandOfHonor());
        prepareHail(3);

        harness.castInstantForX(player1, 0, 3,
                Map.of(legalAttacker.getId(), 1, illegalAttacker.getId(), 2));
        illegalAttacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(legalAttacker.getMarkedDamage()).isEqualTo(1);
        assertThat(illegalAttacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Assignments must sum to X")
    void assignmentsMustSumToX() {
        Permanent attacker = addAttacker(new HandOfHonor());
        prepareHail(2);

        assertThatThrownBy(() ->
                harness.castInstantForX(player1, 0, 2, Map.of(attacker.getId(), 1))
        ).isInstanceOf(IllegalStateException.class);
    }

    private Permanent addAttacker(Card card) {
        Permanent attacker = addCreatureReady(player2, card);
        attacker.setAttacking(true);
        return attacker;
    }

    private void prepareHail(int xValue) {
        harness.setHand(player1, List.of(new HailOfArrows()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
    }
}
