package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CampusGuideTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability offers basic lands and puts the choice on top")
    void acceptsBasicLandSearch() {
        Card basicLand = new Plains();
        Card nonland = new GrizzlyBears();
        setup(List.of(basicLand, nonland));

        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(basicLand);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(basicLand);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB ability does not search")
    void declinesBasicLandSearch() {
        Card basicLand = new Plains();
        Card nonland = new GrizzlyBears();
        setup(List.of(basicLand, nonland));

        resolveEtbMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(basicLand, nonland);
    }

    private void setup(List<Card> library) {
        harness.setHand(player1, List.of(new CampusGuide()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.getGameData().playerDecks.get(player1.getId()).clear();
        harness.getGameData().playerDecks.get(player1.getId()).addAll(library);
        harness.castCreature(player1, 0);
    }

    private void resolveEtbMayPrompt() {
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
