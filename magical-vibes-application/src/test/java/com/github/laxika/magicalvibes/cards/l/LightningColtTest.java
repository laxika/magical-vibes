package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ChandraHopesBeacon;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.InvasionOfTarkir;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LightningColt.class, GrizzlyBears.class, Forest.class, ChandraHopesBeacon.class,
        InvasionOfTarkir.class})
class LightningColtTest extends BaseCardTest {

    @Test
    void entersAndDealsThreeDamageToTargetPlayer() {
        castAndResolveLightningColt(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertOnBattlefield(player1, "Lightning Colt");
    }

    @Test
    void entersAndDealsThreeDamageToTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        castAndResolveLightningColt(target.getId());

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new LightningColt()));
        addLightningColtMana();

        assertThatThrownBy(() -> harness.castCreature(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetItsController() {
        castAndResolveLightningColt(player1.getId());

        harness.assertLife(player1, 17);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Lightning Colt");
    }

    @Test
    void canTargetACreatureItsControllerControls() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castAndResolveLightningColt(target.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void dealsThreeDamageToAPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraHopesBeacon());
        target.setCounterCount(CounterType.LOYALTY, 5);

        castAndResolveLightningColt(target.getId());

        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Chandra, Hope's Beacon");
        harness.assertLife(player2, 20);
    }

    @Test
    void canTargetABattleAndRemoveThreeDefenseCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new InvasionOfTarkir());
        target.setCounterCount(CounterType.DEFENSE, 5);
        target.setProtectorPlayerId(player1.getId());

        castAndResolveLightningColt(target.getId());

        assertThat(target.getCounterCount(CounterType.DEFENSE)).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Invasion of Tarkir");
        harness.assertLife(player2, 20);
    }

    @Test
    void canBeCastDuringAnOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.ensurePriority(player1);

        castAndResolveLightningColt(player2.getId());

        harness.assertLife(player2, 17);
        harness.assertOnBattlefield(player1, "Lightning Colt");
    }

    @Test
    void damageTriggerStillResolvesAfterLightningColtLeaves() {
        harness.setHand(player1, List.of(new LightningColt()));
        addLightningColtMana();
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
        Permanent source = gqs.findPermanentById(gd, harness.getPermanentId(player1, "Lightning Colt"));
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, source));

        resolveAllTriggers();

        harness.assertLife(player2, 17);
        harness.assertInGraveyard(player1, "Lightning Colt");
    }

    @Test
    void damageTriggerDoesNotResolveWhenItsTargetLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new LightningColt()));
        addLightningColtMana();
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .destroyPermanentToGraveyard(gd, target));

        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Lightning Colt");
        assertThat(gd.stack).isEmpty();
    }

    private void castAndResolveLightningColt(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new LightningColt()));
        addLightningColtMana();
        harness.castCreature(player1, 0, targetId);
        resolveAllTriggers();
    }

    private void addLightningColtMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);
    }
}
