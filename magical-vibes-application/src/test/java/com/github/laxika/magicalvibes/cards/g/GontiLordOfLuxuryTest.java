package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GontiLordOfLuxuryTest extends BaseCardTest {

    private void castGonti(List<Card> opponentLibrary) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player2, opponentLibrary);
        harness.setHand(player1, List.of(new GontiLordOfLuxury()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB exiles one of the target opponent's top four cards face down")
    void etbExilesOneTopCardAndBottomsTheRest() {
        List<Card> library = List.of(new GrizzlyBears(), new LlanowarElves(), new Shock(),
                new LightningBolt(), new GrizzlyBears());
        castGonti(library);

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(
                PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(4);

        Card chosen = search.params().cards().get(2);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(2));

        UUID gontiId = harness.getPermanentId(player1, "Gonti, Lord of Luxury");
        assertThat(gd.getCardsExiledByPermanent(gontiId)).containsExactly(chosen);
        assertThat(gd.exiledCards).filteredOn(e -> e.card().getId().equals(chosen.getId()))
                .allMatch(ExiledCardEntry::faceDown);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(4)
                .containsExactlyInAnyOrder(library.get(0), library.get(1), library.get(3), library.get(4));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("May cast the selected card with any type of mana after Gonti leaves")
    void mayCastSelectedCardAfterGontiLeaves() {
        List<Card> library = List.of(new GrizzlyBears(), new LlanowarElves(), new Shock(),
                new LightningBolt());
        castGonti(library);

        Card chosen = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)
                .params().cards().get(0);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.setHand(player2, List.of(new LightningBolt()));
        harness.addMana(player2, ManaColor.RED, 1);
        UUID gontiId = harness.getPermanentId(player1, "Gonti, Lord of Luxury");
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player2, 0, gontiId);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "Gonti, Lord of Luxury");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castFromExile(player1, chosen.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("ETB cannot target its controller")
    void etbCannotTargetItsController() {
        harness.setHand(player1, List.of(new GontiLordOfLuxury()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
