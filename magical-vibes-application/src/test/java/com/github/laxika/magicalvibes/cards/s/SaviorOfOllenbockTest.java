package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SaviorOfOllenbock.class, GrizzlyBears.class, HillGiant.class, Unsummon.class})
class SaviorOfOllenbockTest extends BaseCardTest {

    @Test
    @DisplayName("When Savior trains, it exiles a target creature from the battlefield")
    void trainsAndExilesBattlefieldCreature() {
        Permanent savior = addCreatureReady(player1, new SaviorOfOllenbock());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(savior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getCard().getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("When Savior trains, it can exile a creature card from a graveyard")
    void trainsAndExilesGraveyardCreature() {
        Permanent savior = addCreatureReady(player1, new SaviorOfOllenbock());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(new HillGiant()));
        UUID giantId = gd.playerGraveyards.get(player2.getId()).stream()
                .filter(card -> card.getName().equals("Hill Giant"))
                .findFirst().orElseThrow().getId();

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(giantId));
        harness.passBothPriorities();

        assertThat(savior.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertNotInGraveyard(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId())).extracting(Card::getName)
                .containsExactly("Hill Giant");
    }

    @Test
    @DisplayName("Cards exiled by Savior return under their owners' control when it leaves")
    void exiledCardsReturnWhenSaviorLeaves() {
        Permanent savior = addCreatureReady(player1, new SaviorOfOllenbock());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player2, List.of(new HillGiant()));
        UUID giantId = gd.playerGraveyards.get(player2.getId()).getFirst().getId();

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(giantId));
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, savior.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Savior of Ollenbock");
        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }
}
