package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShrikeForce.class, GrizzlyBears.class})
class ShrikeForceTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Shrike Force")
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new ShrikeForce());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Vigilance and double strike work in combat")
    void vigilanceAndDoubleStrikeWorkInCombat() {
        Permanent shrikeForce = addCreatureReady(player1, new ShrikeForce());

        declareAttackers(player1, List.of(0));
        assertThat(shrikeForce.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
