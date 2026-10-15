package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.ReckonerBankbuster;
import com.github.laxika.magicalvibes.cards.s.SmugglersCopter;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MilesTailsPrower.class, Disenchant.class, Ornithopter.class,
        ReckonerBankbuster.class, SmugglersCopter.class})
class MilesTailsProwerTest extends BaseCardTest {

    @Test
    void nonFlyingVehicleGetsAFlyingCounter() {
        harness.addToBattlefield(player1, new MilesTailsPrower());
        Card vehicleCard = vehicle("Vehicle", false);
        harness.castFromHand(player1, vehicleCard, "{0}");
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
        harness.castFromHand(player1, vehicleCard, "{0}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent vehicle = findPermanent(player1, "Flying Vehicle");
        assertThat(vehicle.getCounterCount(CounterType.FLYING)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    void flyingIsCheckedAtResolutionRatherThanEntry() {
        harness.addToBattlefield(player1, new MilesTailsPrower());
        harness.setLibrary(player1, List.of(new Ornithopter()));
        harness.castFromHand(player1, new ReckonerBankbuster(), "{2}");
        harness.passBothPriorities();

        Permanent vehicle = findPermanent(player1, "Reckoner Bankbuster");
        vehicle.setCounterCount(CounterType.FLYING, 1);
        harness.passBothPriorities();

        assertThat(vehicle.getCounterCount(CounterType.FLYING)).isEqualTo(1);
        harness.assertInHand(player1, "Ornithopter");
    }

    @Test
    void vehicleWithoutPrintedFlyingStillDrawsAfterLeavingWithAFlyingCounter() {
        harness.addToBattlefield(player1, new MilesTailsPrower());
        harness.setLibrary(player1, List.of(new Ornithopter()));
        harness.castFromHand(player1, new ReckonerBankbuster(), "{2}");
        harness.passBothPriorities();

        Permanent vehicle = findPermanent(player1, "Reckoner Bankbuster");
        vehicle.setCounterCount(CounterType.FLYING, 1);
        harness.setHand(player1, List.of(new Disenchant()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, vehicle.getId());
        harness.assertInGraveyard(player1, "Reckoner Bankbuster");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Ornithopter");
    }

    @Test
    void opponentsVehicleDoesNotTrigger() {
        harness.addToBattlefield(player1, new MilesTailsPrower());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ReckonerBankbuster(), "{2}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player2, "Reckoner Bankbuster")
                .getCounterCount(CounterType.FLYING)).isZero();
    }

    @Test
    void flyingArtifactThatIsNotAVehicleDoesNotTrigger() {
        harness.addToBattlefield(player1, new MilesTailsPrower());
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void realFlyingVehicleDrawsWithoutAddingACounter() {
        harness.addToBattlefield(player1, new MilesTailsPrower());
        harness.setLibrary(player1, List.of(new Ornithopter()));
        harness.castFromHand(player1, new SmugglersCopter(), "{2}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Smuggler's Copter")
                .getCounterCount(CounterType.FLYING)).isZero();
        harness.assertInHand(player1, "Ornithopter");
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
