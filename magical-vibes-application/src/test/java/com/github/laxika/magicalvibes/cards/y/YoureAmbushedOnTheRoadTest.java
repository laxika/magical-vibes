package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YoureAmbushedOnTheRoad.class, GrizzlyBears.class})
class YoureAmbushedOnTheRoadTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureYouControlToItsOwnersHand() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new YoureAmbushedOnTheRoad()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    void boostsTargetCreatureUntilEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new YoureAmbushedOnTheRoad()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        UUID targetId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.castInstant(player1, 0, 1, targetId);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Grizzly Bears");
        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    @Test
    void modesCannotTargetCreatureOpponentControls() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new YoureAmbushedOnTheRoad()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);

        harness.setHand(player1, List.of(new YoureAmbushedOnTheRoad()));
        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, targetId))
                .isInstanceOf(IllegalStateException.class);
    }
}
