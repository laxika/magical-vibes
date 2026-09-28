package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SophiaDoggedDetective.class, GrizzlyBears.class})
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
        assertThat(tiny.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(gqs.hasKeyword(gd, tiny, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void DogCombatDamageCreatesFoodThenClue() {
        addCreatureReady(player1, new SophiaDoggedDetective());
        Permanent dog = addDogToken(player1);
        addCreatureReady(player1, new GrizzlyBears()).setAttacking(true);
        dog.setAttacking(true);

        resolveCombatDamage();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void sacrificesArtifactTokenToPutCountersOnEachDog() {
        addCreatureReady(player1, new SophiaDoggedDetective());
        Permanent firstDog = addDogToken(player1);
        Permanent secondDog = addDogToken(player1);
        Permanent nonDog = addCreatureReady(player1, new GrizzlyBears());
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
        return addReady(player, dog);
    }

    private Permanent addFoodToken(com.github.laxika.magicalvibes.model.Player player) {
        Card food = new Card();
        food.setName("Food");
        food.setType(CardType.ARTIFACT);
        food.setToken(true);
        food.setSubtypes(List.of(CardSubtype.FOOD));
        return addReady(player, food);
    }

    private Permanent addReady(com.github.laxika.magicalvibes.model.Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void resolveCombatDamage() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
