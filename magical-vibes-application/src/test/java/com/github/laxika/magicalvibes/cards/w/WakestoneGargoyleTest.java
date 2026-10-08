package com.github.laxika.magicalvibes.cards.w;

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

@CardUsed({WakestoneGargoyle.class})
class WakestoneGargoyleTest extends BaseCardTest {

    @Test
    @DisplayName("Defender creatures cannot attack before the ability resolves")
    void defenderCreaturesCannotAttackBeforeActivation() {
        addCreatureReady(player1, new WakestoneGargoyle());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The ability lets your defender creatures attack")
    void abilityLetsYourDefenderCreaturesAttack() {
        Permanent gargoyle = addCreatureReady(player1, new WakestoneGargoyle());
        Permanent otherGargoyle = addCreatureReady(player1, new WakestoneGargoyle());
        addCreatureReady(player2, new WakestoneGargoyle());
        activateAbility();

        declareAttackers(List.of(0, 1));

        assertThat(gargoyle.isAttacking()).isTrue();
        assertThat(otherGargoyle.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The ability also affects a defender that enters later this turn")
    void abilityAffectsLaterDefender() {
        addCreatureReady(player1, new WakestoneGargoyle());
        addCreatureReady(player2, new WakestoneGargoyle());
        activateAbility();
        Permanent otherGargoyle = addCreatureReady(player1, new WakestoneGargoyle());

        declareAttackers(List.of(0, 1));

        assertThat(otherGargoyle.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The ability does not bypass summoning sickness")
    void abilityDoesNotBypassSummoningSickness() {
        addCreatureReady(player1, new WakestoneGargoyle());
        activateAbility();
        harness.addToBattlefieldAndReturn(player1, new WakestoneGargoyle());

        assertThatThrownBy(() -> declareAttackers(List.of(1)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The ability does not affect an opponent's defender creature")
    void abilityDoesNotAffectOpponentDefender() {
        addCreatureReady(player1, new WakestoneGargoyle());
        addCreatureReady(player2, new WakestoneGargoyle());
        activateAbility();

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("The attack permission expires at end of turn")
    void attackPermissionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new WakestoneGargoyle());
        activateAbility();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Gargoyle can activate the ability")
    void tappedSummoningSickSourceCanActivateAbility() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new WakestoneGargoyle());
        source.tap();
        Permanent defender = addCreatureReady(player1, new WakestoneGargoyle());

        activateAbility();
        declareAttackers(List.of(1));

        assertThat(defender.isAttacking()).isTrue();
        assertThat(source.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability does not allow a tapped defender to attack")
    void abilityDoesNotBypassTappedRestriction() {
        Permanent gargoyle = addCreatureReady(player1, new WakestoneGargoyle());
        gargoyle.tap();
        activateAbility();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    private void activateAbility() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }
}
