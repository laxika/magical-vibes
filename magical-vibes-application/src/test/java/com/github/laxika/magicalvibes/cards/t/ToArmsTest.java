package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.cards.g.GruulSignet;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ToArms.class, GhostWarden.class, GruulSignet.class})
class ToArmsTest extends BaseCardTest {

    @Test
    void untapsOwnCreaturesAndDrawsACard() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GhostWarden());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new GruulSignet());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GhostWarden());
        ownCreature.tap();
        ownArtifact.tap();
        opponentCreature.tap();

        GhostWarden drawCard = new GhostWarden();
        harness.setLibrary(player1, List.of(drawCard));
        harness.castFromHand(player1, new ToArms(), "{1}{W}");
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(ownArtifact.isTapped()).isTrue();
        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawCard);
    }
}
