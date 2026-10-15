package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AlphaMyr;
import com.github.laxika.magicalvibes.cards.t.TelJiladExile;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AlphaMyr.class, DiscipleOfTheVault.class, DrossProwler.class, TelJiladExile.class})
class DrossProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Dross Prowler cannot be blocked by a nonblack, nonartifact creature")
    void cannotBeBlockedByNonblackNonartifactCreature() {
        attackingProwler();
        addCreatureReady(player2, new TelJiladExile());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("cannot block Dross Prowler (fear)");
    }

    @Test
    @DisplayName("Dross Prowler can be blocked by an artifact creature")
    void canBeBlockedByArtifactCreature() {
        attackingProwler();
        Permanent blocker = addCreatureReady(player2, new AlphaMyr());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("declares 1 blocker"));
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Dross Prowler can be blocked by a black creature")
    void canBeBlockedByBlackCreature() {
        attackingProwler();
        Permanent blocker = addCreatureReady(player2, new DiscipleOfTheVault());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent attackingProwler() {
        return addCreatureReady(player1, new DrossProwler());
    }
}
