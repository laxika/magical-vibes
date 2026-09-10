package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VictorValgavothsSeneschal.class, DazzlingTheaterPropRoom.class, GrizzlyBears.class})
class VictorValgavothsSeneschalTest extends BaseCardTest {

    @Test
    void eerieProgressesThroughSurveilDiscardAndReanimation() {
        addVictor();
        Card topCard = testCard("Top card", CardType.INSTANT);
        Card secondCard = testCard("Second card", CardType.SORCERY);
        harness.setLibrary(player1, List.of(topCard, secondCard));
        Card discarded = testCard("Discarded card", CardType.INSTANT);
        harness.setHand(player2, new ArrayList<>(List.of(discarded)));
        Card reanimated = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(reanimated)));
        harness.setHand(player1, List.of(testEnchantment(), testEnchantment(), testEnchantment()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        castEnchantmentAndResolveSpell();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(
                List.of(0, 1), List.of()));

        castEnchantmentAndResolveSpell();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);

        castEnchantmentAndResolveSpell();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(
                permanent -> permanent.getCard() == reanimated);
        assertThat(gd.playerGraveyards.get(player2.getId())).doesNotContain(reanimated);
    }

    @Test
    void fullyUnlockingARoomTriggersEerie() {
        addVictor();
        harness.setHand(player1, List.of(new DazzlingTheaterPropRoom()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Card discarded = testCard("Discarded card", CardType.INSTANT);
        harness.setHand(player2, new ArrayList<>(List.of(discarded)));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.unlockRoomDoor(player1, 1, 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
    }

    private Permanent addVictor() {
        return harness.addToBattlefieldAndReturn(player1, new VictorValgavothsSeneschal());
    }

    private void castEnchantmentAndResolveSpell() {
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private Card testEnchantment() {
        return testCard("Test enchantment", CardType.ENCHANTMENT);
    }

    private Card testCard(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        card.setManaCost("{1}");
        return card;
    }
}
