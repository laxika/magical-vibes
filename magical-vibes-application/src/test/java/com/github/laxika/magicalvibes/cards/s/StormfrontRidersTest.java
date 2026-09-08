package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormfrontRiders.class, Boomerang.class, GrizzlyBears.class, Island.class})
class StormfrontRidersTest extends BaseCardTest {

    @Test
    @DisplayName("ETB returning two creatures creates two Soldier tokens")
    void etbReturningTwoCreaturesCreatesTwoSoldiers() {
        UUID firstBearId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        UUID secondBearId = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId();
        harness.setHand(player1, List.of(new StormfrontRiders()));
        addStormfrontMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMultiplePermanentsChosen(player1, List.of(firstBearId, secondBearId));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).hasSize(2)
                .allMatch(permanent -> permanent.getCard().isToken());
        assertThat(countPermanents(player1, "Stormfront Riders")).isEqualTo(1);
    }

    @Test
    @DisplayName("Returning Stormfront Riders itself also creates a Soldier token")
    void returningSelfAlsoCreatesSoldier() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new StormfrontRiders());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        UUID stormfrontId = harness.getPermanentId(player1, "Stormfront Riders");
        harness.castInstant(player1, 0, stormfrontId);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Soldier")).hasSize(1);
        assertThat(countPermanents(player1, "Stormfront Riders")).isZero();
    }

    @Test
    @DisplayName("Returning a noncreature does not trigger Stormfront Riders")
    void returningNoncreatureDoesNotTrigger() {
        harness.addToBattlefield(player1, new StormfrontRiders());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, noncreature.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    private void addStormfrontMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.WHITE, 1);
    }
}
