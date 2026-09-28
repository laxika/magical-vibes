package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.i.IsamaruHoundOfKonda;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AGirlAndHerDogs.class, IsamaruHoundOfKonda.class})
class AGirlAndHerDogsTest extends BaseCardTest {

    @Test
    void entersWithAChosenNameForALegendaryDogToken() {
        harness.enterBattlefieldAndReturn(player1, new AGirlAndHerDogs());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Pax");

        Permanent dog = findPermanents(player1, "Pax").stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(dog.getCard().getSubtypes()).contains(CardSubtype.DOG);
        assertThat(dog.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(dog.getEffectivePower()).isEqualTo(1);
        assertThat(dog.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void createsAnotherChosenNameTokenAtUpkeep() {
        harness.addToBattlefieldAndReturn(player1, new AGirlAndHerDogs());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Rover");

        assertThat(findPermanents(player1, "Rover"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1);
    }

    @Test
    void getsPlusOneForEachLegendaryCreatureControlledWhenAttacking() {
        Permanent girl = addCreatureReady(player1, new AGirlAndHerDogs());
        addCreatureReady(player1, new IsamaruHoundOfKonda());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, girl)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, girl)).isEqualTo(4);
    }
}
