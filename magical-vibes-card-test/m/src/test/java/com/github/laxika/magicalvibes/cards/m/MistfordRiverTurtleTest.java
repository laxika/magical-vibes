package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SporecapSpider;
import com.github.laxika.magicalvibes.cards.w.WorthyKnight;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MistfordRiverTurtle.class, SporecapSpider.class, WorthyKnight.class})
class MistfordRiverTurtleTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking makes another attacking non-Human creature unblockable")
    void makesAnotherAttackingNonHumanCreatureUnblockable() {
        addCreatureReady(player1, new MistfordRiverTurtle());
        Permanent attacker = addCreatureReady(player1, new SporecapSpider());
        Permanent blocker = addCreatureReady(player2, new SporecapSpider());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("The attack trigger cannot target a Human creature")
    void cannotTargetHumanCreature() {
        addCreatureReady(player1, new MistfordRiverTurtle());
        Permanent nonHumanAttacker = addCreatureReady(player1, new SporecapSpider());
        Permanent humanAttacker = addCreatureReady(player1, new WorthyKnight());

        declareAttackers(List.of(0, 1, 2));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, humanAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, nonHumanAttacker.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The attack trigger cannot target the Turtle itself")
    void cannotTargetItself() {
        Permanent turtle = addCreatureReady(player1, new MistfordRiverTurtle());
        addCreatureReady(player1, new SporecapSpider());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, turtle.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The attack trigger cannot target a nonattacking non-Human creature")
    void cannotTargetNonattackingCreature() {
        addCreatureReady(player1, new MistfordRiverTurtle());
        Permanent attacker = addCreatureReady(player1, new SporecapSpider());
        Permanent nonattacker = addCreatureReady(player1, new SporecapSpider());
        addCreatureReady(player2, new SporecapSpider());

        declareAttackers(List.of(0, 1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonattacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();
        assertThat(attacker.isCantBeBlocked()).isTrue();
        assertThat(nonattacker.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Attacking alone does not leave a target choice pending")
    void attackingAloneHasNoLegalTarget() {
        Permanent turtle = addCreatureReady(player1, new MistfordRiverTurtle());
        addCreatureReady(player2, new SporecapSpider());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(turtle.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Removing the Turtle does not stop its attack trigger from resolving")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        Permanent turtle = addCreatureReady(player1, new MistfordRiverTurtle());
        Permanent attacker = addCreatureReady(player1, new SporecapSpider());
        addCreatureReady(player2, new SporecapSpider());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(turtle);
        harness.passBothPriorities();

        assertThat(attacker.isCantBeBlocked()).isTrue();
    }
}
