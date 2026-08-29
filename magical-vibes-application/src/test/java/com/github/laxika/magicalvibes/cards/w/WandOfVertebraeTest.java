package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WandOfVertebraeTest extends BaseCardTest {

    @Test
    void millsOneCardFromTargetPlayersLibrary() {
        addReadyWand(player1);
        Card topCard = gd.playerDecks.get(player2.getId()).getFirst();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).doesNotContain(topCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(topCard);
    }

    @Test
    void exilesItselfAndShufflesUpToFiveCardsFromOwnGraveyard() {
        Permanent wand = addReadyWand(player1);
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card libraryCard = new Forest();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(libraryCard));
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(first.getId(), second.getId()));
        assertThat(wand.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore + 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wand.getCard());
    }

    @Test
    void cannotTargetCardsInOpponentsGraveyard() {
        addReadyWand(player1);
        Card opponentCard = new Forest();
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(opponentCard.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyWand(Player player) {
        Permanent wand = harness.addToBattlefieldAndReturn(player, new WandOfVertebrae());
        wand.setSummoningSick(false);
        return wand;
    }
}
