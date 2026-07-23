package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IllusionaryPresenceTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    /** Resolve landwalk choice (top), then cumulative upkeep (pay). */
    private void resolveUpkeepChoosing(Permanent presence, Keyword landwalk) {
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities(); // landwalk trigger → keyword choice
        harness.handleListChoice(player1, landwalk.name());

        harness.passBothPriorities(); // cumulative upkeep → may pay
        assertThat(presence.getCounterCount(CounterType.AGE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Illusionary Presence")
    void paysCumulativeUpkeep() {
        Permanent presence = harness.addToBattlefieldAndReturn(player1, new IllusionaryPresence());

        resolveUpkeepChoosing(presence, Keyword.ISLANDWALK);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(presence);
        assertThat(presence.getCounterCount(CounterType.AGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Illusionary Presence")
    void declineSacrifices() {
        Permanent presence = harness.addToBattlefieldAndReturn(player1, new IllusionaryPresence());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLANDWALK");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(presence);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Illusionary Presence"));
    }

    @Test
    @DisplayName("Upkeep grants chosen landwalk until end of turn")
    void upkeepGrantsChosenLandwalk() {
        Permanent presence = harness.addToBattlefieldAndReturn(player1, new IllusionaryPresence());

        resolveUpkeepChoosing(presence, Keyword.FORESTWALK);

        assertThat(gqs.hasKeyword(gd, presence, Keyword.FORESTWALK)).isTrue();
    }

    @Test
    @DisplayName("Granted landwalk wears off at end of turn")
    void landwalkResetsAtEndOfTurn() {
        Permanent presence = harness.addToBattlefieldAndReturn(player1, new IllusionaryPresence());

        resolveUpkeepChoosing(presence, Keyword.ISLANDWALK);
        assertThat(gqs.hasKeyword(gd, presence, Keyword.ISLANDWALK)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, presence, Keyword.ISLANDWALK)).isFalse();
    }
}
