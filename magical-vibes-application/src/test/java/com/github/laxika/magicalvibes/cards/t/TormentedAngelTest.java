package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CapashenTemplar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TormentedAngel.class, CapashenTemplar.class})
class TormentedAngelTest extends BaseCardTest {

    @Test
    @DisplayName("Tormented Angel cannot be blocked by a ground creature")
    void cannotBeBlockedByGroundCreature() {
        addCreatureReady(player1, new TormentedAngel());
        addCreatureReady(player2, new CapashenTemplar());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Tormented Angel (flying)");
    }

    @Test
    @DisplayName("A flying creature can block Tormented Angel")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new TormentedAngel());
        Permanent blocker = addCreatureReady(player2, new TormentedAngel());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
