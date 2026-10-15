package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyAllPermanents;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BearerOfTheHeavens.class, GrizzlyBears.class, LightningBolt.class, Forest.class, Plains.class})
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

        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player2, "Plains");
    }

    @Test
    @DisplayName("Death during the end step waits until the following turn's end step")
    void deathDuringEndStepWaitsForFollowingEndStep() {
        harness.forceStep(TurnStep.END_STEP);
        Permanent bearer = harness.addToBattlefieldAndReturn(player1, new BearerOfTheHeavens());
        bearer.setMarkedDamage(7);
        harness.addToBattlefield(player2, new GrizzlyBears());

        destroyBearer();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.getDelayedActions(DelayedDestroyAllPermanents.class)).hasSize(1);

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The delayed destruction includes permanents that enter after the death trigger resolves")
    void destroysPermanentsThatEnterLater() {
        Permanent bearer = harness.addToBattlefieldAndReturn(player1, new BearerOfTheHeavens());
        bearer.setMarkedDamage(7);
        destroyBearer();
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Regeneration saves a permanent and the delayed ability triggers only once")
    void regenerationSavesPermanentAndDestructionDoesNotRepeat() {
        Permanent bearer = harness.addToBattlefieldAndReturn(player1, new BearerOfTheHeavens());
        bearer.setMarkedDamage(7);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        destroyBearer();
        harness.passBothPriorities();

        harness.passUntil(TurnStep.END_STEP);
        bears.setRegenerationShield(1);
        harness.withAutoStop(TurnStep.END_STEP, () -> harness.passBothPriorities());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bears.isTapped()).isTrue();
        assertThat(bears.getRegenerationShield()).isZero();

        harness.passUntilWithNoAttackers(player2, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
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
        harness.castAndResolveInstant(player2, 0, bearer.getId());
    }
}
