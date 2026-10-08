package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ClammyProwler;
import com.github.laxika.magicalvibes.cards.d.DazzlingTheaterPropRoom;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VictorValgavothsSeneschal.class, DazzlingTheaterPropRoom.class, GrizzlyBears.class, ClammyProwler.class})
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

    @Test
    void queuedTriggersCountResolutionsAndReanimatedEnchantmentTriggersWithoutAFourthReward() {
        addVictor();
        Card top = new DazzlingTheaterPropRoom();
        Card second = new DazzlingTheaterPropRoom();
        harness.setLibrary(player1, List.of(top, second));
        Card discarded = new DazzlingTheaterPropRoom();
        Card retained = new DazzlingTheaterPropRoom();
        harness.setHand(player2, List.of(discarded, retained));
        Card reanimated = new ClammyProwler();
        harness.setGraveyard(player1, List.of(reanimated));

        harness.enterBattlefieldAndReturn(player1, new ClammyProwler());
        harness.enterBattlefieldAndReturn(player1, new ClammyProwler());
        harness.enterBattlefieldAndReturn(player1, new ClammyProwler());
        assertThat(gd.stack).hasSize(3);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player2, 0);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class)).isNotNull();
        harness.handleGraveyardCardChosen(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == reanimated);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(reanimated);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top, second);
    }

    @Test
    void opponentEnchantmentDoesNotTriggerEerie() {
        addVictor();

        harness.enterBattlefieldAndReturn(player2, new ClammyProwler());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void thirdResolutionWithNoCreatureInAnyGraveyardFinishesNormally() {
        addVictor();
        harness.setLibrary(player1, List.of());
        harness.setHand(player2, List.of());
        Card noncreature = new DazzlingTheaterPropRoom();
        harness.setGraveyard(player1, List.of(noncreature));
        harness.setGraveyard(player2, List.of());

        for (int i = 0; i < 3; i++) {
            harness.enterBattlefieldAndReturn(player1, new ClammyProwler());
            harness.passBothPriorities();
        }

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(noncreature);
    }

    @Test
    void firstResolutionOnNextTurnSurveilsAgain() {
        addVictor();
        harness.setLibrary(player1, List.of(new ClammyProwler(), new DazzlingTheaterPropRoom()));
        harness.setLibrary(player2, List.of(new ClammyProwler(), new DazzlingTheaterPropRoom()));
        harness.enterBattlefieldAndReturn(player1, new ClammyProwler());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.enterBattlefieldAndReturn(player1, new ClammyProwler());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void surveilCanPutACreatureIntoGraveyardForTheThirdResolution() {
        addVictor();
        Card retained = new DazzlingTheaterPropRoom();
        Card reanimated = new ClammyProwler();
        harness.setLibrary(player1, List.of(retained, reanimated));
        harness.setHand(player2, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());

        harness.enterBattlefieldAndReturn(player1, new ClammyProwler());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(reanimated);

        harness.enterBattlefieldAndReturn(player1, new ClammyProwler());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new ClammyProwler());
        harness.passBothPriorities();
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == reanimated);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
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
