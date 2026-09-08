package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RallyOfWings.class, AirElemental.class, GrizzlyBears.class})
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

    private void castRallyOfWings() {
        harness.setHand(player1, List.of(new RallyOfWings()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, List.of());
        harness.passBothPriorities();
    }
}
