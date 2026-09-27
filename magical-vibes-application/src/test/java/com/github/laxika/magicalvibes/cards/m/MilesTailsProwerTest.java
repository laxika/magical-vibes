package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(MilesTailsPrower.class)
class MilesTailsProwerTest extends BaseCardTest {

    @Test
    void nonFlyingVehicleGetsAFlyingCounter() {
        harness.addToBattlefield(player1, new MilesTailsPrower());
        Card vehicleCard = vehicle("Vehicle", false);
        harness.setHand(player1, List.of(vehicleCard));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent vehicle = findPermanent(player1, "Vehicle");
        assertThat(vehicle.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void flyingVehicleMakesYouDrawACard() {
        harness.addToBattlefield(player1, new MilesTailsPrower());
        Card vehicleCard = vehicle("Flying Vehicle", true);
        harness.setHand(player1, List.of(vehicleCard));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent vehicle = findPermanent(player1, "Flying Vehicle");
        assertThat(vehicle.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    private Card vehicle(String name, boolean flying) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost("{0}");
        card.setSubtypes(List.of(CardSubtype.VEHICLE));
        card.setKeywords(flying ? Set.of(Keyword.FLYING) : Set.of());
        return card;
    }
}
