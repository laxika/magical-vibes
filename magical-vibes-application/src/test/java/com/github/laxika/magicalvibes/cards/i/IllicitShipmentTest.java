package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({IllicitShipment.class, Forest.class, GrizzlyBears.class, HillGiant.class, LlanowarElves.class})
class IllicitShipmentTest extends BaseCardTest {

    @Test
    void searchesLibraryForAnyCardAndPutsItIntoHand() {
        Card shipment = new IllicitShipment();
        Card chosenCard = new GrizzlyBears();
        harness.setHand(player1, List.of(shipment));
        harness.setLibrary(player1, List.of(new Forest(), chosenCard));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().canFailToFind()).isFalse();
        assertThat(search.params().reveals()).isFalse();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(search.params().cards().indexOf(chosenCard)));

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(chosenCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(shipment.getId()));
    }

    @Test
    void casualtyCopiesTheSpellAndSacrificesTheChosenCreature() {
        Permanent casualtyCreature = addCreatureReady(player1, new HillGiant());
        Card shipment = new IllicitShipment();
        Card firstChoice = new Forest();
        Card secondChoice = new GrizzlyBears();
        harness.setHand(player1, List.of(shipment));
        harness.setLibrary(player1, List.of(firstChoice, secondChoice));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorceryWithSacrifice(player1, 0, casualtyCreature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        chooseFirstSearchCard(firstChoice);
        harness.passBothPriorities();
        chooseFirstSearchCard(secondChoice);

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(firstChoice.getId(), secondChoice.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card.getId().equals(shipment.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(casualtyCreature.getId()));
    }

    @Test
    void cannotPayCasualtyWithAnUnderpoweredCreature() {
        Permanent casualtyCreature = addCreatureReady(player1, new LlanowarElves());
        harness.setHand(player1, List.of(new IllicitShipment()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        assertThatThrownBy(() -> harness.castSorceryWithSacrifice(player1, 0, casualtyCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("power 3");
    }

    private void chooseFirstSearchCard(Card card) {
        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(search.params().cards().indexOf(card)));
    }
}
