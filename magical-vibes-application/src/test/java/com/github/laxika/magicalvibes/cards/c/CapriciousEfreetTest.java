package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.d.DarksteelColossus;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
import com.github.laxika.magicalvibes.cards.k.KalonianBehemoth;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CapriciousEfreet.class, RuneclawBear.class, CanyonMinotaur.class, Mountain.class, KalonianBehemoth.class, DarksteelColossus.class, RodOfRuin.class})
class CapriciousEfreetTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger presents own nonland permanent selection")
    void upkeepTriggerPresentsOwnTargetSelection() {
        addReadyEfreet(player1);
        addCreatureReady(player1, new RuneclawBear());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
    }

    @Test
    @DisplayName("Efreet can target itself as own nonland permanent")
    void efreetCanTargetItself() {
        Permanent efreet = addReadyEfreet(player1);
        // No other nonland permanents — only the Efreet itself is a valid own target

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Choose the Efreet itself as own target
        harness.handlePermanentChosen(player1, efreet.getId());

        // No opponent nonland permanents, so ability goes directly to stack
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetIds()).containsExactly(efreet.getId());
    }

    @Test
    @DisplayName("After own target, presents opponent nonland permanent selection")
    void afterOwnTargetPresentsOpponentSelection() {
        addReadyEfreet(player1);
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player2, new CanyonMinotaur());

        advanceToUpkeep(player1);

        // Step 1: choose own target
        harness.clearMessages();
        harness.handlePermanentChosen(player1, bears.getId());

        // Step 2: multi-permanent choice for opponent targets
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        assertThat(harness.getConn1().getMessagesContaining("\"type\":\"INTERACTION_PROMPT\""))
                .hasSize(1);
        assertThat(harness.getConn1().getMessagesContaining("\"type\":\"GAME_STATE\"")).isEmpty();
        assertThat(harness.getConn2().getMessagesContaining("\"type\":\"GAME_STATE\"")).isEmpty();
    }

    @Test
    @DisplayName("Can choose zero opponent targets (skipping optional targets)")
    void canChooseZeroOpponentTargets() {
        addReadyEfreet(player1);
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        addCreatureReady(player2, new CanyonMinotaur());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.clearMessages();
        harness.handleMultiplePermanentsChosen(player1, List.of()); // zero opponents

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetIds())
                .containsExactly(bears.getId());
        assertThat(harness.getConn1().getMessagesContaining("\"type\":\"GAME_STATE\"")).hasSize(1);
        assertThat(harness.getConn2().getMessagesContaining("\"type\":\"GAME_STATE\"")).hasSize(1);
    }

    @Test
    @DisplayName("Can choose one opponent target")
    void canChooseOneOpponentTarget() {
        addReadyEfreet(player1);
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        Permanent hillGiant = addCreatureReady(player2, new CanyonMinotaur());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(hillGiant.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetIds())
                .containsExactly(bears.getId(), hillGiant.getId());
        assertThat(gd.gameLog)
                .extracting(GameLogEntry::plainText)
                .contains("Capricious Efreet's ability targets Runeclaw Bear, Canyon Minotaur.");
    }

    @Test
    @DisplayName("Can choose two opponent targets")
    void canChooseTwoOpponentTargets() {
        addReadyEfreet(player1);
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        Permanent hillGiant = addCreatureReady(player2, new CanyonMinotaur());
        Permanent bears2 = addCreatureReady(player2, new RuneclawBear());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(hillGiant.getId(), bears2.getId()));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetIds())
                .containsExactly(bears.getId(), hillGiant.getId(), bears2.getId());
    }

    @Test
    @DisplayName("Resolving with only own target destroys it")
    void resolvingWithOnlyOwnTargetDestroysIt() {
        Permanent efreet = addReadyEfreet(player1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, efreet.getId());

        // No opponent targets → stack entry with just own target
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities(); // resolve

        // Efreet destroyed itself (only target, random pick with 1 element)
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(efreet.getId()));
    }

    @Test
    @DisplayName("Resolving destroys exactly one permanent from the target pool")
    void resolvingDestroysExactlyOnePermanent() {
        addReadyEfreet(player1);
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        Permanent hillGiant = addCreatureReady(player2, new CanyonMinotaur());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(hillGiant.getId()));

        int totalBefore = gd.playerBattlefields.get(player1.getId()).size()
                + gd.playerBattlefields.get(player2.getId()).size();

        harness.passBothPriorities(); // resolve

        int totalAfter = gd.playerBattlefields.get(player1.getId()).size()
                + gd.playerBattlefields.get(player2.getId()).size();

        // Exactly one permanent should have been destroyed
        assertThat(totalBefore - totalAfter).isEqualTo(1);
    }

    @Test
    @DisplayName("If all targets leave before resolution, ability fizzles")
    void abilityFizzlesIfAllTargetsLeave() {
        addReadyEfreet(player1);
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player1.getId()).remove(bears);

        int battlefieldSizeBefore = gd.playerBattlefields.get(player1.getId()).size();
        harness.passBothPriorities(); // resolve — target gone

        // Nothing should be destroyed
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(battlefieldSizeBefore);
    }

    @Test
    @DisplayName("Lands are not valid targets for own permanent selection")
    void landsNotValidOwnTargets() {
        Permanent efreet = addReadyEfreet(player1);
        addReadyLand(player1);

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);

        // The only valid own target should be the Efreet itself (not the land)
        // Choosing the Efreet should work
        harness.handlePermanentChosen(player1, efreet.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Skips opponent target step when opponent has no nonland permanents")
    void skipsOpponentStepWhenNoOpponentNonlands() {
        addReadyEfreet(player1);
        Permanent bears = addCreatureReady(player1, new RuneclawBear());
        addReadyLand(player2); // only a land — no valid opponent targets

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());

        // Should go directly to stack (no multi-permanent choice)
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetIds())
                .containsExactly(bears.getId());
    }

    @Test
    @DisplayName("Stack entry is a triggered ability")
    void stackEntryIsTriggeredAbility() {
        addReadyEfreet(player1);
        Permanent bears = addCreatureReady(player1, new RuneclawBear());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType())
                .isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    void ownShroudedPermanentIsNotOfferedAsTarget() {
        Permanent efreet = addReadyEfreet(player1);
        Permanent behemoth = addCreatureReady(player1, new KalonianBehemoth());

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).contains(efreet.getId()).doesNotContain(behemoth.getId());
        harness.handlePermanentChosen(player1, efreet.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(behemoth);
    }

    @Test
    void opponentShroudedPermanentIsNotOfferedAsTarget() {
        Permanent efreet = addReadyEfreet(player1);
        Permanent behemoth = addCreatureReady(player2, new KalonianBehemoth());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, efreet.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetIds()).containsExactly(efreet.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(behemoth);
    }

    @Test
    void ownTargetThatChangesControllerIsNotDestroyed() {
        Permanent efreet = addReadyEfreet(player1);

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, efreet.getId());
        gd.playerBattlefields.get(player1.getId()).remove(efreet);
        gd.playerBattlefields.get(player2.getId()).add(efreet);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(efreet);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentTargetThatChangesControllerIsNotDestroyed() {
        Permanent efreet = addReadyEfreet(player1);
        Permanent bear = addCreatureReady(player2, new RuneclawBear());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, efreet.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(bear.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(efreet);
        gd.playerBattlefields.get(player2.getId()).remove(bear);
        gd.playerBattlefields.get(player1.getId()).add(bear);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bear);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void remainingLegalOpponentTargetIsDestroyedWhenOwnTargetLeaves() {
        Permanent efreet = addReadyEfreet(player1);
        Permanent bear = addCreatureReady(player2, new RuneclawBear());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, efreet.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(bear.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(efreet);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bear.getCard());
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent efreet = addReadyEfreet(player1);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(efreet);
    }

    @Test
    void indestructibleTargetCanBeChosenAndSurvivesDestruction() {
        addReadyEfreet(player1);
        Permanent colossus = addCreatureReady(player1, new DarksteelColossus());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, colossus.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(colossus);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void noncreatureArtifactCanBeTargetedAndDestroyed() {
        addReadyEfreet(player1);
        Permanent rod = harness.addToBattlefieldAndReturn(player1, new RodOfRuin());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, rod.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(rod);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(rod.getCard());
    }

    private Permanent addReadyEfreet(Player player) {
        return addCreatureReady(player, new CapriciousEfreet());
    }

    private Permanent addReadyLand(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Mountain());
    }
}
