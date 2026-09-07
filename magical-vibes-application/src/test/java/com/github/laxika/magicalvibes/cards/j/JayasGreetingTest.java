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

    private void castGreeting(Permanent target) {
        harness.setHand(player1, List.of(new JayasGreeting()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
