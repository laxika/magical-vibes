package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DevkarinDissident;
import com.github.laxika.magicalvibes.cards.u.UnexplainedDisappearance;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodOperative.class, DevkarinDissident.class, UnexplainedDisappearance.class})
class BloodOperativeTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may exile a target card from a graveyard")
    void etbExilesTargetGraveyardCard() {
        Card target = new DevkarinDissident();
        harness.setGraveyard(player2, List.of(target));
        harness.castFromHand(player1, new BloodOperative());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(target);
    }

    @Test
    @DisplayName("Surveiling may pay 3 life to return Blood Operative from the graveyard")
    void surveilReturnsBloodOperativeToHandForThreeLife() {
        Card bloodOperative = new BloodOperative();
        Card topCard = new DevkarinDissident();
        harness.setGraveyard(player1, List.of(bloodOperative));
        harness.setLibrary(player1, List.of(topCard));
        harness.addToBattlefield(player2, new DevkarinDissident());
        harness.setHand(player1, List.of(new UnexplainedDisappearance()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Devkarin Dissident"));
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).contains(bloodOperative);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bloodOperative);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    void etbCanExileNoncreatureFromOwnGraveyard() {
        Card target = new UnexplainedDisappearance();
        harness.setGraveyard(player1, List.of(target));
        harness.castFromHand(player1, new BloodOperative());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(target);
    }

    @Test
    void etbExileMayBeDeclined() {
        Card target = new DevkarinDissident();
        harness.setGraveyard(player2, List.of(target));
        harness.castFromHand(player1, new BloodOperative());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(target);
        assertThat(gd.getPlayerExiledCards(player2.getId())).doesNotContain(target);
    }

    @Test
    void surveillingBloodOperativeIntoGraveyardTriggersItsReturn() {
        Card bloodOperative = new BloodOperative();
        harness.setGraveyard(player1, List.of());
        harness.setLibrary(player1, List.of(bloodOperative));
        harness.setLife(player1, 20);
        castSurveilSpell();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bloodOperative);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).contains(bloodOperative);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bloodOperative);
    }

    @Test
    void returnMayBeDeclinedEvenWhenSurveilledCardStaysOnTop() {
        Card bloodOperative = new BloodOperative();
        Card topCard = new DevkarinDissident();
        harness.setGraveyard(player1, List.of(bloodOperative));
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);
        castSurveilSpell();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bloodOperative);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bloodOperative);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    void surveillingEmptyLibraryStillTriggersReturn() {
        Card bloodOperative = new BloodOperative();
        harness.setGraveyard(player1, List.of(bloodOperative));
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);
        castSurveilSpell();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
        assertThat(gd.playerHands.get(player1.getId())).contains(bloodOperative);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(bloodOperative);
    }

    @Test
    void cannotReturnWithoutEnoughLifeToPay() {
        Card bloodOperative = new BloodOperative();
        harness.setGraveyard(player1, List.of(bloodOperative));
        harness.setLibrary(player1, List.of(new DevkarinDissident()));
        harness.setLife(player1, 2);
        castSurveilSpell();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bloodOperative);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bloodOperative);
    }

    @Test
    void opponentsSurveilDoesNotTriggerReturn() {
        Card bloodOperative = new BloodOperative();
        harness.setGraveyard(player2, List.of(bloodOperative));
        harness.setLibrary(player1, List.of(new DevkarinDissident()));
        harness.setLife(player2, 20);
        castSurveilSpell();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bloodOperative);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(bloodOperative);
    }

    @Test
    void returnDoesNothingIfSourceLeavesGraveyardBeforeResolution() {
        Card bloodOperative = new BloodOperative();
        harness.setGraveyard(player1, List.of(bloodOperative));
        harness.setLibrary(player1, List.of(new DevkarinDissident()));
        harness.setLife(player1, 20);
        castSurveilSpell();
        harness.handleMayAbilityChosen(player1, false);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(bloodOperative));
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(bloodOperative);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bloodOperative);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void eachGraveyardCopyRequiresItsOwnLifePayment() {
        Card first = new BloodOperative();
        Card second = new BloodOperative();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(new DevkarinDissident()));
        harness.setLife(player1, 20);
        castSurveilSpell();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(14);
        assertThat(gd.playerHands.get(player1.getId())).contains(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
    }

    private void castSurveilSpell() {
        harness.addToBattlefield(player2, new DevkarinDissident());
        harness.setHand(player1, List.of(new UnexplainedDisappearance()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0,
                harness.getPermanentId(player2, "Devkarin Dissident"));
    }
}
