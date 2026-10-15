package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.a.ArmoredArmadillo;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BonnyPallClearcutter.class, Forest.class, ArmoredArmadillo.class, Island.class})
class BonnyPallClearcutterTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a legendary Beau whose power and toughness equal your land count")
    void createsDynamicBeauToken() {
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        harness.setHand(player1, List.of(new BonnyPallClearcutter()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent beau = findPermanent(player1, "Beau");
        assertThat(beau.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(beau.getCard().getSubtypes()).contains(CardSubtype.OX);
        assertThat(beau.getCard().getSupertypes()).contains(CardSupertype.LEGENDARY);
        assertThat(gqs.getEffectivePower(gd, beau)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, beau)).isEqualTo(2);

        harness.addToBattlefield(player1, new Forest());

        assertThat(gqs.getEffectivePower(gd, beau)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, beau)).isEqualTo(3);
    }

    @Test
    @DisplayName("Draws before offering a land from hand to the battlefield when attacking")
    void drawsThenPutsLandFromHandOntoBattlefield() {
        addBonnyAndArmadillo();
        Card landInHand = new Forest();
        Card drawnLand = new Island();
        harness.setHand(player1, List.of(landInHand));
        harness.setLibrary(player1, List.of(drawnLand));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnLand, landInHand);
        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(landInHand.getId(), drawnLand.getId());

        harness.handleMultipleCardsChosen(player1, List.of(landInHand.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnLand);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(landInHand.getId()));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Offers a land from the graveyard and can decline the choice")
    void putsLandFromGraveyardOrDeclines() {
        addBonnyAndArmadillo();
        Card landInGraveyard = new Forest();
        Card drawnLand = new Island();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(drawnLand));
        harness.setGraveyard(player1, List.of(landInGraveyard));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnLand);
        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(landInGraveyard.getId(), drawnLand.getId());

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(landInGraveyard);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .noneMatch(permanent -> permanent.getCard().getId().equals(landInGraveyard.getId())))
                .isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Returns a graveyard land when another creature attacks without Bonny")
    void returnsGraveyardLandWithoutBonnyAttacking() {
        addBonnyAndArmadillo();
        Card land = new Forest();
        Card nonland = new ArmoredArmadillo();
        Card drawnCard = new BonnyPallClearcutter();
        harness.setGraveyard(player1, List.of(land, nonland));
        harness.setLibrary(player1, List.of(drawnCard));

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        PendingInteraction.PutCardFromHandOrGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutCardFromHandOrGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(land.getId());
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonland);
        Permanent returnedLand = findPermanent(player1, "Forest");
        assertThat(returnedLand.getCard().getId()).isEqualTo(land.getId());
        assertThat(returnedLand.isTapped()).isFalse();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The land drawn by the attack trigger can be put onto the battlefield")
    void putsJustDrawnLandOntoBattlefield() {
        addBonnyAndArmadillo();
        Card drawnLand = new Island();
        harness.setLibrary(player1, List.of(drawnLand));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(drawnLand.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanent(player1, "Island").getCard().getId()).isEqualTo(drawnLand.getId());
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Draws without offering nonlands or an opponent's lands")
    void drawsWithNoEligibleLand() {
        addBonnyAndArmadillo();
        Card drawnCard = new ArmoredArmadillo();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setGraveyard(player1, List.of(new BonnyPallClearcutter()));
        harness.setHand(player2, List.of(new Forest()));
        harness.setGraveyard(player2, List.of(new Island()));

        declareAttackers(List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void addBonnyAndArmadillo() {
        addCreatureReady(player1, new BonnyPallClearcutter());
        addCreatureReady(player1, new ArmoredArmadillo());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
    }
}
