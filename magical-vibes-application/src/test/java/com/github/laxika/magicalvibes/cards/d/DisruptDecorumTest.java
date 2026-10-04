package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisruptDecorum.class, GrizzlyBears.class})
class DisruptDecorumTest extends BaseCardTest {

    @Test
    @DisplayName("Goads all opposing creatures and not creatures controlled by the caster")
    void goadsOpposingCreaturesOnly() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        castAndResolve();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Does not goad creatures that enter after the spell resolves")
    void doesNotGoadLaterCreatures() {
        castAndResolve();
        addCreatureReady(player2, new GrizzlyBears());

        assertThatCode(() -> declareAttackers(player2, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Goad expires at the caster's next turn")
    void goadExpiresAtCastersNextTurn() {
        addCreatureReady(player2, new GrizzlyBears());
        castAndResolve();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        declareAttackers(player2, List.of());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Every opposing creature that can attack must attack")
    void allOpposingCreaturesMustAttack() {
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        castAndResolve();

        assertThatThrownBy(() -> declareAttackers(player2, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A goaded creature can attack the caster when no other opponent exists")
    void canAttackCasterInTwoPlayerGame() {
        addCreatureReady(player2, new GrizzlyBears());
        castAndResolve();

        assertThatCode(() -> declareAttackers(player2, List.of(0))).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Tapped and summoning-sick creatures are not required to attack")
    void creaturesUnableToAttackMayStayBack() {
        addCreatureReady(player2, new GrizzlyBears()).tap();
        addCreatureReady(player2, new GrizzlyBears()).setSummoningSick(true);
        castAndResolve();

        assertThatCode(() -> declareAttackers(player2, List.of())).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("A creature goaded while tapped must attack once it becomes able")
    void tappedCreatureIsStillGoaded() {
        var creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();
        castAndResolve();
        creature.untap();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("Goad requires attacking again in an additional combat when able")
    void mustAttackInEachCombat() {
        var creature = addCreatureReady(player2, new GrizzlyBears());
        castAndResolve();
        declareAttackers(player2, List.of(0));
        creature.untap();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    @Test
    @DisplayName("A goaded creature remains goaded after changing controllers")
    void goadSurvivesControlChange() {
        var creature = addCreatureReady(player2, new GrizzlyBears());
        castAndResolve();
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerBattlefields.get(player1.getId()).add(creature);

        assertThatThrownBy(() -> declareAttackers(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");
    }

    private void castAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DisruptDecorum()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
