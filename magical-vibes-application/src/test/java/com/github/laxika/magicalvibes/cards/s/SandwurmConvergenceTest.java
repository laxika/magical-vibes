package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SandwurmConvergenceTest extends BaseCardTest {

    // ===== Static: creatures with flying can't attack you or your planeswalkers =====

    @Test
    @DisplayName("Flying creature can't attack the controller")
    void flyerCantAttackController() {
        harness.addToBattlefield(player2, new SandwurmConvergence());
        addCreatureReady(player1, new SuntailHawk()); // flyer, index 0

        beginAttack(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't attack");
    }

    @Test
    @DisplayName("Non-flying creature can still attack the controller")
    void nonFlyerCanAttackController() {
        harness.addToBattlefield(player2, new SandwurmConvergence());
        addCreatureReady(player1, new GrizzlyBears()); // ground creature, index 0

        beginAttack(player1);

        // Not throwing proves the ground creature may attack the Sandwurm Convergence controller.
        gs.declareAttackers(gd, player1, List.of(0));
    }

    @Test
    @DisplayName("Flying creature can't attack a planeswalker the controller controls")
    void flyerCantAttackControllersPlaneswalker() {
        harness.addToBattlefield(player2, new SandwurmConvergence());
        Permanent planeswalker = addPlaneswalker(player2, 4);
        addCreatureReady(player1, new SuntailHawk()); // flyer, index 0

        beginAttack(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't attack");
    }

    @Test
    @DisplayName("Non-flying creature can still attack a planeswalker the controller controls")
    void nonFlyerCanAttackControllersPlaneswalker() {
        harness.addToBattlefield(player2, new SandwurmConvergence());
        Permanent planeswalker = addPlaneswalker(player2, 4);
        addCreatureReady(player1, new GrizzlyBears()); // ground creature, index 0

        beginAttack(player1);

        // The ground creature is unaffected — the restriction only bars flyers.
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
    }

    // ===== End step: create a 5/5 green Wurm token =====

    @Test
    @DisplayName("At the controller's end step, creates a 5/5 green Wurm token")
    void endStepCreatesWurmToken() {
        harness.addToBattlefield(player1, new SandwurmConvergence());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to END_STEP, trigger fires onto stack
        harness.passBothPriorities(); // resolve trigger

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).hasSize(1);
        Permanent wurm = tokens.getFirst();
        assertThat(wurm.getCard().getPower()).isEqualTo(5);
        assertThat(wurm.getCard().getToughness()).isEqualTo(5);
        assertThat(wurm.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(wurm.getCard().getSubtypes()).contains(CardSubtype.WURM);
    }

    @Test
    @DisplayName("Does not create a token at the opponent's end step")
    void noTokenOnOpponentEndStep() {
        harness.addToBattlefield(player1, new SandwurmConvergence());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to player2's END_STEP
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .toList();

        assertThat(tokens).isEmpty();
    }

    // ===== Helpers =====

    private void beginAttack(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private Permanent addPlaneswalker(Player player, int loyalty) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        card.setLoyalty(loyalty);
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
