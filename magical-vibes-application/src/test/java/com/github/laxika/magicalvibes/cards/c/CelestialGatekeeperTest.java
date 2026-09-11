package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DauntingDefender;
import com.github.laxika.magicalvibes.cards.s.StormCrow;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CelestialGatekeeper.class, WrathOfGod.class, StormCrow.class,
        DauntingDefender.class, GrizzlyBears.class})
class CelestialGatekeeperTest extends BaseCardTest {

    private Card killGatekeeper(List<Card> graveyard) {
        Permanent gatekeeper = harness.addToBattlefieldAndReturn(player1, new CelestialGatekeeper());
        Card gatekeeperCard = gatekeeper.getCard();
        harness.setGraveyard(player1, graveyard);

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        return gatekeeperCard;
    }

    @Test
    @DisplayName("When Celestial Gatekeeper dies, it exiles itself and returns up to two targeted Bird or Cleric permanents")
    void exilesItselfAndReturnsTwoMatchingPermanents() {
        Card bird = new StormCrow();
        Card cleric = new DauntingDefender();
        Card gatekeeperCard = killGatekeeper(List.of(bird, cleric));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bird.getId(), cleric.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(gatekeeperCard.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(bird.getId(), cleric.getId());
    }

    @Test
    @DisplayName("Celestial Gatekeeper can return only one of its two available targets")
    void returnsOnlyOneChosenPermanent() {
        Card bird = new StormCrow();
        Card cleric = new DauntingDefender();
        killGatekeeper(List.of(bird, cleric));

        harness.handleMultipleCardsChosen(player1, List.of(bird.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .containsExactly(bird.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(cleric.getId());
    }

    @Test
    @DisplayName("Only Bird and Cleric permanent cards are offered as targets")
    void excludesNonMatchingCards() {
        Card bears = new GrizzlyBears();
        Card gatekeeperCard = killGatekeeper(List.of(bears));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(gatekeeperCard.getId());
        assertThat(choice.validCardIds()).doesNotContain(bears.getId());
    }
}
