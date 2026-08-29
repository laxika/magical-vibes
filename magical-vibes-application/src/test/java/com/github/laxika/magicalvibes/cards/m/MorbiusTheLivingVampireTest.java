package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MorbiusTheLivingVampire.class, GrizzlyBears.class, LlanowarElves.class, Shock.class})
class MorbiusTheLivingVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles itself and lets its controller choose one of the top three cards")
    void exilesSelfAndChoosesCardFromTopThree() {
        MorbiusTheLivingVampire morbius = new MorbiusTheLivingVampire();
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(morbius));
        harness.setLibrary(player1, List.of(bears, elves, shock));
        prepareAbility();

        harness.activateGraveyardAbility(player1, 0);
        harness.assertNotInGraveyard(player1, "Morbius the Living Vampire");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(morbius);

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(elves.getId()));

        harness.assertInHand(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactly(bears, shock);
        assertThat(reorder.toBottom()).isTrue();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock, bears);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void prepareAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
    }
}
