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

    @Test
    void doesNotCreateADogDuringOpponentsUpkeep() {
        harness.addToBattlefieldAndReturn(player1, new AGirlAndHerDogs());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void canGiveSuccessiveDogsDifferentArbitraryNames() {
        harness.enterBattlefieldAndReturn(player1, new AGirlAndHerDogs());
        resolveAllTriggers();
        harness.handleListChoice(player1, "My First Very Good Dog");

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleListChoice(player1, "My Second Very Good Dog");

        assertThat(findPermanents(player1, "My First Very Good Dog")).hasSize(1);
        assertThat(findPermanents(player1, "My Second Very Good Dog")).hasSize(1);
    }

    @Test
    void attackBonusCountsItsDogButNotOpposingLegendsOrNonlegendaryCreatures() {
        Permanent girl = harness.enterBattlefieldAndReturn(player1, new AGirlAndHerDogs());
        girl.setSummoningSick(false);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Pax");
        harness.addToBattlefieldAndReturn(player1, new AGirlAndHerDogs());
        addCreatureReady(player2, new IsamaruHoundOfKonda());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, girl)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, girl)).isEqualTo(4);
    }

    @Test
    void attackWithoutLegendaryCreaturesDoesNotBoostItself() {
        Permanent girl = addCreatureReady(player1, new AGirlAndHerDogs());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, girl)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, girl)).isEqualTo(3);
    }

    @Test
    void attackBonusIncludesEachDifferentlyNamedLegendaryDog() {
        Permanent girl = harness.enterBattlefieldAndReturn(player1, new AGirlAndHerDogs());
        resolveAllTriggers();
        harness.handleListChoice(player1, "Pax");
        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleListChoice(player1, "Rover");

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, girl)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, girl)).isEqualTo(5);
    }
}
