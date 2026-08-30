package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EdgeOfAutumn.class, Forest.class, Plains.class, GrizzlyBears.class})
class EdgeOfAutumnTest extends BaseCardTest {

    @Test
    @DisplayName("Cycling sacrifices a land, searches for a tapped basic land, and draws")
    void cyclingSacrificesLandSearchesAndDraws() {
        Permanent sacrificed = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new EdgeOfAutumn()));
        harness.setLibrary(player1, List.of(new Plains(), new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Plains") && permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sacrificed);
        harness.assertInGraveyard(player1, "Edge of Autumn");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling prompts when more than one land can be sacrificed")
    void cyclingPromptsForSacrificeChoice() {
        List<Permanent> lands = addForests(5);
        harness.setHand(player1, List.of(new EdgeOfAutumn()));
        harness.setLibrary(player1, List.of(new Plains()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, lands.getFirst().getId());
        harness.passBothPriorities();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Plains") && permanent.isTapped());
        harness.assertInGraveyard(player1, "Edge of Autumn");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Cycling skips the search after sacrificing a fifth land and still draws")
    void cyclingSkipsSearchWhenStillControllingMoreThanFourLands() {
        List<Permanent> lands = addForests(6);
        harness.setHand(player1, List.of(new EdgeOfAutumn()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addCyclingMana();

        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, lands.getFirst().getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertInGraveyard(player1, "Edge of Autumn");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Cycling cannot be activated without a land to sacrifice")
    void cyclingRequiresLandToSacrifice() {
        harness.setHand(player1, List.of(new EdgeOfAutumn()));
        addCyclingMana();

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addCyclingMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private List<Permanent> addForests(int count) {
        List<Permanent> lands = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            lands.add(harness.addToBattlefieldAndReturn(player1, new Forest()));
        }
        return lands;
    }
}
