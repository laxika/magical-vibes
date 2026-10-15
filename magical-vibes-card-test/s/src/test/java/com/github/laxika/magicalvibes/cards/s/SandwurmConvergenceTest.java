package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenInitiate;
import com.github.laxika.magicalvibes.cards.a.AwakenedSkyclave;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.g.GideonOfTheTrials;
import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SandwurmConvergence.class, AvenInitiate.class, DuneBeetle.class, GideonOfTheTrials.class})
class SandwurmConvergenceTest extends BaseCardTest {

    @Test
    @DisplayName("Flying creature can't attack the controller")
    void flyerCantAttackController() {
        harness.addToBattlefield(player2, new SandwurmConvergence());
        addCreatureReady(player1, new AvenInitiate()); // flyer, index 0

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Non-flying creature can still attack the controller")
    void nonFlyerCanAttackController() {
        harness.addToBattlefield(player2, new SandwurmConvergence());
        addCreatureReady(player1, new DuneBeetle()); // ground creature, index 0

        declareAttackers(player1, List.of(0));
    }

    @Test
    @DisplayName("Flying creature can't attack a planeswalker the controller controls")
    void flyerCantAttackControllersPlaneswalker() {
        harness.addToBattlefield(player2, new SandwurmConvergence());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GideonOfTheTrials());
        planeswalker.setCounterCount(CounterType.LOYALTY, planeswalker.getCard().getLoyalty());
        addCreatureReady(player1, new AvenInitiate()); // flyer, index 0

        beginAttack(player1);

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Non-flying creature can still attack a planeswalker the controller controls")
    void nonFlyerCanAttackControllersPlaneswalker() {
        harness.addToBattlefield(player2, new SandwurmConvergence());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new GideonOfTheTrials());
        planeswalker.setCounterCount(CounterType.LOYALTY, planeswalker.getCard().getLoyalty());
        addCreatureReady(player1, new DuneBeetle()); // ground creature, index 0

        beginAttack(player1);

        // The ground creature is unaffected — the restriction only bars flyers.
        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, planeswalker.getId()));
    }

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
        assertThat(wurm.getCard().hasType(CardType.CREATURE)).isTrue();
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

    private void beginAttack(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    @Test
    @CardUsed({InvasionOfZendikar.class, AwakenedSkyclave.class})
    @DisplayName("Flying creature can attack its controller's Siege despite Sandwurm Convergence")
    void flyerCanAttackOwnBattle() {
        addCreatureReady(player1, new AvenInitiate());
        harness.addToBattlefield(player1, new SandwurmConvergence());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());
        battle.setCounterCount(CounterType.DEFENSE, battle.getCard().getDefense());

        beginAttack(player1);

        gs.declareAttackers(gd, player1, List.of(0), Map.of(0, battle.getId()));
    }

    @Test
    @DisplayName("End-step trigger creates a token even after its source leaves")
    void triggerSurvivesSourceRemoval() {
        Permanent convergence = harness.addToBattlefieldAndReturn(player1, new SandwurmConvergence());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(convergence);
        gd.playerGraveyards.get(player1.getId()).add(convergence.getCard());
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Wurm")).isEqualTo(1);
        assertThat(countPermanents(player2, "Wurm")).isZero();
    }

    @Test
    @DisplayName("Each copy creates a token at its controller's end step")
    void eachCopyCreatesToken() {
        harness.addToBattlefield(player1, new SandwurmConvergence());
        harness.addToBattlefield(player1, new SandwurmConvergence());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Wurm")).isEqualTo(2);
        assertThat(countPermanents(player2, "Wurm")).isZero();
    }
}
