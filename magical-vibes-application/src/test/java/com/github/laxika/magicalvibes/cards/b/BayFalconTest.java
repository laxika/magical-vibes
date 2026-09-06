package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.v.ViashinoWarrior;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BayFalcon.class, ViashinoWarrior.class})
class BayFalconTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a non-flying creature from blocking")
    void flyingPreventsNonFlyingCreatureBlocking() {
        Permanent attacker = addCreatureReady(player1, new BayFalcon());
        addCreatureReady(player2, new ViashinoWarrior());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("Vigilance keeps Bay Falcon untapped when it attacks")
    void vigilanceKeepsItUntapped() {
        Permanent attacker = addCreatureReady(player1, new BayFalcon());

        declareAttackers(List.of(0));

        assertThat(attacker.isTapped()).isFalse();
    }
}
