package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BlackKnight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SquirmingMass.class, GrizzlyBears.class, BlackKnight.class, Ornithopter.class})
class SquirmingMassTest extends BaseCardTest {

    private void addAttacker() {
        Permanent attacker = addCreatureReady(player1, new SquirmingMass());
        attacker.setAttacking(true);
    }

    private Permanent prepareBlocker(Card blockerCard) {
        Permanent blocker = addCreatureReady(player2, blockerCard);
        addAttacker();
        prepareDeclareBlockers();
        return blocker;
    }

    @Test
    @DisplayName("Fear prevents a nonblack, nonartifact creature from blocking")
    void nonblackNonartifactCreatureCannotBlock() {
        prepareBlocker(new GrizzlyBears());

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    @DisplayName("Fear allows a black creature to block")
    void blackCreatureCanBlock() {
        Permanent blocker = prepareBlocker(new BlackKnight());

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Fear allows an artifact creature to block")
    void artifactCreatureCanBlock() {
        Permanent blocker = prepareBlocker(new Ornithopter());

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
