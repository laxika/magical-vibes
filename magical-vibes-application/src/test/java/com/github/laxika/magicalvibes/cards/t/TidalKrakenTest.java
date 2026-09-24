package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DeadlyInsect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TidalKraken.class, GrizzlyBears.class, DeadlyInsect.class})
class TidalKrakenTest extends BaseCardTest {

    @Test
    @DisplayName("Tidal Kraken cannot be blocked by a ground creature")
    void cannotBeBlocked() {
        addCreatureReady(player2, new GrizzlyBears());

        addCreatureReady(player1, new TidalKraken());
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("A face-down Tidal Kraken can be blocked")
    void faceDownCanBeBlocked() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        Permanent attacker = addCreatureReady(player1, new TidalKraken());
        attacker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Unblocked Tidal Kraken deals 6 damage to defending player")
    void dealsDamageWhenUnblocked() {
        harness.setLife(player2, 20);

        addCreatureReady(player1, new TidalKraken());
        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }
}
