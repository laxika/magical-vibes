package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragTheCanal.class, GrizzlyBears.class})
class DragTheCanalTest extends BaseCardTest {

    @Test
    void createsDetectiveWithoutMorbidBonus() {
        castDragTheCanal();

        assertThat(findPermanents(player1, "Detective")).hasSize(1);
        harness.assertLife(player1, 20);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    @Test
    void gainsLifeSurveilsAndInvestigatesWhenCreatureDiedThisTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Card keptCard = new GrizzlyBears();
        Card surveiledCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(surveiledCard, keptCard));
        bears.setMarkedDamage(2);
        harness.runStateBasedActions();

        castDragTheCanal();

        GameData gd = harness.getGameData();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(findPermanents(player1, "Detective")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        harness.assertLife(player1, 22);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(surveiledCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(keptCard);
    }

    @Test
    void createsWhiteAndBlueDetectiveToken() {
        castDragTheCanal();

        var detective = findPermanent(player1, "Detective");
        assertThat(detective.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(detective.getCard().getSubtypes()).contains(CardSubtype.DETECTIVE);
        assertThat(detective.getEffectivePower()).isEqualTo(2);
        assertThat(detective.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    void checksOpponentsCreatureDeathAfterCastingAndInvestigatesWithEmptyLibrary() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new DragTheCanal(), "{U}{B}");

        bears.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Detective")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);

        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int clueIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(findPermanent(player1, "Clue"));
        harness.activateAbility(player1, clueIndex, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    void creatureCardAlreadyInGraveyardDoesNotEnableBonus() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Card libraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(libraryCard));

        castDragTheCanal();

        harness.assertLife(player1, 20);
        assertThat(findPermanents(player1, "Detective")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    private void castDragTheCanal() {
        harness.castFromHand(player1, new DragTheCanal(), "{U}{B}");
        harness.passBothPriorities();
    }
}
