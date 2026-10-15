package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AlpineGrizzly;
import com.github.laxika.magicalvibes.cards.j.JeskaiWindscout;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HighspireMantis.class, AlpineGrizzly.class, JeskaiWindscout.class})
class HighspireMantisTest extends BaseCardTest {

    @Test
    void groundCreatureCannotBlockMantis() {
        addCreatureReady(player1, new HighspireMantis());
        addCreatureReady(player2, new AlpineGrizzly());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void trampleDealsExcessDamageThroughFlyingBlocker() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new HighspireMantis());
        Permanent blocker = addCreatureReady(player2, new JeskaiWindscout());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CombatDamageAssignment.class);
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 2
        ));

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player2, "Jeskai Windscout");
        harness.assertOnBattlefield(player1, "Highspire Mantis");
    }

    @Test
    void flyingCreatureCanBlockGroundAttacker() {
        addCreatureReady(player1, new AlpineGrizzly());
        Permanent mantis = addCreatureReady(player2, new HighspireMantis());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(mantis.isBlocking()).isTrue();
    }
}
