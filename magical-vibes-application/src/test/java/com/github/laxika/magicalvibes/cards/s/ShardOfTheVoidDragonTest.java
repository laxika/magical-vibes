package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShardOfTheVoidDragon.class, Forest.class, GrizzlyBears.class, MindStone.class})
class ShardOfTheVoidDragonTest extends BaseCardTest {

    @Test
    void attackMakesEachOpponentSacrificeANonlandPermanent() {
        addCreatureReady(player1, new ShardOfTheVoidDragon());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent nonland = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land).doesNotContain(nonland);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void putsTwoCountersOnArtifactPutIntoGraveyardOrExile() {
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new ShardOfTheVoidDragon());

        Permanent graveyardArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        removeToGraveyard(graveyardArtifact);
        assertThat(shard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);

        Card tokenCard = new MindStone();
        tokenCard.setToken(true);
        Permanent exiledArtifact = harness.addToBattlefieldAndReturn(player2, tokenCard);
        removeToExile(exiledArtifact);

        assertThat(shard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    private void removeToGraveyard(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, permanent));
        harness.passBothPriorities();
    }

    private void removeToExile(Permanent permanent) {
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, permanent));
        harness.passBothPriorities();
    }
}
