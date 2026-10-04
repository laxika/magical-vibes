package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GatherTheWhiteLotus.class, Plains.class})
class GatherTheWhiteLotusTest extends BaseCardTest {

    @Test
    void createsOneAllyForEachPlainsAndStartsScryTwo() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new GatherTheWhiteLotus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Ally")).hasSize(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    void scriesWithoutCreatingTokensWhenOnlyOpponentControlsPlains() {
        harness.addToBattlefield(player2, new Plains());
        Plains first = new Plains();
        Plains second = new Plains();
        Plains third = new Plains();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new GatherTheWhiteLotus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Ally")).isEmpty();
        assertThat(findPermanents(player2, "Ally")).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        harness.assertInGraveyard(player1, "Gather the White Lotus");
    }

    @Test
    void countsPlainsAtResolutionAndStillCreatesTokensWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new GatherTheWhiteLotus()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, 0);
        harness.addToBattlefield(player1, new Plains());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Ally")).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Gather the White Lotus");
    }
}
