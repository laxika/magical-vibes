package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SoulGuideGryffTest extends BaseCardTest {

    @Test
    @DisplayName("ETB prompts for up to one graveyard target before ability goes on stack")
    void etbPromptsForGraveyardTarget() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));
        castSoulGuideGryff();

        harness.passBothPriorities(); // resolve creature → ETB target prompt

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Soul-Guide Gryff"));
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

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"));
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

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Shock"));
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

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
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
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Soul-Guide Gryff"));
    }

    private void castSoulGuideGryff() {
        harness.setHand(player1, List.of(new SoulGuideGryff()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
    }
}
