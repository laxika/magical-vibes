package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OathOfScholarsTest extends BaseCardTest {

    @Test
    void activePlayerMayDiscardTheirHandAndDrawThreeCards() {
        harness.addToBattlefield(player1, new OathOfScholars());
        harness.setHand(player1, List.of(new Forest(), new Island(), new Mountain()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        setLibrary(player2);

        advanceToUpkeep(player2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player1.getId());
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void decliningLeavesTheActivePlayersHandUntouched() {
        harness.addToBattlefield(player1, new OathOfScholars());
        harness.setHand(player1, List.of(new Forest(), new Island()));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        setLibrary(player2);

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void targetMustStillHaveMoreCardsWhenTheAbilityResolves() {
        harness.addToBattlefield(player1, new OathOfScholars());
        harness.setHand(player1, List.of(new Forest(), new Island()));
        harness.setHand(player2, List.of(new GrizzlyBears()));

        advanceToUpkeep(player2);
        harness.handlePermanentChosen(player2, player1.getId());
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private void setLibrary(com.github.laxika.magicalvibes.model.Player player) {
        gd.playerDecks.put(player.getId(), new ArrayList<>(List.of(new Forest(), new Island(), new Mountain())));
    }
}
