package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PerpetualTimepieceTest extends BaseCardTest {

    @Test
    void millsTwoCards() {
        addTimepiece();
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card third = new AirElemental();
        harness.setLibrary(player1, List.of(first, second, third));

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
    }

    @Test
    void exilesItselfAndShufflesTargetedGraveyardCardsIntoLibrary() {
        Permanent timepiece = addTimepiece();
        Card first = new Forest();
        Card second = new GrizzlyBears();
        Card libraryCard = new AirElemental();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(libraryCard.getId(), first.getId(), second.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(timepiece.getCard());
    }

    @Test
    void canChooseNoGraveyardTargets() {
        Permanent timepiece = addTimepiece();
        Card card = new Forest();
        harness.setGraveyard(player1, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(card);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(timepiece.getCard());
    }

    @Test
    void cannotTargetAnOpponentsGraveyard() {
        addTimepiece();
        Card card = new Forest();
        harness.setGraveyard(player2, List.of(card));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 1, List.of(card.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addTimepiece() {
        return harness.addToBattlefieldAndReturn(player1, new PerpetualTimepiece());
    }
}
