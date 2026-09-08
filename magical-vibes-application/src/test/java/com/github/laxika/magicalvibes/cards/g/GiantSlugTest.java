package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GiantSlugTest extends BaseCardTest {

    @Test
    @DisplayName("Pays five mana and grants the chosen landwalk at the controller's next upkeep")
    void grantsChosenLandwalkAtNextUpkeep() {
        Permanent slug = addSlug();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, slug, Keyword.FORESTWALK)).isFalse();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, Keyword.FORESTWALK.name());

        assertThat(gqs.hasKeyword(gd, slug, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("The delayed landwalk waits for the controller's upkeep")
    void waitsForControllerUpkeep() {
        addSlug();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.getDelayedActions(com.github.laxika.magicalvibes.model.action.GrantChosenLandwalkAtNextUpkeep.class))
                .hasSize(1);
    }

    @Test
    @DisplayName("Chosen landwalk wears off at end of turn")
    void landwalkWearsOffAtEndOfTurn() {
        Permanent slug = addSlug();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, Keyword.ISLANDWALK.name());

        assertThat(gqs.hasKeyword(gd, slug, Keyword.ISLANDWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, slug, Keyword.ISLANDWALK)).isFalse();
    }

    private Permanent addSlug() {
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        return harness.addToBattlefieldAndReturn(player1, new GiantSlug());
    }
}
