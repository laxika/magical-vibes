package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrotesqueDemise;
import com.github.laxika.magicalvibes.cards.k.KayasWrath;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SyndicateMessenger.class, KayasWrath.class, GrotesqueDemise.class})
class SyndicateMessengerTest extends BaseCardTest {

    @Test
    @DisplayName("Afterlife 1 creates a 1/1 white and black Spirit token with flying")
    void afterlifeCreatesSpiritToken() {
        harness.addToBattlefield(player1, new SyndicateMessenger());

        harness.castFromHand(player1, new KayasWrath(), "{W}{W}{B}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Syndicate Messenger");

        List<Permanent> tokens = findPermanents(player1, "Spirit");
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SPIRIT);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
    }

    @Test
    @DisplayName("Afterlife waits for its trigger to resolve after the creature dies")
    void afterlifeUsesTheStack() {
        harness.addToBattlefield(player1, new SyndicateMessenger());
        harness.castFromHand(player1, new KayasWrath(), "{W}{W}{B}{B}");

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Syndicate Messenger");
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("Simultaneous deaths create one Spirit for each Messenger's controller")
    void simultaneousDeathsCreateTokensForBothControllers() {
        harness.addToBattlefield(player1, new SyndicateMessenger());
        harness.addToBattlefield(player1, new SyndicateMessenger());
        harness.addToBattlefield(player2, new SyndicateMessenger());
        harness.castFromHand(player1, new KayasWrath(), "{W}{W}{B}{B}");

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Syndicate Messenger")).isEmpty();
        assertThat(findPermanents(player2, "Syndicate Messenger")).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).hasSize(2);
        assertThat(findPermanents(player2, "Spirit")).hasSize(1);
    }

    @Test
    @DisplayName("Exiling the Messenger does not trigger afterlife")
    void exileDoesNotCreateToken() {
        harness.addToBattlefield(player1, new SyndicateMessenger());
        Permanent messenger = findPermanent(player1, "Syndicate Messenger");
        harness.setHand(player1, List.of(new GrotesqueDemise()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castInstant(player1, 0, messenger.getId());

        resolveAllTriggers();

        assertThat(gd.exiledCards)
                .anyMatch(entry -> entry.card().getId().equals(messenger.getCard().getId()));
        assertThat(findPermanents(player1, "Syndicate Messenger")).isEmpty();
        assertThat(findPermanents(player1, "Spirit")).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(card -> card.getName()).doesNotContain("Syndicate Messenger");
    }
}
