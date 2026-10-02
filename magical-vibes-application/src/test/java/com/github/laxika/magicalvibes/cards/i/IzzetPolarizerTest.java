package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.e.Electrolyze;
import com.github.laxika.magicalvibes.cards.f.FlameJavelin;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IzzetPolarizer.class, Ionize.class, Electrolyze.class, FlameJavelin.class})
class IzzetPolarizerTest extends BaseCardTest {

    @Test
    void deathOffersIonizeOrElectrolyzeAndConjuresTheChoiceIntoHand() {
        harness.addToBattlefield(player1, new IzzetPolarizer());
        harness.setHand(player2, List.of(new FlameJavelin()));
        harness.addMana(player2, ManaColor.RED, 6);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        UUID polarizerId = harness.getPermanentId(player1, "Izzet Polarizer");
        harness.castInstant(player2, 0, polarizerId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.SpellbookCardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.SpellbookCardChoice.class);
        assertThat(choice.cards())
                .extracting(Card::getName)
                .containsExactlyInAnyOrder("Ionize", "Electrolyze");

        Card selectedCard = choice.cards().stream()
                .filter(card -> card.getName().equals("Electrolyze"))
                .findFirst()
                .orElseThrow();
        harness.handleMultipleCardsChosen(player1, List.of(selectedCard.getId()));

        harness.assertInGraveyard(player1, "Izzet Polarizer");
        harness.assertInHand(player1, "Electrolyze");
    }
}
