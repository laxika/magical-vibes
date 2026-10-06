package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RallyOfWings.class, AirElemental.class, GrizzlyBears.class, Plains.class})
class RallyOfWingsTest extends BaseCardTest {

    @Test
    void untapsOwnCreaturesAndBoostsOnlyOwnCreaturesWithFlying() {
        Permanent ownFlyer = addCreatureReady(player1, new AirElemental());
        ownFlyer.tap();
        Permanent ownGroundCreature = addCreatureReady(player1, new GrizzlyBears());
        ownGroundCreature.tap();
        Permanent opposingFlyer = addCreatureReady(player2, new AirElemental());
        opposingFlyer.tap();

        castRallyOfWings();

        assertThat(ownFlyer.isTapped()).isFalse();
        assertThat(ownGroundCreature.isTapped()).isFalse();
        assertThat(opposingFlyer.isTapped()).isTrue();
        assertThat(ownFlyer.getPowerModifier()).isEqualTo(2);
        assertThat(ownFlyer.getToughnessModifier()).isEqualTo(2);
        assertThat(ownGroundCreature.getPowerModifier()).isZero();
        assertThat(ownGroundCreature.getToughnessModifier()).isZero();
        assertThat(opposingFlyer.getPowerModifier()).isZero();
        assertThat(opposingFlyer.getToughnessModifier()).isZero();
    }

    @Test
    void flyingBoostWearsOffAtEndOfTurn() {
        Permanent ownFlyer = addCreatureReady(player1, new AirElemental());

        castRallyOfWings();

        assertThat(ownFlyer.getPowerModifier()).isEqualTo(2);
        assertThat(ownFlyer.getToughnessModifier()).isEqualTo(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownFlyer.getPowerModifier()).isZero();
        assertThat(ownFlyer.getToughnessModifier()).isZero();
    }

    @Test
    void doesNotUntapNoncreaturePermanents() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        land.tap();
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        creature.tap();

        castRallyOfWings();

        assertThat(land.isTapped()).isTrue();
        assertThat(land.getPowerModifier()).isZero();
        assertThat(land.getToughnessModifier()).isZero();
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    void resolvesWithoutCreaturesOrTargets() {
        castRallyOfWings();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Rally of Wings");
    }

    @Test
    void doesNotBoostCreaturesEnteringAfterResolution() {
        Permanent originalFlyer = addCreatureReady(player1, new AirElemental());

        castRallyOfWings();

        Permanent laterFlyer = harness.enterBattlefieldAndReturn(player1, new AirElemental());

        assertThat(originalFlyer.getPowerModifier()).isEqualTo(2);
        assertThat(originalFlyer.getToughnessModifier()).isEqualTo(2);
        assertThat(laterFlyer.getPowerModifier()).isZero();
        assertThat(laterFlyer.getToughnessModifier()).isZero();
    }

    @Test
    void includesCreaturesEnteringBeforeResolution() {
        harness.setHand(player1, List.of(new RallyOfWings()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0);

        Permanent flyer = harness.enterBattlefieldAndReturn(player1, new AirElemental());
        flyer.tap();
        harness.passBothPriorities();

        assertThat(flyer.isTapped()).isFalse();
        assertThat(flyer.getPowerModifier()).isEqualTo(2);
        assertThat(flyer.getToughnessModifier()).isEqualTo(2);
    }

    private void castRallyOfWings() {
        harness.setHand(player1, List.of(new RallyOfWings()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0);
    }
}
