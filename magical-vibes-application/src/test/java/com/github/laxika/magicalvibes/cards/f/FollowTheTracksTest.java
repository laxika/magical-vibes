package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed(FollowTheTracks.class)
class FollowTheTracksTest extends BaseCardTest {

    @Test
    void castingFollowTheTracksResolves() {
        harness.setHand(player1, List.of(new FollowTheTracks()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Follow the Tracks");
    }
}
