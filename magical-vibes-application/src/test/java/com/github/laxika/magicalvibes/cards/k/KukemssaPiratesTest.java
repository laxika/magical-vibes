package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.IronTuskElephant;
import com.github.laxika.magicalvibes.cards.m.ManaPrism;
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

@CardUsed({KukemssaPirates.class, Forest.class, IronTuskElephant.class, ManaPrism.class})
class KukemssaPiratesTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent atk = addCreatureReady(player1, new KukemssaPirates());
        atk.setAttacking(true);
        return atk;
    }

    private Permanent addDefenderArtifact() {
        return harness.addToBattlefieldAndReturn(player2, new ManaPrism());
    }

    private void advanceToMayChoice(Permanent target) {
        prepareDeclareBlockers();
        // Defender declares no blocks, so Kukemssa Pirates is unblocked and its trigger fires.
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting and choosing an artifact gains control of it and prevents combat damage")
    void acceptGainsControlAndPreventsDamage() {
        Permanent artifact = addDefenderArtifact();
        Permanent attacker = addAttacker();
        int defenderLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToMayChoice(artifact);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defenderLifeBefore);
    }

    @Test
    @DisplayName("Control of the artifact is permanent — it survives the source leaving the battlefield")
    void controlIsPermanent() {
        Permanent artifact = addDefenderArtifact();
        Permanent attacker = addAttacker();

        advanceToMayChoice(artifact);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // POSTCOMBAT_MAIN -> END_STEP

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        // The combat-damage prevention itself is only for the turn.
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();
    }

    @Test
    @DisplayName("Declining the may leaves the artifact with its controller and deals combat damage")
    void declineDoesNothing() {
        Permanent artifact = addDefenderArtifact();
        Permanent attacker = addAttacker();
        int defenderLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToMayChoice(artifact);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());

        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defenderLifeBefore - 2);
    }

    @Test
    @DisplayName("A target that leaves before resolution is not replaced")
    void targetLeavesBeforeResolution() {
        Permanent artifact = addDefenderArtifact();
        Permanent attacker = addAttacker();
        int defenderLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToMayChoice(artifact);
        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());

        resolveCombat();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defenderLifeBefore - 2);
    }

    @Test
    @DisplayName("Only artifacts the defending player controls can be taken")
    void nonArtifactsAreNotOffered() {
        Permanent artifact = addDefenderArtifact();
        harness.addToBattlefield(player2, new Forest());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new IronTuskElephant());
        Permanent attacker = addAttacker();

        advanceToMayChoice(artifact);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("A blocked attacker does not trigger the ability")
    void blockedNoTrigger() {
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new IronTuskElephant());
        blocker.setSummoningSick(false);
        addAttacker();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
    }

    @Test
    @DisplayName("Does not retarget an artifact that enters after the trigger")
    void targetDoesNotChangeAfterTrigger() {
        Permanent originalArtifact = addDefenderArtifact();
        Permanent attacker = addAttacker();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, originalArtifact.getId());
        gd.playerBattlefields.get(player2.getId()).remove(originalArtifact);
        Permanent replacementArtifact = addDefenderArtifact();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(replacementArtifact);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(replacementArtifact);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Does not create an ability when the defending player controls no artifact")
    void noArtifactMeansNoTrigger() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new IronTuskElephant());
        addAttacker();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
