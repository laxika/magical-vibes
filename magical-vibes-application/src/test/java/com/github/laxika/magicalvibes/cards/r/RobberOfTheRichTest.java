package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RobberOfTheRich.class, Divination.class, Island.class})
class RobberOfTheRichTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with the larger-hand opponent exiles their top card and permits casting it with any color")
    void attacksExileAndPermitCastingTopCard() {
        Permanent robber = addCreatureReady(player1, new RobberOfTheRich());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Island(), new Island()));
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        ExiledCardEntry entry = gd.findExiledCard(topCard.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isFalse();
        assertThat(entry.sourcePermanentId()).isEqualTo(robber.getId());
        assertThat(entry.ownerId()).isEqualTo(player2.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("The attack trigger does not fire when the defending player does not have the larger hand")
    void noExileWhenDefendingPlayerDoesNotHaveMoreCardsInHand() {
        harness.addToBattlefield(player1, new RobberOfTheRich());
        harness.setHand(player1, List.of(new Island()));
        harness.setHand(player2, List.of(new Island()));
        Card topCard = new Divination();
        harness.setLibrary(player2, List.of(topCard));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.playerLibraries.get(player2.getId())).contains(topCard);
    }
}
