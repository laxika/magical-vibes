package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BlightMamba;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimContest.class, HornedTurtle.class, HillGiant.class, BlightMamba.class})
class GrimContestTest extends BaseCardTest {

    @Test
    void creaturesDealDamageEqualToTheirToughness() {
        harness.addToBattlefield(player1, new HornedTurtle());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new GrimContest()));
        addContestMana();

        UUID turtleId = harness.getPermanentId(player1, "Horned Turtle");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castAndResolveInstant(player1, 0, List.of(turtleId, giantId));

        harness.assertInGraveyard(player2, "Hill Giant");
        harness.assertOnBattlefield(player1, "Horned Turtle");
        Permanent turtle = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(turtle.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    void cannotTargetOwnCreatureAsSecondTarget() {
        harness.addToBattlefield(player1, new HornedTurtle());
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new GrimContest()));
        addContestMana();

        UUID firstTargetId = harness.getPermanentId(player1, "Horned Turtle");
        UUID secondTargetId = harness.getPermanentId(player1, "Hill Giant");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(firstTargetId, secondTargetId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void neitherCreatureDealsDamageWhenATargetIsRemovedBeforeResolution() {
        harness.addToBattlefield(player1, new HornedTurtle());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new GrimContest()));
        addContestMana();

        UUID turtleId = harness.getPermanentId(player1, "Horned Turtle");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, List.of(turtleId, giantId));
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        Permanent turtle = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(turtle.getMarkedDamage()).isZero();
    }

    @Test
    void cannotTargetOpponentsCreatureAsFirstTarget() {
        Permanent turtle = harness.addToBattlefieldAndReturn(player2, new HornedTurtle());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new GrimContest()));
        addContestMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, List.of(turtle.getId(), giant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothCreaturesDealLethalDamage() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new GrimContest()));
        addContestMana();

        harness.castAndResolveInstant(player1, 0, List.of(first.getId(), second.getId()));

        harness.assertInGraveyard(player1, "Hill Giant");
        harness.assertInGraveyard(player2, "Hill Giant");
    }

    @Test
    void usesToughnessAtResolutionIncludingCounters() {
        Permanent turtle = harness.addToBattlefieldAndReturn(player1, new HornedTurtle());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new GrimContest()));
        addContestMana();
        harness.castInstant(player1, 0, List.of(turtle.getId(), giant.getId()));
        giant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Horned Turtle");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(giant.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void neitherCreatureDealsDamageWhenFirstTargetLeaves() {
        Permanent turtle = harness.addToBattlefieldAndReturn(player1, new HornedTurtle());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player1, List.of(new GrimContest()));
        addContestMana();
        harness.castInstant(player1, 0, List.of(turtle.getId(), giant.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(giant.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Hill Giant");
    }

    @Test
    void infectDoesNotReduceSimultaneousReturnDamage() {
        Permanent mamba = harness.addToBattlefieldAndReturn(player1, new BlightMamba());
        mamba.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent turtle = harness.addToBattlefieldAndReturn(player2, new HornedTurtle());
        harness.setHand(player1, List.of(new GrimContest()));
        addContestMana();

        harness.castAndResolveInstant(player1, 0, List.of(mamba.getId(), turtle.getId()));

        harness.assertInGraveyard(player1, "Blight Mamba");
        harness.assertOnBattlefield(player2, "Horned Turtle");
        assertThat(turtle.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    private void addContestMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
