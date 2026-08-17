package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DiscoveryDispersalTest extends BaseCardTest {

    @Test
    @DisplayName("Discovery surveils 2, then draws a card")
    void discoverySurveilsThenDraws() {
        Card topCard = new Island();
        Card secondCard = new Forest();
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, secondCard, drawnCard));
        harness.setHand(player1, List.of(new DiscoveryDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("Dispersal returns the opponent's greatest-mana-value nonland and makes them discard")
    void dispersalReturnsGreatestManaValueNonlandThenOpponentDiscards() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player2, List.of(new Forest(), new Island()));
        harness.setHand(player1, List.of(new DiscoveryDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInHand(player2, "Hill Giant");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Dispersal lets the opponent choose among tied greatest mana values")
    void dispersalOpponentChoosesTiedPermanent() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        harness.setHand(player2, List.of(new Forest()));
        harness.setHand(player1, List.of(new DiscoveryDispersal()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player2, second.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(first).doesNotContain(second);
        harness.assertInHand(player2, "Hill Giant");
        harness.assertInGraveyard(player2, "Forest");
    }
}
