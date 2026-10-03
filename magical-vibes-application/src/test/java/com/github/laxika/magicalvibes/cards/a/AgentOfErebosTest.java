package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AgentOfErebos.class, Forest.class, GloriousAnthem.class, GrizzlyBears.class})
class AgentOfErebosTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield exiles a target player's graveyard")
    void ownEntryExilesTargetPlayersGraveyard() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Forest()));
        castAgentOfErebos();

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Another enchantment entering under your control exiles a target player's graveyard")
    void anotherEnchantmentEntryExilesTargetPlayersGraveyard() {
        harness.addToBattlefield(player1, new AgentOfErebos());
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Forest()));
        harness.castFromHand(player1, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An enchantment entering under an opponent's control does not trigger it")
    void opponentEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new AgentOfErebos());
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new Forest()));
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GloriousAnthem(), "{1}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Its own entry triggers exactly once and can target its controller")
    void ownEntryCanExileControllersGraveyardExactlyOnce() {
        Forest ownCard = new Forest();
        GrizzlyBears opponentCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        castAgentOfErebos();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A player with an empty graveyard remains a legal target")
    void canTargetEmptyGraveyard() {
        harness.setGraveyard(player2, List.of());
        castAgentOfErebos();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The ability exiles cards added to the graveyard before it resolves")
    void exilesGraveyardContentsAtResolution() {
        Forest originalCard = new Forest();
        GrizzlyBears laterCard = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(originalCard));
        castAgentOfErebos();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setGraveyard(player2, List.of(originalCard, laterCard));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(originalCard, laterCard);
    }

    @Test
    @DisplayName("A nonenchantment creature entering under your control does not trigger it")
    void nonEnchantmentEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new AgentOfErebos());
        Forest graveyardCard = new Forest();
        harness.setGraveyard(player2, List.of(graveyardCard));
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A second Agent triggers both itself and the Agent already on the battlefield")
    void enchantmentCreatureEntryTriggersBothAgents() {
        harness.addToBattlefield(player1, new AgentOfErebos());
        Forest ownCard = new Forest();
        GrizzlyBears opponentCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        castAgentOfErebos();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(ownCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.stack).isEmpty();
    }

    private void castAgentOfErebos() {
        harness.castFromHand(player1, new AgentOfErebos(), "{3}{B}");
    }
}
