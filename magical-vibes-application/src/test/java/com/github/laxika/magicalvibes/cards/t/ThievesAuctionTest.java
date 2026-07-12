package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ThievesAuctionTest extends BaseCardTest {

    private void cast(com.github.laxika.magicalvibes.model.Player caster) {
        harness.setHand(caster, List.of(new ThievesAuction()));
        harness.addMana(caster, ManaColor.RED, 7);
        harness.castSorcery(caster, 0, 0);
        harness.passBothPriorities();
    }

    private UUID poolCardId(String name) {
        return activeAuction().pool().stream()
                .filter(c -> c.getName().equals(name))
                .map(Card::getId)
                .findFirst()
                .orElseThrow();
    }

    private PendingInteraction.PermanentAuctionChoice activeAuction() {
        return gd.interaction.activeInteraction(PendingInteraction.PermanentAuctionChoice.class);
    }

    @Test
    @DisplayName("Exiles all nontoken permanents and prompts the controller first")
    void exilesAllAndPromptsController() {
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new RodOfRuin());

        cast(player1);

        PendingInteraction.PermanentAuctionChoice auction = activeAuction();
        assertThat(auction).isNotNull();
        assertThat(auction.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(auction.pool()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Raging Goblin", "Plains", "Rod of Ruin");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Players pick in turn order and cards enter tapped under the chooser's control")
    void picksRotateAndEnterTapped() {
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new RodOfRuin());

        cast(player1);

        // Controller picks first.
        harness.handleMultipleCardsChosen(player1, List.of(poolCardId("Raging Goblin")));

        // Then the other player.
        assertThat(activeAuction().choosingPlayerId()).isEqualTo(player2.getId());
        assertThat(activeAuction().pool()).hasSize(2);
        harness.handleMultipleCardsChosen(player2, List.of(poolCardId("Rod of Ruin")));

        // Back to the controller for the last card.
        assertThat(activeAuction().choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(activeAuction().pool()).hasSize(1);
        harness.handleMultipleCardsChosen(player1, List.of(poolCardId("Plains")));

        assertThat(gd.interaction.activeInteraction()).isNull();

        List<Permanent> p1 = gd.playerBattlefields.get(player1.getId());
        List<Permanent> p2 = gd.playerBattlefields.get(player2.getId());
        assertThat(p1).extracting(p -> p.getCard().getName())
                .containsExactlyInAnyOrder("Raging Goblin", "Plains");
        assertThat(p2).extracting(p -> p.getCard().getName()).containsExactly("Rod of Ruin");
        assertThat(p1).allMatch(Permanent::isTapped);
        assertThat(p2).allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("A player may claim a card an opponent controlled, changing its controller")
    void claimingOpponentCardChangesControl() {
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player2, new RodOfRuin());

        cast(player1);

        // Controller grabs the opponent's Rod of Ruin.
        harness.handleMultipleCardsChosen(player1, List.of(poolCardId("Rod of Ruin")));
        // Opponent is left with the Raging Goblin.
        harness.handleMultipleCardsChosen(player2, List.of(poolCardId("Raging Goblin")));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getName())
                .containsExactly("Rod of Ruin");
        assertThat(gd.playerBattlefields.get(player2.getId())).extracting(p -> p.getCard().getName())
                .containsExactly("Raging Goblin");
    }

    @Test
    @DisplayName("Token permanents are not exiled and stay on the battlefield")
    void tokensAreNotExiled() {
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, tokenCreature());
        gd.playerBattlefields.get(player2.getId()).clear();

        cast(player1);

        assertThat(activeAuction().pool()).extracting(Card::getName).containsExactly("Raging Goblin");
        // Token stayed put during the auction.
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getName())
                .containsExactly("Goblin Token");

        harness.handleMultipleCardsChosen(player1, List.of(poolCardId("Raging Goblin")));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).extracting(p -> p.getCard().getName())
                .containsExactlyInAnyOrder("Raging Goblin", "Goblin Token");
    }

    @Test
    @DisplayName("An invalid pick re-prompts the same player without advancing")
    void invalidPickReprompts() {
        harness.addToBattlefield(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new GloriousAnthem());

        cast(player1);

        harness.handleMultipleCardsChosen(player1, List.of(UUID.randomUUID()));

        PendingInteraction.PermanentAuctionChoice auction = activeAuction();
        assertThat(auction).isNotNull();
        assertThat(auction.choosingPlayerId()).isEqualTo(player1.getId());
        assertThat(auction.pool()).hasSize(2);
    }

    private Card tokenCreature() {
        Card token = new Card();
        token.setName("Goblin Token");
        token.setType(CardType.CREATURE);
        token.setManaCost("");
        token.setColor(CardColor.RED);
        token.setPower(1);
        token.setToughness(1);
        token.setToken(true);
        return token;
    }
}
