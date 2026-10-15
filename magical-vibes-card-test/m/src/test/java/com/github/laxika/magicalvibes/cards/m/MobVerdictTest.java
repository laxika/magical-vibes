package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TheValeyard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MobVerdict.class, Forest.class, GrizzlyBears.class, TheValeyard.class})
class MobVerdictTest extends BaseCardTest {

    @Test
    void damagesEachOpponentVoteRecipientAndDrawsForVotesReceived() {
        Forest draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new MobVerdict(), "{2}{R}{R}");
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(card -> card.getId())
                .contains(creature.getCard().getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void damagesAllOpposingCreaturesButLeavesControllersCreaturesAndNoncreaturesUnharmed() {
        Forest draw = new Forest();
        harness.setLibrary(player1, List.of(draw));
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.castFromHand(player1, new MobVerdict(), "{2}{R}{R}");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownCreature);
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactlyInAnyOrder(firstCreature.getCard(), secondCreature.getCard());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(draw);
    }

    @Test
    void additionalVoteFromTheValeyardIncreasesDamageToTheOnlyOpponent() {
        harness.addToBattlefield(player1, new TheValeyard());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new MobVerdict(), "{2}{R}{R}");
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        harness.assertLife(player1, 20);
    }
}
