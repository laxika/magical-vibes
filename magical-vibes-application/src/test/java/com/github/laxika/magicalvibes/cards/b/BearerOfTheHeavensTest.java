package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyAllPermanents;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BearerOfTheHeavensTest extends BaseCardTest {

    @Test
    @DisplayName("Death registers a delayed global destruction trigger")
    void deathRegistersDelayedTrigger() {
        addBearerWithOneToughness();
        destroyBearer();

        harness.assertInGraveyard(player1, "Bearer of the Heavens");
        assertThat(gd.getDelayedActions(DelayedDestroyAllPermanents.class)).isEmpty();

        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedDestroyAllPermanents.class)).hasSize(1);
    }

    @Test
    @DisplayName("Delayed trigger destroys every permanent at the next end step")
    void destroysAllPermanentsAtNextEndStep() {
        addBearerWithOneToughness();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Plains());
        destroyBearer();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Plains");
    }

    private void addBearerWithOneToughness() {
        BearerOfTheHeavens bearer = new BearerOfTheHeavens();
        bearer.setToughness(1);
        harness.addToBattlefield(player1, bearer);
    }

    private void destroyBearer() {
        Permanent bearer = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.castInstant(player2, 0, bearer.getId());
        harness.passBothPriorities();
    }
}
