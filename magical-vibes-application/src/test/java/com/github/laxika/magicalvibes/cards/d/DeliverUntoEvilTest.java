package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.AmbushViper;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.n.NicolBolasGodPharaoh;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeliverUntoEvil.class, AmbushViper.class, Island.class, NicolBolasGodPharaoh.class, Shock.class})
class DeliverUntoEvilTest extends BaseCardTest {

    @Test
    void opponentLeavesTwoTargetsInGraveyardAndReturnsTheRest() {
        Card island = new Island();
        Card shock = new Shock();
        Card viper = new AmbushViper();
        Card fourth = new Island();
        List<Card> targets = List.of(island, shock, viper, fourth);
        harness.setGraveyard(player1, targets);
        cast(targets);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.minCount()).isEqualTo(2);
        assertThat(choice.maxCount()).isEqualTo(2);

        harness.handleMultipleCardsChosen(player2, List.of(shock.getId(), fourth.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactlyInAnyOrder(shock, fourth);
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(island, viper);
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card instanceof DeliverUntoEvil);
    }

    @Test
    void bolasPlaneswalkerReturnsAllTargetsWithoutAnOpponentChoice() {
        Card island = new Island();
        Card shock = new Shock();
        List<Card> targets = List.of(island, shock);
        harness.setGraveyard(player1, targets);
        var bolas = harness.addToBattlefieldAndReturn(player1, new NicolBolasGodPharaoh());
        bolas.setCounterCount(CounterType.LOYALTY, 7);
        cast(targets);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(island, shock);
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card instanceof DeliverUntoEvil);
    }

    @Test
    void canChooseNoTargetsAndStillExilesItself() {
        harness.setGraveyard(player1, List.of());
        cast(List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).anyMatch(card -> card instanceof DeliverUntoEvil);
    }

    private void cast(List<Card> targets) {
        harness.setHand(player1, List.of(new DeliverUntoEvil()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        if (!targets.isEmpty()) {
            PendingInteraction.MultiGraveyardChoice choice =
                    gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
            assertThat(choice.playerId()).isEqualTo(player1.getId());
            harness.handleMultipleCardsChosen(player1, targets.stream().map(Card::getId).toList());
        }
        harness.passBothPriorities();
    }
}
