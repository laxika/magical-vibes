package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BorosElite.class})
class BorosEliteTest extends BaseCardTest {

    @Test
    @DisplayName("Does not get a bonus when attacking with fewer than two other creatures")
    void noBonusWithFewerThanTwoOtherAttackers() {
        Permanent elite = addCreatureReady(player1, new BorosElite());
        addCreatureReady(player1, new BorosElite());

        declareAttackers(player1, List.of(0, 1));
        resolveAllTriggers();

        assertThat(elite.getPowerModifier()).isZero();
        assertThat(elite.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Gets +2/+2 when attacking with two other creatures")
    void getsBonusWithTwoOtherAttackers() {
        Permanent elite = addCreatureReady(player1, new BorosElite());
        addCreatureReady(player1, new BorosElite());
        addCreatureReady(player1, new BorosElite());

        declareAttackers(player1, List.of(0, 1, 2));
        resolveAllTriggers();

        assertThat(elite.getPowerModifier()).isEqualTo(2);
        assertThat(elite.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Non-attacking creatures do not count toward battalion")
    void nonAttackingCreaturesDoNotCount() {
        Permanent elite = addCreatureReady(player1, new BorosElite());
        addCreatureReady(player1, new BorosElite());
        addCreatureReady(player1, new BorosElite());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(elite.getPowerModifier()).isZero();
        assertThat(elite.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An Elite that stays behind does not get battalion from three other attackers")
    void nonAttackingEliteDoesNotGetBonus() {
        Permanent elite = addCreatureReady(player1, new BorosElite());
        addCreatureReady(player1, new BorosElite());
        addCreatureReady(player1, new BorosElite());
        addCreatureReady(player1, new BorosElite());

        declareAttackers(List.of(1, 2, 3));
        resolveAllTriggers();

        assertThat(elite.getPowerModifier()).isZero();
        assertThat(elite.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Battalion still resolves after the other creatures leave combat")
    void bonusSurvivesOtherAttackersLeavingCombat() {
        Permanent elite = addCreatureReady(player1, new BorosElite());
        Permanent second = addCreatureReady(player1, new BorosElite());
        Permanent third = addCreatureReady(player1, new BorosElite());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0, 1, 2)));
        assertThat(gd.stack).hasSize(3);
        second.setAttacking(false);
        third.setAttacking(false);
        resolveAllTriggers();

        assertThat(elite.getPowerModifier()).isEqualTo(2);
        assertThat(elite.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Four attackers grant only +2/+2, which expires after the turn")
    void bonusWithMoreAttackersExpiresAfterTurn() {
        Permanent elite = addCreatureReady(player1, new BorosElite());
        addCreatureReady(player1, new BorosElite());
        addCreatureReady(player1, new BorosElite());
        addCreatureReady(player1, new BorosElite());

        declareAttackers(List.of(0, 1, 2, 3));
        resolveAllTriggers();

        assertThat(elite.getPowerModifier()).isEqualTo(2);
        assertThat(elite.getToughnessModifier()).isEqualTo(2);

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(elite.getPowerModifier()).isZero();
        assertThat(elite.getToughnessModifier()).isZero();
    }
}
