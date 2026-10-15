package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.Telepathy;
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

@CardUsed({JeeringHomunculus.class, GrizzlyBears.class, Telepathy.class})
class JeeringHomunculusTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB may goads the target creature")
    void acceptingEtbMayGoadsTargetCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        resolveGoad(bears);

        assertThat(als.getMustAttackRequirementCount(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the ETB may does not goad the target creature")
    void decliningEtbMayDoesNotGoadTargetCreature() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        castJeeringHomunculus();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(als.getMustAttackRequirementCount(gd, bears)).isZero();
    }

    @Test
    @DisplayName("The ETB may can target only a creature")
    void etbMayRejectsNoncreatureTarget() {
        harness.addToBattlefield(player2, new Telepathy());

        castJeeringHomunculus();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(
                player1, harness.getPermanentId(player2, "Telepathy")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @CardUsed({JeeringHomunculus.class})
    @DisplayName("The ETB ability can goad Jeering Homunculus itself")
    void canGoadItself() {
        castJeeringHomunculus();
        harness.passBothPriorities();
        Permanent homunculus = findPermanent(player1, "Jeering Homunculus");
        harness.handlePermanentChosen(player1, homunculus.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gqs.isGoaded(gd, homunculus)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, homunculus)).isEqualTo(1);
    }

    @Test
    @DisplayName("A goaded creature must attack but can attack the goading player in a two-player game")
    void goadedCreatureMustAttackInTwoPlayerGame() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        resolveGoad(bears);

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        declareAttackers(player2, List.of(0));

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A tapped goaded creature is not required to attack")
    void tappedGoadedCreatureNeedNotAttack() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        resolveGoad(bears);
        bears.tap();

        declareAttackers(player2, List.of());

        assertThat(bears.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("Goad lasts through the opponent's turn and expires when your next turn begins")
    void goadExpiresAtControllersNextTurn() {
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        resolveGoad(bears);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(gqs.isGoaded(gd, bears)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
        assertThat(gqs.isGoaded(gd, bears)).isFalse();
        assertThat(als.getMustAttackRequirementCount(gd, bears)).isZero();
    }

    private void resolveGoad(Permanent target) {
        castJeeringHomunculus();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    private void castJeeringHomunculus() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new JeeringHomunculus()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
    }
}
