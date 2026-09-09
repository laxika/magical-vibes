package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PathToExile;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OracleOfDust.class, Forest.class, GrizzlyBears.class, PathToExile.class})
class OracleOfDustTest extends BaseCardTest {

    @Test
    void processesAnOpponentOwnedExiledCardThenDrawsAndDiscards() {
        Permanent oracle = addReadyOracle();
        GrizzlyBears bears = new GrizzlyBears();
        Forest drawnCard = new Forest();
        PathToExile exiledCard = new PathToExile();
        harness.setHand(player1, List.of(bears));
        setDeck(player1, List.of(drawnCard));
        harness.setExile(player2, List.of(exiledCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(oracle.isTapped()).isFalse();
        assertThat(gd.findExiledCard(exiledCard.getId())).isNull();
        harness.assertInGraveyard(player2, "Path to Exile");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(bears.getId(), drawnCard.getId());

        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void promptsToChooseAmongMultipleOpponentOwnedExiledCards() {
        addReadyOracle();
        PathToExile first = new PathToExile();
        PathToExile second = new PathToExile();
        harness.setExile(player2, List.of(first, second));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PutOpponentOwnedExiledCardIntoGraveyardCostChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PutOpponentOwnedExiledCardIntoGraveyardCostChoice.class);
        assertThat(choice.validCardIds()).containsExactly(first.getId(), second.getId());

        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));

        assertThat(gd.findExiledCard(first.getId())).isNotNull();
        assertThat(gd.findExiledCard(second.getId())).isNull();
        harness.assertInGraveyard(player2, "Path to Exile");
    }

    @Test
    void cannotActivateWithoutAnOpponentOwnedExiledCard() {
        addReadyOracle();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private Permanent addReadyOracle() {
        OracleOfDust card = new OracleOfDust();
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }

    private void setDeck(com.github.laxika.magicalvibes.model.Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
