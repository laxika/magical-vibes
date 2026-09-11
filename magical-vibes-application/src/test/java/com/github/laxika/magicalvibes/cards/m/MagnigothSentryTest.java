package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MagnigothSentry.class, AirElemental.class})
class MagnigothSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Reach allows Magnigoth Sentry to block a flying creature")
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new AirElemental());
        Permanent sentry = addCreatureReady(player2, new MagnigothSentry());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(sentry.isBlocking()).isTrue();
    }
}
