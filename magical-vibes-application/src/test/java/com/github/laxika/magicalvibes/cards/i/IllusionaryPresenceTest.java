package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IllusionaryPresence.class, Island.class, BalduvianBears.class})
class IllusionaryPresenceTest extends BaseCardTest {

    /** Resolve landwalk choice (top), then cumulative upkeep (pay). */
    private void resolveUpkeepChoosing(Permanent presence, Keyword landwalk) {
        resolveUpkeepChoosing(presence, landwalk, 1);
    }

    private void resolveUpkeepChoosing(Permanent presence, Keyword landwalk, int upkeepMana) {
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities(); // landwalk trigger → keyword choice
        harness.handleListChoice(player1, landwalk.name());

        harness.passBothPriorities(); // cumulative upkeep → may pay
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.addMana(player1, ManaColor.BLUE, upkeepMana);
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
        harness.assertInGraveyard(player1, "Illusionary Presence");
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

    @Test
    @DisplayName("Cumulative upkeep costs more for each age counter")
    void cumulativeUpkeepIncreasesWithAgeCounters() {
        Permanent presence = harness.addToBattlefieldAndReturn(player1, new IllusionaryPresence());

        resolveUpkeepChoosing(presence, Keyword.ISLANDWALK);
        assertThat(presence.getCounterCount(CounterType.AGE)).isEqualTo(1);

        resolveUpkeepChoosing(presence, Keyword.FORESTWALK, 2);

        assertThat(presence.getCounterCount(CounterType.AGE)).isEqualTo(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(presence);
    }

    @Test
    @DisplayName("Chosen landwalk prevents blocking when the defending player controls that land type")
    void chosenLandwalkPreventsBlocking() {
        Permanent presence = addCreatureReady(player1, new IllusionaryPresence());
        harness.addToBattlefield(player2, new Island());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        resolveUpkeepChoosing(presence, Keyword.ISLANDWALK);
        declareAttackers(List.of(indexOf(player1, presence)));
        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, presence)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Chosen landwalk allows blocking when the defending player lacks that land type")
    void chosenLandwalkAllowsBlockingWithoutThatLandType() {
        Permanent presence = addCreatureReady(player1, new IllusionaryPresence());
        Permanent blocker = addCreatureReady(player2, new BalduvianBears());

        resolveUpkeepChoosing(presence, Keyword.ISLANDWALK);
        declareAttackers(List.of(indexOf(player1, presence)));
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, presence))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Landwalk choice includes nonbasic land types")
    void offersNonbasicLandTypes() {
        harness.addToBattlefield(player1, new IllusionaryPresence());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("DESERT");
    }

    @Test
    @DisplayName("Landwalk choice includes the Cave land type")
    void offersCaveLandType() {
        harness.addToBattlefield(player1, new IllusionaryPresence());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).contains("CAVE");
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
