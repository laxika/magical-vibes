package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinGoon.class, FugitiveWizard.class})
class GoblinGoonTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot attack when the creature counts are tied")
    void cannotAttackWhenCreatureCountsAreTied() {
        addCreatureReady(player1, new GoblinGoon());
        addCreatureReady(player2, new FugitiveWizard());

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can attack when controlling more creatures than defending player")
    void canAttackWhenControllingMoreCreatures() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GoblinGoon());
        addCreatureReady(player1, new FugitiveWizard());

        declareAttackers(player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Cannot block when the creature counts are tied")
    void cannotBlockWhenCreatureCountsAreTied() {
        addCreatureReady(player1, new FugitiveWizard());
        addCreatureReady(player2, new GoblinGoon());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can block when controlling more creatures than attacking player")
    void canBlockWhenControllingMoreCreatures() {
        addCreatureReady(player1, new FugitiveWizard());
        Permanent goon = addCreatureReady(player2, new GoblinGoon());
        addCreatureReady(player2, new FugitiveWizard());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(goon.isBlocking()).isTrue();
    }
}
