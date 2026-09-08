package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JamuraanLion.class, JujuBubble.class, PhyrexianWalker.class})
class JamuraanLionTest extends BaseCardTest {

    @Test
    @DisplayName("Ability makes the target creature unable to block this turn")
    void abilityPreventsBlocking() {
        Permanent lion = addCreatureReady(player1, new JamuraanLion());
        Permanent target = addCreatureReady(player2, new PhyrexianWalker());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(lion.isTapped()).isTrue();
        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addCreatureReady(player1, new JamuraanLion());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new JujuBubble());
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Target creature cannot be declared as a blocker this turn")
    void targetCreatureCannotBeDeclaredAsBlocker() {
        addCreatureReady(player1, new JamuraanLion());
        addCreatureReady(player1, new PhyrexianWalker());
        Permanent blocker = addCreatureReady(player2, new PhyrexianWalker());
        Permanent otherBlocker = addCreatureReady(player2, new PhyrexianWalker());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, blocker.getId());
        harness.passBothPriorities();

        declareAttackers(player1, List.of(1));
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");

        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 1)));

        assertThat(blocker.isBlocking()).isFalse();
        assertThat(otherBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can't-block effect wears off at end of turn")
    void cantBlockWearsOff() {
        addCreatureReady(player1, new JamuraanLion());
        Permanent target = addCreatureReady(player2, new PhyrexianWalker());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBlockThisTurn()).isTrue();

        harness.passUntil(player1, TurnStep.CLEANUP);
        assertThat(target.isCantBlockThisTurn()).isFalse();
    }
}
