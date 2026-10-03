package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrowdOfTrueBelievers.class, GrizzlyBears.class})
class CrowdOfTrueBelieversTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts a creature attacking alone and gains 1 life")
    void boostsAttackingAloneCreatureAndGainsLife() {
        addCreatureReady(player1, new CrowdOfTrueBelievers());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1));
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);
        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Cannot target a creature when multiple creatures were declared as attackers")
    void cannotTargetCreatureWhenAttackingWithMultipleCreatures() {
        addCreatureReady(player1, new CrowdOfTrueBelievers());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(1, 2));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking alone");
    }

    @Test
    void canTargetRemainingAttackerAfterOtherAttackerLeavesCombat() {
        addCreatureReady(player1, new CrowdOfTrueBelievers());
        Permanent attacker = addCreatureReady(player1, new CrowdOfTrueBelievers());
        Permanent otherAttacker = addCreatureReady(player1, new CrowdOfTrueBelievers());
        addCreatureReady(player2, new CrowdOfTrueBelievers());

        declareAttackers(List.of(1, 2));
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, otherAttacker);
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        harness.assertLife(player1, 21);
    }

    @Test
    void cannotTargetDeclaredAttackerWhenAnotherCreatureIsNowAttacking() {
        addCreatureReady(player1, new CrowdOfTrueBelievers());
        Permanent attacker = addCreatureReady(player1, new CrowdOfTrueBelievers());
        Permanent additionalAttacker = addCreatureReady(player1, new CrowdOfTrueBelievers());
        addCreatureReady(player2, new CrowdOfTrueBelievers());

        declareAttackers(List.of(1));
        additionalAttacker.setAttacking(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking alone");
    }

    @Test
    void doesNotResolveWhenTargetStopsAttacking() {
        addCreatureReady(player1, new CrowdOfTrueBelievers());
        Permanent attacker = addCreatureReady(player1, new CrowdOfTrueBelievers());
        addCreatureReady(player2, new CrowdOfTrueBelievers());

        declareAttackers(List.of(1));
        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        harness.assertLife(player1, 20);
    }

    @Test
    void doesNotResolveWhenAnotherCreatureStartsAttackingInResponse() {
        addCreatureReady(player1, new CrowdOfTrueBelievers());
        Permanent attacker = addCreatureReady(player1, new CrowdOfTrueBelievers());
        Permanent additionalAttacker = addCreatureReady(player1, new CrowdOfTrueBelievers());
        addCreatureReady(player2, new CrowdOfTrueBelievers());

        declareAttackers(List.of(1));
        harness.activateAbility(player1, 0, null, attacker.getId());
        additionalAttacker.setAttacking(true);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        harness.assertLife(player1, 20);
    }

    @Test
    void abilityStillResolvesAfterSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new CrowdOfTrueBelievers());
        Permanent attacker = addCreatureReady(player1, new CrowdOfTrueBelievers());
        addCreatureReady(player2, new CrowdOfTrueBelievers());

        declareAttackers(List.of(1));
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        harness.assertLife(player1, 21);
    }

    @Test
    void cannotTargetNonattackingCreature() {
        addCreatureReady(player1, new CrowdOfTrueBelievers());
        Permanent target = addCreatureReady(player1, new CrowdOfTrueBelievers());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking alone");
    }


    @Test
    void cannotTargetOpponentsAttackingCreature() {
        addCreatureReady(player1, new CrowdOfTrueBelievers());
        Permanent attacker = addCreatureReady(player2, new CrowdOfTrueBelievers());

        declareAttackers(player2, List.of(0));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void tappingSourcePaysCostAndPreventsSecondActivation() {
        Permanent source = addCreatureReady(player1, new CrowdOfTrueBelievers());
        Permanent attacker = addCreatureReady(player1, new CrowdOfTrueBelievers());
        addCreatureReady(player2, new CrowdOfTrueBelievers());

        declareAttackers(List.of(1));
        harness.activateAbility(player1, 0, null, attacker.getId());
        assertThat(source.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertLife(player1, 21);
    }

    @Test
    void boostExpiresAtEndOfTurnWithoutChangingToughness() {
        addCreatureReady(player1, new CrowdOfTrueBelievers());
        Permanent attacker = addCreatureReady(player1, new CrowdOfTrueBelievers());
        addCreatureReady(player2, new CrowdOfTrueBelievers());

        declareAttackers(List.of(1));
        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        harness.assertLife(player1, 21);
    }

}
