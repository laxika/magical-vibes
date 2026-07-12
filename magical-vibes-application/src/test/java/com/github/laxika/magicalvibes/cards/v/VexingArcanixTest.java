package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VexingArcanixTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability prompts the target player to name a card")
    void resolvingPromptsTargetPlayer() {
        addReadyArcanix(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        var interaction = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction.playerId()).isEqualTo(player2.getId());
        assertThat(interaction.context()).isInstanceOf(ChoiceContext.TargetPlayerNameCardRevealTopChoice.class);
    }

    @Test
    @DisplayName("Correct name puts the top card into the target's hand with no damage")
    void correctNameGoesToHand() {
        harness.setLife(player2, 20);
        addReadyArcanix(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card topCard = createNamedCard("Lightning Bolt");
        gd.playerDecks.get(player2.getId()).addFirst(topCard);
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Lightning Bolt");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Wrong name puts the top card into the graveyard and deals 2 damage to the target")
    void wrongNameGoesToGraveyardAndDeals2Damage() {
        harness.setLife(player2, 20);
        addReadyArcanix(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        Card topCard = createNamedCard("Grizzly Bears");
        gd.playerDecks.get(player2.getId()).addFirst(topCard);
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Lightning Bolt");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).noneMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Empty library does not crash and deals no damage")
    void emptyLibraryHandledGracefully() {
        harness.setLife(player2, 20);
        addReadyArcanix(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gd.playerDecks.get(player2.getId()).clear();
        int handBefore = gd.playerHands.get(player2.getId()).size();

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player2, "Lightning Bolt");

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handBefore);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private Permanent addReadyArcanix(Player player) {
        VexingArcanix card = new VexingArcanix();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private static Card createNamedCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("{1}");
        card.setColor(CardColor.RED);
        return card;
    }
}
