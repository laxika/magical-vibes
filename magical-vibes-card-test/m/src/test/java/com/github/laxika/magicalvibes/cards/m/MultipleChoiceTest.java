package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SpinedKarok;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MultipleChoice.class, SpinedKarok.class})
class MultipleChoiceTest extends BaseCardTest {

    @Test
    void xOneScriesThenDraws() {
        harness.setHand(player1, List.of(new MultipleChoice()));
        harness.setLibrary(player1, List.of(new SpinedKarok()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
    }

    @Test
    void xTwoLetsChosenPlayerReturnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());
        harness.setHand(player1, List.of(new MultipleChoice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.PermanentChoice playerChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(playerChoice.validPlayerIds()).containsExactlyInAnyOrder(player1.getId(), player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handlePermanentChosen(player2, bears.getId());

        assertThat(findPermanents(player2, "Spined Karok")).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).anyMatch(card -> card.getName().equals("Spined Karok"));
    }

    @Test
    void xThreeCreatesElemental() {
        harness.setHand(player1, List.of(new MultipleChoice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 3);

        Permanent elemental = findPermanent(player1, "Elemental");
        assertThat(elemental.getEffectivePower()).isEqualTo(4);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(4);
        assertThat(elemental.getCard().getColors()).containsExactlyInAnyOrder(CardColor.BLUE, CardColor.RED);
        assertThat(elemental.getCard().getSubtypes()).contains(CardSubtype.ELEMENTAL);
    }

    @Test
    void xFourDoesAllThreeModes() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new SpinedKarok());
        harness.setHand(player1, List.of(new MultipleChoice()));
        harness.setLibrary(player1, List.of(new SpinedKarok()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 4);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.handlePermanentChosen(player2, bears.getId());

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player2, "Spined Karok")).isEmpty();
        assertThat(findPermanents(player1, "Elemental")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void xZeroHasNoEffect() {
        harness.setHand(player1, List.of(new MultipleChoice()));
        harness.setLibrary(player1, List.of(new MultipleChoice()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void xTwoCanDeclineChoosingAPlayer() {
        harness.addToBattlefield(player2, new SpinedKarok());
        harness.setHand(player1, List.of(new MultipleChoice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Spined Karok");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Elemental")).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void xFiveStillDrawsAndCreatesTokenWhenChosenPlayerHasNoCreatures() {
        harness.setHand(player1, List.of(new MultipleChoice()));
        harness.setLibrary(player1, List.of(new MultipleChoice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castAndResolveSorcery(player1, 0, 5);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player2.getId());

        harness.assertInHand(player1, "Multiple Choice");
        assertThat(findPermanents(player1, "Elemental")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void xFourCreatesTokenEvenWhenPlayerChoiceIsDeclined() {
        harness.setHand(player1, List.of(new MultipleChoice()));
        harness.setLibrary(player1, List.of(new MultipleChoice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 4);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Multiple Choice");
        assertThat(findPermanents(player1, "Elemental")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void xTwoCanChooseTheCasterWhoChoosesTheirOwnCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new SpinedKarok());
        harness.setHand(player1, List.of(new MultipleChoice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, bears.getId());

        harness.assertNotOnBattlefield(player1, "Spined Karok");
        harness.assertInHand(player1, "Spined Karok");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void xOneDrawsAfterPuttingTheScriedCardOnTheBottom() {
        SpinedKarok bottomedCard = new SpinedKarok();
        MultipleChoice drawnCard = new MultipleChoice();
        harness.setHand(player1, List.of(new MultipleChoice()));
        harness.setLibrary(player1, List.of(bottomedCard, drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottomedCard);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
