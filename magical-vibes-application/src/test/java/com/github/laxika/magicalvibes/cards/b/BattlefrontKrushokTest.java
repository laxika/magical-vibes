package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FeralKrushok;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BattlefrontKrushok.class, FeralKrushok.class})
class BattlefrontKrushokTest extends BaseCardTest {

    @Test
    @DisplayName("Battlefront Krushok cannot be blocked by two creatures")
    void cannotBeBlockedByTwoCreatures() {
        addAttackingCreature(new BattlefrontKrushok());
        addBlocker();
        addBlocker();

        beginBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Battlefront Krushok restricts creatures you control with +1/+1 counters")
    void counteredCreatureCannotBeBlockedByTwoCreatures() {
        addReadyCreature(new BattlefrontKrushok());
        Permanent attacker = addAttackingCreature(new FeralKrushok());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addBlocker();
        addBlocker();

        beginBlockerDeclaration();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1)
        )))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked by more than 1 creature");
    }

    @Test
    @DisplayName("Battlefront Krushok does not restrict creatures without +1/+1 counters")
    void creatureWithoutCounterCanBeBlockedByTwoCreatures() {
        addReadyCreature(new BattlefrontKrushok());
        addAttackingCreature(new FeralKrushok());
        addBlocker();
        addBlocker();

        beginBlockerDeclaration();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1),
                new BlockerAssignment(1, 1)
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void krushokCanBeBlockedByOneCreature() {
        addAttackingCreature(new BattlefrontKrushok());
        addBlocker();
        beginBlockerDeclaration();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0)))).doesNotThrowAnyException();
    }

    @Test
    void counteredCreatureCanBeBlockedByOneCreature() {
        addReadyCreature(new BattlefrontKrushok());
        Permanent attacker = addAttackingCreature(new FeralKrushok());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        addBlocker();
        beginBlockerDeclaration();

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 1)))).doesNotThrowAnyException();
    }

    @Test
    void opponentsKrushokDoesNotRestrictAttackerWithCounter() {
        Permanent attacker = addAttackingCreature(new FeralKrushok());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addToBattlefield(player2, new BattlefrontKrushok());
        addBlocker();
        addBlocker();
        beginBlockerDeclaration();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0), new BlockerAssignment(2, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    void losingLastCounterRemovesBlockerRestriction() {
        addReadyCreature(new BattlefrontKrushok());
        Permanent attacker = addAttackingCreature(new FeralKrushok());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        addBlocker();
        addBlocker();
        beginBlockerDeclaration();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 1), new BlockerAssignment(1, 1))))
                .doesNotThrowAnyException();
    }

    @Test
    void restrictionEndsWhenKrushokLeavesBattlefield() {
        Permanent krushok = addReadyCreature(new BattlefrontKrushok());
        Permanent attacker = addAttackingCreature(new FeralKrushok());
        attacker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        addBlocker();
        addBlocker();
        gd.playerBattlefields.get(player1.getId()).remove(krushok);
        gd.playerGraveyards.get(player1.getId()).add(krushok.getCard());
        beginBlockerDeclaration();

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .doesNotThrowAnyException();
    }

    private Permanent addReadyCreature(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addAttackingCreature(Card card) {
        Permanent permanent = addReadyCreature(card);
        permanent.setAttacking(true);
        return permanent;
    }

    private void addBlocker() {
        Permanent permanent = harness.addToBattlefieldAndReturn(player2, new FeralKrushok());
        permanent.setSummoningSick(false);
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
