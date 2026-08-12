package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GiantSlugTest extends BaseCardTest {

    private Permanent activateSlug() {
        Permanent slug = harness.addToBattlefieldAndReturn(player1, new GiantSlug());
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        return slug;
    }

    @Test
    @DisplayName("The ability waits for the activator's next upkeep and uses a basic land type choice")
    void waitsForNextUpkeep() {
        Permanent slug = activateSlug();

        assertThat(gqs.hasKeyword(gd, slug, Keyword.ISLANDWALK)).isFalse();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isNull();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");

        harness.handleListChoice(player1, "ISLAND");

        assertThat(gqs.hasKeyword(gd, slug, Keyword.ISLANDWALK)).isTrue();
    }

    @Test
    @DisplayName("The chosen landwalk wears off at the end of the upkeep's turn")
    void landwalkWearsOffAtEndOfTurn() {
        Permanent slug = activateSlug();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "FOREST");

        assertThat(gqs.hasKeyword(gd, slug, Keyword.FORESTWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, slug, Keyword.FORESTWALK)).isFalse();
    }
}
