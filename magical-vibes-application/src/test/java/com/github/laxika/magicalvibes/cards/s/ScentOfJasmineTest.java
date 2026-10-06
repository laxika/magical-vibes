package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.v.VoiceOfDuty;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScentOfJasmine.class, VoiceOfDuty.class, SerraAdvocate.class,
        ScentOfCinder.class, ScentOfBrine.class})
class ScentOfJasmineTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life for each white card in hand")
    void gainsLifeForWhiteCardsInHand() {
        VoiceOfDuty firstWhiteCard = new VoiceOfDuty();
        SerraAdvocate secondWhiteCard = new SerraAdvocate();
        harness.setHand(player1, List.of(
                new ScentOfJasmine(),
                firstWhiteCard,
                secondWhiteCard,
                new ScentOfCinder()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.handleMultipleCardsChosen(player1, List.of(firstWhiteCard.getId(), secondWhiteCard.getId()));
        harness.assertLife(player1, 24);
    }

    @Test
    @DisplayName("Gains no life when hand has no white cards")
    void gainsNoLifeWithoutWhiteCards() {
        harness.setHand(player1, List.of(new ScentOfJasmine(), new ScentOfCinder(), new ScentOfBrine()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Gains life only for the white cards chosen to reveal")
    void gainsLifeForChosenWhiteCards() {
        VoiceOfDuty firstWhiteCard = new VoiceOfDuty();
        SerraAdvocate secondWhiteCard = new SerraAdvocate();
        harness.setHand(player1, List.of(
                new ScentOfJasmine(), firstWhiteCard, secondWhiteCard, new ScentOfCinder()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(firstWhiteCard.getId(), secondWhiteCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of(firstWhiteCard.getId()));

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Revealed cards remain in hand and can be revealed for another spell")
    void canRevealTheSameCardAgain() {
        ScentOfJasmine secondSpell = new ScentOfJasmine();
        VoiceOfDuty whiteCard = new VoiceOfDuty();
        harness.setHand(player1, List.of(new ScentOfJasmine(), secondSpell, whiteCard));
        harness.setHand(player2, List.of(new SerraAdvocate()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(secondSpell.getId(), whiteCard.getId());
        harness.handleMultipleCardsChosen(player1, List.of(secondSpell.getId(), whiteCard.getId()));

        harness.assertLife(player1, 24);
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Scent of Jasmine");
        harness.assertInHand(player1, "Voice of Duty");

        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(whiteCard.getId()));

        harness.assertLife(player1, 26);
        harness.assertLife(player2, 20);
        harness.assertInHand(player1, "Voice of Duty");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Can reveal zero white cards")
    void canRevealZeroWhiteCards() {
        VoiceOfDuty whiteCard = new VoiceOfDuty();
        harness.setHand(player1, List.of(new ScentOfJasmine(), whiteCard, new ScentOfCinder()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.RevealAnyNumberOfCardsFromHandChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.RevealAnyNumberOfCardsFromHandChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(whiteCard.getId());

        harness.handleMultipleCardsChosen(player1, List.of());

        harness.assertLife(player1, 20);
    }
}
