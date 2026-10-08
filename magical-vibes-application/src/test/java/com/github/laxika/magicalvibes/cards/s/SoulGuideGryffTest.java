package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SoulGuideGryff.class, GrizzlyBears.class, Shock.class})
class SoulGuideGryffTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts for up to one graveyard target before ability goes on stack")
    void etbPromptsForGraveyardTarget() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        castSoulGuideGryff();

        harness.passBothPriorities(); // resolve creature → ETB target prompt

        harness.assertOnBattlefield(player1, "Soul-Guide Gryff");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount())
                .isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB exiles chosen card from opponent's graveyard")
    void etbExilesOpponentGraveyardCard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        castSoulGuideGryff();

        harness.passBothPriorities(); // resolve creature → target prompt
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities(); // resolve ETB

        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("ETB can exile a noncreature card from controller's graveyard")
    void etbExilesOwnNoncreature() {
        Card shock = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shock)));
        castSoulGuideGryff();

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("ETB can choose zero targets when graveyards are not empty")
    void etbCanChooseZeroTargets() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        castSoulGuideGryff();

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("ETB with empty graveyards skips target prompt and resolves doing nothing")
    void etbWithEmptyGraveyards() {
        castSoulGuideGryff();

        harness.passBothPriorities(); // resolve creature → ETB on stack with 0 targets
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Soul-Guide Gryff");
    }

    @Test
    @DisplayName("ETB does not exile another card when its chosen target leaves the graveyard")
    void etbDoesNotRetargetMissingCard() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears, shock)));
        castSoulGuideGryff();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        harness.setGraveyard(player2, new ArrayList<>(List.of(shock)));
        harness.setHand(player2, List.of(bears));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("ETB exiles only the selected card when both graveyards contain cards")
    void etbExilesOnlySelectedCard() {
        Card bears = new GrizzlyBears();
        Card shock = new Shock();
        harness.setGraveyard(player1, new ArrayList<>(List.of(shock)));
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        castSoulGuideGryff();
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Shock");
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(shock);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void castSoulGuideGryff() {
        harness.castFromHand(player1, new SoulGuideGryff(), "{4}{W}");
    }
}
