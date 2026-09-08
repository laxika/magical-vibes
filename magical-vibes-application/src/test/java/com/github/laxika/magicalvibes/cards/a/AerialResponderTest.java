package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AerialResponderTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Aerial Responder")
    void flyingPreventsGroundCreatureFromBlocking() {
        addCreatureReady(player1, new AerialResponder());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block");
    }

    @Test
    @DisplayName("Vigilance keeps Aerial Responder untapped and lifelink gains life in combat")
    void vigilanceAndLifelinkWorkInCombat() {
        Permanent responder = addCreatureReady(player1, new AerialResponder());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareAttackers(player1, List.of(0));
        assertThat(responder.isTapped()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
    }
}
