package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantSlug.class, Island.class, GrizzlyBears.class})
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

    @Test
    @DisplayName("The chosen landwalk prevents blocking while the defending player controls that land type")
    void chosenLandwalkPreventsBlockingAgainstMatchingLand() {
        Permanent slug = activateSlugAndChoose("ISLAND");
        assertThat(gqs.hasKeyword(gd, slug, Keyword.ISLANDWALK)).isTrue();

        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        assertThat(gqs.effectiveBasicLandTypes(gd, island)).contains(CardSubtype.ISLAND);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        slug.setSummoningSick(false);
        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(slug)));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(slug)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("The chosen landwalk does not prevent blocking without that land type")
    void chosenLandwalkAllowsBlockingAgainstDifferentLand() {
        Permanent slug = activateSlugAndChoose("FOREST");
        harness.addToBattlefield(player2, new Island());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        slug.setSummoningSick(false);
        declareAttackersAndPrepareBlockers(List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(slug)));

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(slug))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent activateSlugAndChoose(String landType) {
        Permanent slug = activateSlug();

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, landType);

        return slug;
    }
}
