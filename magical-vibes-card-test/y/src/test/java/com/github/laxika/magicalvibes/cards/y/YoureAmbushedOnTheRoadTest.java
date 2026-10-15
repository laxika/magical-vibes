package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.h.HillGiantHerdgorger;
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

@CardUsed({YoureAmbushedOnTheRoad.class, HillGiantHerdgorger.class})
class YoureAmbushedOnTheRoadTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureYouControlToItsOwnersHand() {
        harness.addToBattlefield(player1, new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new YoureAmbushedOnTheRoad()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        UUID targetId = harness.getPermanentId(player1, "Hill Giant Herdgorger");

        harness.castInstant(player1, 0, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hill Giant Herdgorger");
        harness.assertInHand(player1, "Hill Giant Herdgorger");
    }

    @Test
    void boostsTargetCreatureUntilEndOfTurn() {
        harness.addToBattlefield(player1, new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new YoureAmbushedOnTheRoad()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        UUID targetId = harness.getPermanentId(player1, "Hill Giant Herdgorger");

        harness.castInstant(player1, 0, 1, targetId);
        harness.passBothPriorities();

        Permanent bear = findPermanent(player1, "Hill Giant Herdgorger");
        assertThat(bear.getPowerModifier()).isEqualTo(1);
        assertThat(bear.getToughnessModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(bear.getPowerModifier()).isZero();
        assertThat(bear.getToughnessModifier()).isZero();
    }

    @Test
    void retreatCannotTargetCreatureOpponentControls() {
        harness.addToBattlefield(player2, new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new YoureAmbushedOnTheRoad()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        UUID targetId = harness.getPermanentId(player2, "Hill Giant Herdgorger");

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 0, targetId))
                .isInstanceOf(IllegalStateException.class);

    }
    @Test
    void standAndFightCanTargetCreatureOpponentControls() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new YoureAmbushedOnTheRoad()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 1, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Hill Giant Herdgorger");
    }

    @Test
    void retreatReturnsBorrowedCreatureToItsOwner() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        gd.stolenCreatures.put(creature.getId(), player2.getId());
        harness.setHand(player1, List.of(new YoureAmbushedOnTheRoad()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Hill Giant Herdgorger");
        harness.assertInHand(player2, "Hill Giant Herdgorger");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void standAndFightDoesNotResolveWhenTargetIsReturnedInResponse() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new HillGiantHerdgorger());
        harness.setHand(player1, List.of(new YoureAmbushedOnTheRoad(), new YoureAmbushedOnTheRoad()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0, 1, creature.getId());
        harness.castInstant(player1, 0, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Hill Giant Herdgorger");
        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
