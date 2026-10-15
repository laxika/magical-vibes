package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GreaterWerewolf;
import com.github.laxika.magicalvibes.cards.y.YoungWolf;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpiritOfTheHunt.class, YoungWolf.class, GreaterWerewolf.class, GrizzlyBears.class})
class SpiritOfTheHuntTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Spirit of the Hunt boosts other Wolves and Werewolves you control")
    void enteringBoostsOtherWolvesAndWerewolves() {
        Permanent wolf = addCreatureReady(player1, new YoungWolf());
        Permanent werewolf = addCreatureReady(player1, new GreaterWerewolf());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingWolf = addCreatureReady(player2, new YoungWolf());

        castSpiritOfTheHunt();

        assertThat(wolf.getToughnessModifier()).isEqualTo(3);
        assertThat(werewolf.getToughnessModifier()).isEqualTo(3);
        assertThat(bears.getToughnessModifier()).isZero();
        assertThat(opposingWolf.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Spirit of the Hunt does not boost itself")
    void enteringDoesNotBoostItself() {
        castSpiritOfTheHunt();

        Permanent spirit = findPermanent(player1, "Spirit of the Hunt");

        assertThat(spirit.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Spirit of the Hunt's boost ends at end of turn")
    void boostEndsAtEndOfTurn() {
        Permanent wolf = addCreatureReady(player1, new YoungWolf());

        castSpiritOfTheHunt();
        assertThat(wolf.getToughnessModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(wolf.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Spirit of the Hunt can be cast during an opponent's combat")
    void canBeCastDuringOpponentsCombat() {
        Permanent otherSpirit = addCreatureReady(player1, new SpiritOfTheHunt());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new SpiritOfTheHunt(), "{1}{G}{G}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit of the Hunt")).hasSize(2);
        assertThat(otherSpirit.getPowerModifier()).isZero();
        assertThat(otherSpirit.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("A Wolf entering in response is included when the original trigger resolves")
    void includesWolfEnteringBeforeTriggerResolves() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new SpiritOfTheHunt(), "{1}{G}{G}");
        harness.passBothPriorities();
        Permanent firstSpirit = findPermanent(player1, "Spirit of the Hunt");
        assertThat(gd.stack).hasSize(1);
        assertThat(firstSpirit.getToughnessModifier()).isZero();

        harness.castFromHand(player1, new SpiritOfTheHunt(), "{1}{G}{G}");
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit of the Hunt")).hasSize(2)
                .allSatisfy(spirit -> {
                    assertThat(spirit.getPowerModifier()).isZero();
                    assertThat(spirit.getToughnessModifier()).isEqualTo(3);
                });
    }

    @Test
    @DisplayName("A Wolf entering after the trigger resolves does not receive the earlier boost")
    void excludesWolfEnteringAfterTriggerResolves() {
        castSpiritOfTheHunt();
        Permanent firstSpirit = findPermanent(player1, "Spirit of the Hunt");

        harness.castFromHand(player1, new SpiritOfTheHunt(), "{1}{G}{G}");
        harness.passBothPriorities();
        Permanent secondSpirit = findPermanents(player1, "Spirit of the Hunt").get(1);
        resolveAllTriggers();

        assertThat(firstSpirit.getToughnessModifier()).isEqualTo(3);
        assertThat(secondSpirit.getToughnessModifier()).isZero();
    }

    private void castSpiritOfTheHunt() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new SpiritOfTheHunt(), "{1}{G}{G}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
