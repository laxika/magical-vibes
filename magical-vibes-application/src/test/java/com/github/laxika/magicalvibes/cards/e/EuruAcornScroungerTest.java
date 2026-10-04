package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.c.Chitterspitter;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EuruAcornScrounger.class, Chitterspitter.class, GrizzlyBears.class})
class EuruAcornScroungerTest extends BaseCardTest {

    @Test
    void foragingConjuresChitterspitterOntoTheBattlefield() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new EuruAcornScrounger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Chitterspitter")).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard().isToken()).isFalse());
    }

    @Test
    void decliningForageDoesNotConjureChitterspitter() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new EuruAcornScrounger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(findPermanents(player1, "Chitterspitter")).isEmpty();
    }

    @Test
    void squirrelCombatDamageMaySacrificeATokenToPutAnAcornCounterOnChitterspitter() {
        addCreatureReady(player1, new EuruAcornScrounger());
        Permanent chitterspitter = addCreatureReady(player1, new Chitterspitter());
        Permanent squirrel = addSquirrelToken();
        Permanent secondSquirrel = addSquirrelToken();

        resolveCombat();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, squirrel.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(squirrel);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(secondSquirrel);
        assertThat(chitterspitter.getCounterCount(CounterType.ACORN)).isEqualTo(1);
    }

    @Test
    void chitterspitterScalesSquirrelsWithItsAcornCounters() {
        Permanent chitterspitter = addCreatureReady(player1, new Chitterspitter());
        Permanent squirrel = addSquirrelToken();
        chitterspitter.setCounterCount(CounterType.ACORN, 2);

        assertThat(gqs.getEffectivePower(gd, squirrel)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, squirrel)).isEqualTo(3);
    }

    private Permanent addSquirrelToken() {
        Card squirrel = new Card();
        squirrel.setName("Squirrel");
        squirrel.setType(CardType.CREATURE);
        squirrel.setManaCost("");
        squirrel.setPower(1);
        squirrel.setToughness(1);
        squirrel.setToken(true);
        squirrel.setSubtypes(List.of(CardSubtype.SQUIRREL));

        Permanent permanent = new Permanent(squirrel);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    @Test
    void forageConjuringUsesASeparateTriggerWithAResponseWindow() {
        harness.setGraveyard(player1, List.of(new Chitterspitter(), new Chitterspitter(), new Chitterspitter()));
        harness.setHand(player1, List.of(new EuruAcornScrounger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Chitterspitter")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();
        assertThat(findPermanents(player1, "Chitterspitter")).hasSize(1);
    }

    @Test
    void sacrificingFoodForagesWithoutExilingGraveyardCards() {
        Card food = new Card();
        food.setName("Food");
        food.setType(CardType.ARTIFACT);
        food.setManaCost("");
        food.setSubtypes(List.of(CardSubtype.FOOD));
        food.setToken(true);
        Permanent foodPermanent = harness.addToBattlefieldAndReturn(player1, food);
        harness.setGraveyard(player1, List.of(new Chitterspitter(), new Chitterspitter()));
        harness.setHand(player1, List.of(new EuruAcornScrounger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, foodPermanent.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(foodPermanent);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(findPermanents(player1, "Chitterspitter")).hasSize(1);
    }

    @Test
    void decliningTokenSacrificeLeavesTokensAndCountersUnchanged() {
        addCreatureReady(player1, new EuruAcornScrounger());
        Permanent chitterspitter = harness.addToBattlefieldAndReturn(player1, new Chitterspitter());
        Permanent squirrel = addSquirrelToken();

        resolveCombat();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(squirrel);
        assertThat(chitterspitter.getCounterCount(CounterType.ACORN)).isZero();
    }

    @Test
    void oneSacrificeCountersEveryControlledChitterspitterButNotOpponents() {
        addCreatureReady(player1, new EuruAcornScrounger());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Chitterspitter());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Chitterspitter());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new Chitterspitter());
        Permanent squirrel = addSquirrelToken();
        addSquirrelToken();

        resolveCombat();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, squirrel.getId());
        resolveAllTriggers();

        assertThat(first.getCounterCount(CounterType.ACORN)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.ACORN)).isEqualTo(1);
        assertThat(opposing.getCounterCount(CounterType.ACORN)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void fewerThanThreeGraveyardCardsWithoutFoodCannotConjure() {
        harness.setGraveyard(player1, List.of(new Chitterspitter(), new Chitterspitter()));
        harness.setHand(player1, List.of(new EuruAcornScrounger()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(findPermanents(player1, "Chitterspitter")).isEmpty();
    }

    @Test
    void eurusOwnCombatDamageAllowsSacrificingANoncreatureToken() {
        Permanent euru = addCreatureReady(player1, new EuruAcornScrounger());
        euru.setAttacking(true);
        Permanent chitterspitter = harness.addToBattlefieldAndReturn(player1, new Chitterspitter());
        Card food = new Card();
        food.setName("Food");
        food.setType(CardType.ARTIFACT);
        food.setManaCost("");
        food.setSubtypes(List.of(CardSubtype.FOOD));
        food.setToken(true);
        Permanent foodPermanent = harness.addToBattlefieldAndReturn(player1, food);

        resolveCombat();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, foodPermanent.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(foodPermanent);
        assertThat(chitterspitter.getCounterCount(CounterType.ACORN)).isEqualTo(1);
    }

    @Test
    void nonsquirrelCombatDamageDoesNotOfferATokenSacrifice() {
        addCreatureReady(player1, new EuruAcornScrounger());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        Permanent chitterspitter = harness.addToBattlefieldAndReturn(player1, new Chitterspitter());
        Permanent squirrel = addSquirrelToken();
        squirrel.setAttacking(false);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(squirrel);
        assertThat(chitterspitter.getCounterCount(CounterType.ACORN)).isZero();
    }

}
