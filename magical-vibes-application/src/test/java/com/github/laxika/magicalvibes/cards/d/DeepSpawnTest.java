package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DeepSpawnTest extends BaseCardTest {

    @Test
    @DisplayName("Milling two cards keeps Deep Spawn")
    void millingTwoCardsKeepsDeepSpawn() {
        Permanent spawn = addCreatureReady(player1, new DeepSpawn());
        harness.setLibrary(player1, List.of(new Island(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getName())
                .containsExactly("Island", "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining to mill sacrifices Deep Spawn")
    void decliningToMillSacrificesDeepSpawn() {
        addCreatureReady(player1, new DeepSpawn());
        harness.setLibrary(player1, List.of(new Island(), new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Deep Spawn");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Fewer than two cards makes the upkeep cost unavailable")
    void tooFewCardsSacrificesWithoutPrompt() {
        addCreatureReady(player1, new DeepSpawn());
        harness.setLibrary(player1, List.of(new Island()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotOnBattlefield(player1, "Deep Spawn");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Activating grants shroud, taps itself, and skips next untap")
    void activatingGrantsShroudTapsAndSkipsUntap() {
        Permanent spawn = addCreatureReady(player1, new DeepSpawn());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(spawn.getGrantedKeywords()).contains(Keyword.SHROUD);
        assertThat(spawn.isTapped()).isTrue();
        assertThat(spawn.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Shroud wears off at end of turn")
    void shroudWearsOffAtEndOfTurn() {
        Permanent spawn = addCreatureReady(player1, new DeepSpawn());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(spawn.getGrantedKeywords()).contains(Keyword.SHROUD);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(spawn.getGrantedKeywords()).doesNotContain(Keyword.SHROUD);
    }
}
