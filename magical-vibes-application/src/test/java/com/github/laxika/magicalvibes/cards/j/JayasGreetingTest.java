package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JayasGreeting.class, AirElemental.class, Mountain.class})
class JayasGreetingTest extends BaseCardTest {

    @Test
    void dealsThreeDamageAndScriesOne() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop = deck.get(0);

        castGreeting(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        harness.getGameService().handleInteractionAnswer(
                gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(deck.get(deck.size() - 1)).isSameAs(originalTop);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Jaya's Greeting");
    }

    @Test
    void cannotTargetALand() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player2, new Mountain());
        harness.setHand(player1, List.of(new JayasGreeting()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetOwnCreatureAndKeepScryedCardOnTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        List<Card> deck = gd.playerDecks.get(player1.getId());
        List<Card> originalDeck = List.copyOf(deck);
        List<Card> opponentDeck = List.copyOf(gd.playerDecks.get(player2.getId()));

        castGreeting(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(deck).containsExactlyElementsOf(originalDeck);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentDeck);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Jaya's Greeting");
    }

    @Test
    void doesNotScryWhenTargetLeavesBeforeResolution() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        List<Card> originalDeck = List.copyOf(gd.playerDecks.get(player1.getId()));
        harness.setHand(player1, List.of(new JayasGreeting()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.setGraveyard(player2, List.of(target.getCard()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(originalDeck);
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Jaya's Greeting");
    }

    @Test
    void stillScriesWhenDamageIsLethal() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        target.setMarkedDamage(1);

        castGreeting(target);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        harness.assertNotOnBattlefield(player2, "Air Elemental");
        harness.assertInGraveyard(player2, "Air Elemental");
        harness.assertInGraveyard(player1, "Jaya's Greeting");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void dealsDamageWithAnEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        gd.playerDecks.get(player1.getId()).clear();

        castGreeting(target);

        assertThat(target.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Jaya's Greeting");
    }

    private void castGreeting(Permanent target) {
        harness.setHand(player1, List.of(new JayasGreeting()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());
    }
}
