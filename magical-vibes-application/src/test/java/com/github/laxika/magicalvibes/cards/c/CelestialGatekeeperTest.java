package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenEnvoy;
import com.github.laxika.magicalvibes.cards.d.DaruMender;
import com.github.laxika.magicalvibes.cards.v.VexingBeetle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CelestialGatekeeper.class, AvenEnvoy.class, DaruMender.class, VexingBeetle.class})
class CelestialGatekeeperTest extends BaseCardTest {

    private Card killGatekeeper(List<Card> graveyard) {
        Permanent gatekeeper = harness.addToBattlefieldAndReturn(player1, new CelestialGatekeeper());
        Card gatekeeperCard = gatekeeper.getCard();
        harness.setGraveyard(player1, graveyard);
        gatekeeper.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();
        return gatekeeperCard;
    }

    @Test
    @DisplayName("When Celestial Gatekeeper dies, it exiles itself and returns up to two targeted Bird or Cleric permanents")
    void exilesItselfAndReturnsTwoMatchingPermanents() {
        Card bird = new AvenEnvoy();
        Card cleric = new DaruMender();
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
        Card bird = new AvenEnvoy();
        Card cleric = new DaruMender();
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
        Card nonMatching = new VexingBeetle();
        Card gatekeeperCard = killGatekeeper(List.of(nonMatching));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).contains(gatekeeperCard.getId());
        assertThat(choice.validCardIds()).doesNotContain(nonMatching.getId());
    }

    @Test
    @DisplayName("The dying Gatekeeper may be targeted but cannot return after it is exiled")
    void cannotReturnTheExiledSource() {
        Card gatekeeperCard = killGatekeeper(List.of());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(gatekeeperCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(gatekeeperCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(gatekeeperCard.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .doesNotContain(gatekeeperCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(gatekeeperCard.getId());
    }

    @Test
    @DisplayName("The up-to-two ability may choose zero cards and still exile its source")
    void mayChooseNoCards() {
        Card gatekeeperCard = killGatekeeper(List.of());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(gatekeeperCard.getId());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The ability only targets matching permanent cards in its controller's graveyard")
    void targetsOnlyItsControllersGraveyard() {
        Card ownBird = new AvenEnvoy();
        Card opponentCleric = new DaruMender();
        harness.setGraveyard(player2, List.of(opponentCleric));
        Card gatekeeperCard = killGatekeeper(List.of(ownBird));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds())
                .containsExactlyInAnyOrder(gatekeeperCard.getId(), ownBird.getId())
                .doesNotContain(opponentCleric.getId());

        harness.handleMultipleCardsChosen(player1, List.of(ownBird.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .contains(ownBird.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(opponentCleric.getId());
    }
}
