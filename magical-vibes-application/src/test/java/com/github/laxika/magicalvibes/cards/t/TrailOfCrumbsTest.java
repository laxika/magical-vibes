package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.Gingerbrute;
import com.github.laxika.magicalvibes.cards.s.ScorchingDragonfire;
import com.github.laxika.magicalvibes.cards.y.YgraEaterOfAll;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TrailOfCrumbs.class, Gingerbrute.class, ScorchingDragonfire.class})
class TrailOfCrumbsTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a Food token")
    void entersWithFoodToken() {
        castTrailOfCrumbs();

        assertThat(countPermanents(player1, "Food")).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing a Food offers a permanent from the top two after paying {1}")
    void sacrificingFoodPaysAndFindsPermanent() {
        Card permanent = new Gingerbrute();
        Card instant = new ScorchingDragonfire();
        harness.setLibrary(player1, List.of(permanent, instant));
        castTrailOfCrumbs();

        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, foodIndex, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards()).containsExactly(permanent);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(permanent);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(instant);
    }

    @Test
    @DisplayName("Declining the payment does not look at the library")
    void decliningPaymentDoesNothing() {
        Card permanent = new Gingerbrute();
        Card instant = new ScorchingDragonfire();
        harness.setLibrary(player1, List.of(permanent, instant));
        castTrailOfCrumbs();

        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, foodIndex, null, null);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(permanent, instant);
    }

    @Test
    @CardUsed({TuinvaleTreefolk.class, YgraEaterOfAll.class})
    void sacrificingCreatureMadeFoodByContinuousEffectTriggers() {
        Card permanent = new TrailOfCrumbs();
        harness.setLibrary(player1, List.of(permanent));
        castTrailOfCrumbs();
        harness.addToBattlefield(player1, new YgraEaterOfAll());
        Permanent treefolk = addCreatureReady(player1, new TuinvaleTreefolk());
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(treefolk);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, index, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(permanent);
    }

    @Test
    void sacrificingNontokenFoodAlsoTriggers() {
        Card permanent = new TrailOfCrumbs();
        harness.setLibrary(player1, List.of(permanent));
        castTrailOfCrumbs();
        harness.addToBattlefield(player1, new Gingerbrute());
        Permanent gingerbrute = findPermanent(player1, "Gingerbrute");
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(gingerbrute);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, index, 1, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(permanent);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(gingerbrute.getCard());
    }

    @Test
    void opponentsFoodSacrificeDoesNotTrigger() {
        Card permanent = new TrailOfCrumbs();
        harness.setLibrary(player1, List.of(permanent));
        castTrailOfCrumbs();
        harness.addToBattlefield(player2, new Gingerbrute());
        Permanent gingerbrute = findPermanent(player2, "Gingerbrute");
        int index = gd.playerBattlefields.get(player2.getId()).indexOf(gingerbrute);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.activateAbility(player2, index, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(permanent);
    }

    @Test
    void mayDeclinePermanentAndOrderBothCardsOnBottom() {
        Card permanent = new TrailOfCrumbs();
        Card instant = new ScorchingDragonfire();
        Card remaining = new TrailOfCrumbs();
        harness.setLibrary(player1, List.of(permanent, instant, remaining));
        castTrailOfCrumbs();

        sacrificeFoodAndPay();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining, instant, permanent);
    }

    @Test
    void noPermanentStillAllowsOrderingBothCardsOnBottom() {
        Card first = new ScorchingDragonfire();
        Card second = new ScorchingDragonfire();
        Card remaining = new TrailOfCrumbs();
        harness.setLibrary(player1, List.of(first, second, remaining));
        castTrailOfCrumbs();

        sacrificeFoodAndPay();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remaining, second, first);
    }

    @Test
    void canTakePermanentFromOneCardLibrary() {
        Card permanent = new TrailOfCrumbs();
        harness.setLibrary(player1, List.of(permanent));
        castTrailOfCrumbs();

        sacrificeFoodAndPay();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(permanent);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void payingWithEmptyLibraryFinishesAndFoodStillGainsLife() {
        harness.setLibrary(player1, List.of());
        castTrailOfCrumbs();
        int lifeBefore = gd.getLife(player1.getId());

        sacrificeFoodAndPay();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
        assertThat(countPermanents(player1, "Food")).isZero();
    }

    private void sacrificeFoodAndPay() {
        Permanent food = findPermanent(player1, "Food");
        int foodIndex = gd.playerBattlefields.get(player1.getId()).indexOf(food);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, foodIndex, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
    }

    private void castTrailOfCrumbs() {
        harness.castFromHand(player1, new TrailOfCrumbs(), "{1}{G}");
        resolveAllTriggers();
    }
}
