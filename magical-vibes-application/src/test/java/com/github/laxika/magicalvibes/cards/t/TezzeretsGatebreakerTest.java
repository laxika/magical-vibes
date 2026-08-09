package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AerialEngineer;
import com.github.laxika.magicalvibes.cards.a.ArcaneEncyclopedia;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TezzeretsGatebreakerTest extends BaseCardTest {

    @Test
    @DisplayName("Its enters-the-battlefield ability offers blue or artifact cards")
    void offersBlueOrArtifactCard() {
        AerialEngineer aerialEngineer = new AerialEngineer();
        ArcaneEncyclopedia encyclopedia = new ArcaneEncyclopedia();
        setupTopCards(List.of(new Shock(), aerialEngineer, new Island(), encyclopedia, new Swamp()));

        castGatebreaker();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(5);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(aerialEngineer.getId(), encyclopedia.getId());
        assertThat(choice.randomRemainingToBottom()).isTrue();
    }

    @Test
    @DisplayName("Choosing a blue or artifact card puts it into hand and bottoms the rest")
    void choosesEligibleCardAndBottomsRest() {
        AerialEngineer aerialEngineer = new AerialEngineer();
        ArcaneEncyclopedia encyclopedia = new ArcaneEncyclopedia();
        List<Card> topCards = List.of(aerialEngineer, new Shock(), encyclopedia, new Island(), new Swamp());
        setupTopCards(topCards);

        castGatebreaker();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardsChosen(List.of(encyclopedia.getId())));

        assertThat(gd.playerHands.get(player1.getId())).contains(encyclopedia);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrderElementsOf(topCards.stream()
                        .filter(card -> card != encyclopedia)
                        .toList());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Sacrificing it makes only your creatures unblockable this turn")
    void sacrificeMakesOwnCreaturesUnblockable() {
        harness.addToBattlefield(player1, new TezzeretsGatebreaker());
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tezzeret's Gatebreaker");
        assertThat(ownCreature.isCantBeBlocked()).isTrue();
        assertThat(opposingCreature.isCantBeBlocked()).isFalse();
    }

    private void castGatebreaker() {
        harness.setHand(player1, List.of(new TezzeretsGatebreaker()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setupTopCards(List<Card> cards) {
        GameData gameData = harness.getGameData();
        gameData.playerDecks.get(player1.getId()).clear();
        gameData.playerDecks.get(player1.getId()).addAll(cards);
    }
}
