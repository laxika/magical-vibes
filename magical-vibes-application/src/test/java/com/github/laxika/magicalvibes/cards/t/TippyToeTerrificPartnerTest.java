package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AcademyManufactor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.ForswornPaladin;
import com.github.laxika.magicalvibes.cards.r.RangerClass;
import com.github.laxika.magicalvibes.cards.y.YouSeeAPairOfGoblins;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TippyToeTerrificPartner.class, ForswornPaladin.class, Forest.class,
        AcademyManufactor.class, RangerClass.class, YouSeeAPairOfGoblins.class})
class TippyToeTerrificPartnerTest extends BaseCardTest {

    @Test
    void addsFoodToTokenCreationEvent() {
        createTreasureAndFood();

        assertThat(countPermanents(player1, "Treasure")).isOne();
        assertThat(countPermanents(player1, "Food")).isOne();
    }

    @Test
    void drawsAtEndStepAfterGainingLife() {
        createTreasureAndFood();
        harness.setLibrary(player1, List.of(new Forest()));

        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, foodIndex, 0, null, null);
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    void doesNotDrawAtEndStepWithoutLifeGain() {
        harness.addToBattlefield(player1, new TippyToeTerrificPartner());
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void academyManufactorReplacesFoodAddedToCreatureTokenEvent() {
        harness.addToBattlefield(player1, new TippyToeTerrificPartner());
        harness.addToBattlefield(player1, new AcademyManufactor());
        harness.setHand(player1, List.of(new RangerClass()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Wolf")).isOne();
        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(countPermanents(player1, "Clue")).isOne();
        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    @Test
    void doesNotAddFoodToOpponentsTokenCreation() {
        harness.addToBattlefield(player1, new TippyToeTerrificPartner());
        addCreatureReady(player2, new ForswornPaladin());
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player2, "Treasure")).isOne();
        assertThat(countPermanents(player2, "Food")).isZero();
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    @Test
    void lifeGainedAfterEndStepBeginsDoesNotTriggerDraw() {
        createTreasureAndFood();
        harness.setLibrary(player1, List.of(new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();

        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, foodIndex, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotDrawDuringOpponentsEndStepAfterGainingLife() {
        createTreasureAndFood();
        harness.setLibrary(player1, List.of(new Forest()));
        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, foodIndex, 0, null, null);
        harness.passBothPriorities();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void addsOnlyOneFoodWhenCreatingMultipleTokensAtOnce() {
        harness.addToBattlefield(player1, new TippyToeTerrificPartner());
        harness.setHand(player1, List.of(new YouSeeAPairOfGoblins()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalInstant(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Goblin")).isEqualTo(2);
        assertThat(countPermanents(player1, "Food")).isOne();
    }

    private void createTreasureAndFood() {
        harness.addToBattlefield(player1, new TippyToeTerrificPartner());
        addCreatureReady(player1, new ForswornPaladin());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();
    }
}
