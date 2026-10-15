package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.b.BlazingArchon;
import com.github.laxika.magicalvibes.cards.g.GideonJura;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({Taunt.class, GrizzlyBears.class, BlazingArchon.class, GideonJura.class})
class TauntTest extends BaseCardTest {

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Casting Taunt puts it on the stack targeting the chosen player")
    void castingTargetsPlayer() {
        harness.setHand(player1, List.of(new Taunt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
        assertThat(entry.getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Resolving registers the delayed must-attack requirement for the target's next turn")
    void resolvingRegistersPending() {
        harness.setHand(player1, List.of(new Taunt()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.tauntedNextTurn).containsEntry(player2.getId(), player1.getId());
    }

    @Test
    @DisplayName("The requirement activates when the taunted player's turn begins and the pending entry is consumed")
    void activatesOnTargetPlayerTurn() {
        gd.tauntedNextTurn.put(player2.getId(), player1.getId());

        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player2.getId());
        assertThat(gd.tauntedThisTurn).containsEntry(player2.getId(), player1.getId());
        assertThat(gd.tauntedNextTurn).isEmpty();
    }

    @Test
    @DisplayName("A taunted player's creature that can attack becomes a must-attack requirement")
    void tauntedCreatureMustAttack() {
        gd.tauntedThisTurn.put(player2.getId(), player1.getId());
        addCreatureReady(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        List<Integer> attackable = harness.getCombatAttackService()
                .getAttackableCreatureIndices(gd, player2.getId());
        assertThat(harness.getCombatAttackService()
                .getMustAttackIndices(gd, player2.getId(), attackable)).contains(0);

        // Declaring no attackers is illegal — the creature must attack.
        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The taunted creature attacks the taunter, dealing them combat damage")
    void tauntedCreatureAttacksTaunter() {
        harness.setLife(player1, 20);
        gd.tauntedThisTurn.put(player2.getId(), player1.getId());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A creature that cannot attack is not required to attack")
    void unableCreatureCanSkipAttack() {
        gd.tauntedThisTurn.put(player2.getId(), player1.getId());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        bear.tap();

        declareAttackers(player2, List.of());

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The requirement wears off when the taunted player's turn ends")
    void requirementWearsOff() {
        gd.tauntedThisTurn.put(player2.getId(), player1.getId());
        harness.forceActivePlayer(player2);

        advanceTurn();

        assertThat(gd.activePlayerId).isEqualTo(player1.getId());
        assertThat(gd.tauntedThisTurn).isEmpty();
    }

    @Test
    @DisplayName("Resolving Taunt forces creatures gained afterward to attack on the next turn")
    void creaturesGainedAfterResolutionMustAttack() {
        harness.setHand(player1, List.of(new Taunt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        addCreatureReady(player2, new GrizzlyBears());
        advanceTurn();

        assertThatThrownBy(() -> declareAttackers(player2, List.of()))
                .isInstanceOf(IllegalStateException.class);
        declareAttackers(player2, List.of(0));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A creature entering during the taunted turn is exempt while summoning sick")
    void summoningSickCreatureNeedNotAttack() {
        harness.setHand(player1, List.of(new Taunt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        advanceTurn();
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThatCode(() -> declareAttackers(player2, List.of())).doesNotThrowAnyException();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Targeting yourself does not require creatures to attack an opponent")
    void selfTargetDoesNotForceAttackOnOpponent() {
        harness.setHand(player1, List.of(new Taunt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, player1.getId());
        addCreatureReady(player1, new GrizzlyBears());
        advanceTurn();
        advanceTurn();

        assertThatCode(() -> declareAttackers(player1, List.of())).doesNotThrowAnyException();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("When the taunter cannot be attacked, creatures may attack their planeswalker")
    void mayAttackPlaneswalkerWhenTaunterCannotBeAttacked() {
        harness.addToBattlefield(player1, new BlazingArchon());
        Permanent gideon = harness.addToBattlefieldAndReturn(player1, new GideonJura());
        gideon.setCounterCount(CounterType.LOYALTY, 6);
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Taunt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());
        advanceTurn();

        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        assertThatCode(() -> gs.declareAttackers(gd, player2, List.of(0), Map.of(0, gideon.getId())))
                .doesNotThrowAnyException();
    }
}
