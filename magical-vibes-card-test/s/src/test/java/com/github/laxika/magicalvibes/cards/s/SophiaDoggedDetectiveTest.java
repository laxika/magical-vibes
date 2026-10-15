package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WhirlerRogue;
import com.github.laxika.magicalvibes.cards.m.MirrorEntity;
import com.github.laxika.magicalvibes.cards.e.ErdwalIlluminator;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
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

@CardUsed({SophiaDoggedDetective.class, WhirlerRogue.class, MirrorEntity.class, ErdwalIlluminator.class})
class SophiaDoggedDetectiveTest extends BaseCardTest {

    @Test
    void entersWithLegendaryTramplingTinyToken() {
        harness.setHand(player1, List.of(new SophiaDoggedDetective()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent tiny = findPermanent(player1, "Tiny");
        assertThat(tiny.getCard().isToken()).isTrue();
        assertThat(tiny.getCard().getPower()).isEqualTo(2);
        assertThat(tiny.getCard().getToughness()).isEqualTo(2);
        assertThat(tiny.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(tiny.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.DOG, CardSubtype.DETECTIVE);
        assertThat(tiny.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(gqs.hasKeyword(gd, tiny, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void DogCombatDamageCreatesFoodThenClue() {
        addCreatureReady(player1, new SophiaDoggedDetective());
        Permanent dog = addDogToken(player1);
        addCreatureReady(player1, new WhirlerRogue()).setAttacking(true);
        dog.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void sacrificesArtifactTokenToPutCountersOnEachDog() {
        addCreatureReady(player1, new SophiaDoggedDetective());
        Permanent firstDog = addDogToken(player1);
        Permanent secondDog = addDogToken(player1);
        Permanent nonDog = addCreatureReady(player1, new WhirlerRogue());
        addFoodToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(firstDog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(secondDog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonDog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addDogToken(com.github.laxika.magicalvibes.model.Player player) {
        Card dog = new Card();
        dog.setName("Dog");
        dog.setType(CardType.CREATURE);
        dog.setPower(2);
        dog.setToughness(2);
        dog.setToken(true);
        dog.setSubtypes(List.of(CardSubtype.DOG));
        return addCreatureReady(player, dog);
    }

    private Permanent addFoodToken(com.github.laxika.magicalvibes.model.Player player) {
        Card food = new Card();
        food.setName("Food");
        food.setType(CardType.ARTIFACT);
        food.setToken(true);
        food.setSubtypes(List.of(CardSubtype.FOOD));
        return harness.addToBattlefieldAndReturn(player, food);
    }

    @Test
    void eachDogIncludingChangelingsTriggersSeparatelyAndInvestigates() {
        addCreatureReady(player1, new SophiaDoggedDetective());
        addCreatureReady(player1, new ErdwalIlluminator());
        addDogToken(player1).setAttacking(true);
        addCreatureReady(player1, new MirrorEntity()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).hasSize(2);
        assertThat(findPermanents(player1, "Clue")).hasSize(3);
        assertThat(findPermanents(player2, "Food")).isEmpty();
        assertThat(findPermanents(player2, "Clue")).isEmpty();
    }

    @Test
    void opponentsDogDoesNotTriggerSophia() {
        addCreatureReady(player1, new SophiaDoggedDetective());
        addCreatureReady(player2, new MirrorEntity()).setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void nonDogCombatDamageDoesNotCreateTokens() {
        addCreatureReady(player1, new SophiaDoggedDetective()).setAttacking(true);
        addCreatureReady(player1, new WhirlerRogue()).setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void countersIncludeNoncreatureKindredDogsButExcludeOpponentsDogs() {
        addCreatureReady(player1, new SophiaDoggedDetective());
        Card kindredDog = new Card();
        kindredDog.setName("Kindred Dog enchantment");
        kindredDog.setType(CardType.ENCHANTMENT);
        kindredDog.setAdditionalTypes(Set.of(CardType.KINDRED));
        kindredDog.setSubtypes(List.of(CardSubtype.DOG));
        Permanent ownDog = harness.addToBattlefieldAndReturn(player1, kindredDog);
        Permanent opposingDog = addCreatureReady(player2, new MirrorEntity());
        addFoodToken(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(ownDog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingDog.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(findPermanents(player1, "Food")).isEmpty();
    }
}
