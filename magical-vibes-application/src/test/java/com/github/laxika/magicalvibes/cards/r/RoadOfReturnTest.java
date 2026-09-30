package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.EdgarMarkov;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoadOfReturn.class, GrizzlyBears.class, EdgarMarkov.class})
class RoadOfReturnTest extends BaseCardTest {

    @Test
    void returnsTargetPermanentFromGraveyardToHand() {
        Card permanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(permanent));
        prepareCard(2);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0}, List.of(), null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(permanent.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void commanderModePutsCommanderIntoHand() {
        Card commander = new EdgarMarkov();
        gd.playerCommandZones.get(player1.getId()).add(commander);
        prepareCard(2);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{1}, List.of(), null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(commander);
        assertThat(gd.playerCommandZones.get(player1.getId())).doesNotContain(commander);
    }

    @Test
    void entwineResolvesBothModesAndPaysAdditionalMana() {
        Card permanent = new GrizzlyBears();
        Card commander = new EdgarMarkov();
        harness.setGraveyard(player1, List.of(permanent));
        gd.playerCommandZones.get(player1.getId()).add(commander);
        prepareCard(2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0, 1}, List.of(), null);
        harness.handleMultipleCardsChosen(player1, List.of(permanent.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player1.getId())).contains(commander);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void graveyardModeExcludesNonPermanentCards() {
        Card nonPermanent = new RoadOfReturn();
        Card permanent = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonPermanent, permanent));
        prepareCard(2);

        harness.castModalSorceryWithModes(player1, 0, 1, 2, new int[]{0}, List.of(), null);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(permanent.getId());
    }

    @Test
    void entwineRequiresAdditionalMana() {
        harness.setHand(player1, List.of(new RoadOfReturn()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.castModalSorceryWithModes(
                player1, 0, 1, 2, new int[]{0, 1}, List.of(), null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareCard(int greenMana) {
        harness.setHand(player1, List.of(new RoadOfReturn()));
        harness.addMana(player1, ManaColor.GREEN, greenMana);
    }
}
