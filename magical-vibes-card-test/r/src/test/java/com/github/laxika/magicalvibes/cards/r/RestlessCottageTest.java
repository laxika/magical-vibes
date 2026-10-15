package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RestlessCottage.class, GrizzlyBears.class})
class RestlessCottageTest extends BaseCardTest {

    @Test
    @DisplayName("Restless Cottage enters tapped and produces black or green mana")
    void entersTappedAndProducesMana() {
        harness.setHand(player1, List.of(new RestlessCottage()));
        harness.playLand(player1, 0);

        Permanent cottage = findPermanent(player1, "Restless Cottage");
        assertThat(cottage.isTapped()).isTrue();

        cottage.untap();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Restless Cottage becomes a 4/4 black and green Horror and stays a land")
    void animatesIntoHorror() {
        Permanent cottage = addReadyCottage(player1);
        animateCottage(player1);

        assertThat(gqs.isCreature(gd, cottage)).isTrue();
        assertThat(gqs.isLand(gd, cottage)).isTrue();
        assertThat(gqs.getEffectivePower(gd, cottage)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, cottage)).isEqualTo(4);
        assertThat(gqs.getEffectiveColors(gd, cottage))
                .containsExactlyInAnyOrder(CardColor.BLACK, CardColor.GREEN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, cottage)).contains(CardSubtype.HORROR);
    }

    @Test
    @DisplayName("Attacking with Restless Cottage creates Food and exiles a chosen graveyard card")
    void attackingCreatesFoodAndExilesChosenCard() {
        Permanent cottage = addReadyCottage(player1);
        Card ownCard = new GrizzlyBears();
        Card opposingCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opposingCard));
        animateCottage(player1);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(cottage)));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownCard.getId(), opposingCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(opposingCard.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opposingCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard);
    }

    @Test
    @DisplayName("Restless Cottage still creates Food when its graveyard exile is declined")
    void attackingMayExileNothing() {
        Permanent cottage = addReadyCottage(player1);
        Card graveyardCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(graveyardCard));
        animateCottage(player1);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(cottage)));
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Restless Cottage still creates Food when all graveyards are empty")
    void attackingWithEmptyGraveyardsStillCreatesFood() {
        Permanent cottage = addReadyCottage(player1);
        animateCottage(player1);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(cottage)));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    @DisplayName("Restless Cottage can produce black mana")
    void producesBlackMana() {
        Permanent cottage = addReadyCottage(player1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(cottage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Restless Cottage's animation ends during cleanup")
    void animationEndsDuringCleanup() {
        Permanent cottage = addReadyCottage(player1);
        animateCottage(player1);
        assertThat(gqs.isCreature(gd, cottage)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.isCreature(gd, cottage)).isFalse();
        assertThat(gqs.isLand(gd, cottage)).isTrue();
        assertThat(gqs.getEffectiveColors(gd, cottage)).isEmpty();
        assertThat(gqs.effectiveCreatureSubtypes(gd, cottage)).doesNotContain(CardSubtype.HORROR);
    }

    @Test
    @DisplayName("An illegal graveyard target prevents Food creation as well as exile")
    void illegalGraveyardTargetStopsEntireAttackAbility() {
        Permanent cottage = addReadyCottage(player1);
        Card graveyardCard = new RestlessCottage();
        harness.setGraveyard(player2, List.of(graveyardCard));
        animateCottage(player1);

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(cottage)));
        harness.handleMultipleCardsChosen(player1, List.of(graveyardCard.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(graveyardCard));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(graveyardCard);
    }

    @Test
    @DisplayName("Food created by Restless Cottage can be sacrificed for three life")
    void createdFoodCanBeSacrificedForLife() {
        Permanent cottage = addReadyCottage(player1);
        animateCottage(player1);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(cottage)));
        resolveAllTriggers();
        Permanent food = findPermanent(player1, "Food");
        harness.setLife(player1, 10);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(food), 0, null, null);

        assertThat(findPermanents(player1, "Food")).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        harness.passBothPriorities();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(13);
    }

    private void animateCottage(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 2);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.activateAbility(player, 0, 1, null, null);
        harness.passBothPriorities();
    }

    private Permanent addReadyCottage(Player player) {
        return addCreatureReady(player, new RestlessCottage());
    }
}
