package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SaprazzanRaiderTest extends BaseCardTest {

    @Test
    @DisplayName("Returns itself to its owner's hand when it becomes blocked")
    void returnsToHandWhenBlocked() {
        Permanent raider = addCreatureReady(player1, new SaprazzanRaider());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertInHand(player1, "Saprazzan Raider");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(raider.getId()));
    }

    @Test
    @DisplayName("Stays on the battlefield when it is not blocked")
    void staysOnBattlefieldWhenNotBlocked() {
        Permanent raider = addCreatureReady(player1, new SaprazzanRaider());

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(raider.getId()));
        harness.assertNotInHand(player1, "Saprazzan Raider");
    }
}
