package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeylineOfTheVoid;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShardOfTheVoidDragon.class, Forest.class, GrizzlyBears.class, MindStone.class,
        LeylineOfTheVoid.class, MycosynthLattice.class})
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

    @Test
    void opponentChoosesWhichNonlandPermanentToSacrifice() {
        Permanent shard = addCreatureReady(player1, new ShardOfTheVoidDragon());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());
        Permanent chosenArtifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player2, List.of(chosenArtifact.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shard, ownArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .contains(land, firstArtifact).doesNotContain(chosenArtifact);
        harness.assertInGraveyard(player2, "Mind Stone");
        assertThat(shard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void attackDoesNotSacrificeLandsWhenOpponentHasNoNonlandPermanent() {
        Permanent shard = addCreatureReady(player1, new ShardOfTheVoidDragon());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(shard);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(land);
        harness.assertNotInGraveyard(player2, "Forest");
    }

    @Test
    void ownArtifactsTriggerForBothGraveyardAndExile() {
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new ShardOfTheVoidDragon());
        Permanent firstArtifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        Permanent secondArtifact = harness.addToBattlefieldAndReturn(player1, new MindStone());

        removeToGraveyard(firstArtifact);
        assertThat(shard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        removeToExile(secondArtifact);
        assertThat(shard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void nonartifactDeparturesAndArtifactsReturnedToHandDoNotTrigger() {
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new ShardOfTheVoidDragon());
        Permanent firstLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent secondLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        removeToGraveyard(firstLand);
        removeToExile(secondLand);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, artifact));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(artifact.getCard());
        assertThat(shard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void artifactExiledInsteadOfGoingToGraveyardStillTriggers() {
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new ShardOfTheVoidDragon());
        harness.addToBattlefield(player1, new LeylineOfTheVoid());
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MindStone());

        removeToGraveyard(artifact);

        harness.assertNotInGraveyard(player2, "Mind Stone");
        assertThat(gd.findExiledCard(artifact.getCard().getId())).isNotNull();
        assertThat(shard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void permanentMadeArtifactByStaticAbilityTriggersWhenPutIntoGraveyard() {
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new ShardOfTheVoidDragon());
        harness.addToBattlefield(player1, new MycosynthLattice());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        removeToGraveyard(land);

        harness.assertInGraveyard(player2, "Forest");
        assertThat(shard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void permanentMadeArtifactByStaticAbilityTriggersWhenExiled() {
        Permanent shard = harness.addToBattlefieldAndReturn(player1, new ShardOfTheVoidDragon());
        harness.addToBattlefield(player1, new MycosynthLattice());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        removeToExile(land);

        assertThat(gd.findExiledCard(land.getCard().getId())).isNotNull();
        assertThat(shard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
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
