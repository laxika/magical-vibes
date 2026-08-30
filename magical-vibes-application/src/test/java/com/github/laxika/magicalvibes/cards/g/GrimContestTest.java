package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GrimContestTest extends BaseCardTest {

    @Test
    void creaturesDealDamageEqualToTheirToughness() {
        harness.addToBattlefield(player1, new HornedTurtle());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new GrimContest()));
        addContestMana();

        UUID turtleId = harness.getPermanentId(player1, "Horned Turtle");
        UUID giantId = harness.getPermanentId(player2, "Hill Giant");
        harness.castInstant(player1, 0, List.of(turtleId, giantId));
        harness.passBothPriorities();

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

        List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
        UUID firstTargetId = battlefield.get(0).getId();
        UUID secondTargetId = battlefield.get(1).getId();

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

    private void addContestMana() {
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
