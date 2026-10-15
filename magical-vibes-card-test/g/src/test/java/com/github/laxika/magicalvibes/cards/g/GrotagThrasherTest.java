package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrotagThrasher.class, GrizzlyBears.class, FountainOfYouth.class})
class GrotagThrasherTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes the chosen creature unable to block this turn")
    void attackMakesTargetUnableToBlock() {
        addCreatureReady(player1, new GrotagThrasher());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.isCantBlockThisTurn()).isTrue();

        prepareDeclareBlockers();
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack trigger can target a creature controlled by its controller")
    void canTargetOwnCreature() {
        addCreatureReady(player1, new GrotagThrasher());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new GrotagThrasher());
        addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new FountainOfYouth());
        UUID fountainId = harness.getPermanentId(player2, "Fountain of Youth");

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, fountainId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    @DisplayName("The attack trigger can target Grotag Thrasher itself with no opposing creatures")
    void canTargetItself() {
        Permanent thrasher = addCreatureReady(player1, new GrotagThrasher());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, thrasher.getId());
        harness.passBothPriorities();

        assertThat(thrasher.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Only the targeted creature is prevented from blocking")
    void otherCreaturesCanStillBlock() {
        addCreatureReady(player1, new GrotagThrasher());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherBlocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
        assertThat(otherBlocker.isCantBlockThisTurn()).isFalse();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
    }

    @Test
    @DisplayName("The blocking restriction lasts through the end step and expires before the next turn")
    void blockingRestrictionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new GrotagThrasher());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.passUntil(player2, TurnStep.UPKEEP);
        assertThat(target.isCantBlockThisTurn()).isFalse();
    }
}
