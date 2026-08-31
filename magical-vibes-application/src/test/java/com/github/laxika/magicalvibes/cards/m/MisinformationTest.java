package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArcaneDenial;
import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import com.github.laxika.magicalvibes.cards.s.ShieldSphere;
import com.github.laxika.magicalvibes.cards.s.SoldeviDigger;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArcaneDenial.class, Misinformation.class, ShieldSphere.class, SoldeviDigger.class,
        StormCrow.class})
class MisinformationTest extends BaseCardTest {

    @Test
    @DisplayName("At most three cards may be chosen")
    void choiceIsCappedAtThree() {
        harness.setGraveyard(player2, List.of(new ArcaneDenial(), new ShieldSphere(), new SoldeviDigger(), new StormCrow()));
        harness.setHand(player1, List.of(new Misinformation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Only cards in an opponent's graveyard are legal targets")
    void onlyOpponentGraveyardCardsAreLegalTargets() {
        Card opponentCard = new ShieldSphere();
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.setGraveyard(player1, List.of(new SoldeviDigger()));
        harness.setHand(player1, List.of(new Misinformation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(opponentCard.getId());
    }

    @Test
    @DisplayName("Chosen cards move to the top of the opponent's library, last chosen on top")
    void chosenCardsGoOnTopOfOpponentLibrary() {
        Card firstCard = new ArcaneDenial();
        Card secondCard = new ShieldSphere();
        harness.setGraveyard(player2, List.of(firstCard, secondCard));
        harness.setHand(player1, List.of(new Misinformation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(firstCard.getId(), secondCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()).subList(0, 2))
                .extracting(Card::getId)
                .containsExactly(secondCard.getId(), firstCard.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Misinformation");
    }

    @Test
    @DisplayName("Choosing fewer than three cards is allowed")
    void choosingFewerThanThreeCardsIsAllowed() {
        Card chosenCard = new StormCrow();
        Card untouchedCard = new ArcaneDenial();
        harness.setGraveyard(player2, List.of(chosenCard, untouchedCard));
        harness.setHand(player1, List.of(new Misinformation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(chosenCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId()).isEqualTo(chosenCard.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(untouchedCard);
    }

    @Test
    @DisplayName("Choosing no cards is allowed when the opponent's graveyard is nonempty")
    void choosingNoCardsIsAllowed() {
        Card retainedCard = new ShieldSphere();
        harness.setGraveyard(player2, List.of(retainedCard));
        harness.setHand(player1, List.of(new Misinformation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(retainedCard);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(retainedCard);
    }

    @Test
    @CardUsed(GroundSeal.class)
    @DisplayName("Ground Seal prevents targeting cards in graveyards")
    void groundSealPreventsTargetingGraveyardCards() {
        Card targetCard = new StormCrow();
        harness.addToBattlefield(player1, new GroundSeal());
        harness.setGraveyard(player2, List.of(targetCard));
        harness.setHand(player1, List.of(new Misinformation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(targetCard);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(targetCard);
    }

    @Test
    @CardUsed(GroundSeal.class)
    @DisplayName("Ground Seal entering after target selection makes targets illegal")
    void groundSealEnteringAfterTargetSelectionMakesTargetsIllegal() {
        Card targetCard = new StormCrow();
        harness.setGraveyard(player2, List.of(targetCard));
        harness.setHand(player1, List.of(new Misinformation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(targetCard.getId()));
        harness.addToBattlefield(player1, new GroundSeal());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(targetCard);
        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(targetCard);
        harness.assertInGraveyard(player1, "Misinformation");
    }

    @Test
    @DisplayName("With an empty opponent graveyard the spell resolves doing nothing")
    void noOpponentGraveyardCardsResolvesWithNoEffect() {
        Card topCard = new StormCrow();
        harness.setGraveyard(player1, List.of(new SoldeviDigger()));
        gd.playerDecks.get(player2.getId()).addFirst(topCard);
        harness.setHand(player1, List.of(new Misinformation()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getId()).isEqualTo(topCard.getId());
        harness.assertInGraveyard(player1, "Soldevi Digger");
        harness.assertInGraveyard(player1, "Misinformation");
    }
}
