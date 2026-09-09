package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.j.JabarisBanner;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinVandal.class, JabarisBanner.class, BenalishInfantry.class})
class GoblinVandalTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent atk = addCreatureReady(player1, new GoblinVandal());
        atk.setAttacking(true);
        return atk;
    }

    private Permanent addDefenderArtifact() {
        return harness.addToBattlefieldAndReturn(player2, new JabarisBanner());
    }

    private void advanceToMayPayPrompt(Permanent target) {
        prepareDeclareBlockers();
        // Defender declares no blocks, so Goblin Vandal is unblocked and its trigger fires.
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Paying {R} destroys the chosen artifact and prevents the Vandal's combat damage")
    void payDestroysArtifactAndPreventsDamage() {
        Permanent millstone = addDefenderArtifact();
        Permanent attacker = addAttacker();
        int defenderLifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.addMana(player1, ManaColor.RED, 1);

        advanceToMayPayPrompt(millstone);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(millstone);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defenderLifeBefore);
    }

    @Test
    @DisplayName("Declining to pay leaves the artifact alone and the Vandal deals combat damage")
    void declineDoesNothing() {
        Permanent millstone = addDefenderArtifact();
        Permanent attacker = addAttacker();
        int defenderLifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.addMana(player1, ManaColor.RED, 1);

        advanceToMayPayPrompt(millstone);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(millstone);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defenderLifeBefore - 1);
    }

    @Test
    @DisplayName("Only artifacts the defending player controls can be destroyed")
    void nonArtifactsAreNotOffered() {
        // The defending player controls a creature but no artifact.
        harness.addToBattlefieldAndReturn(player2, new BenalishInfantry());
        Permanent attacker = addAttacker();
        harness.addMana(player1, ManaColor.RED, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("An artifact controlled by the attacker is not a legal target")
    void ownArtifactIsNotOffered() {
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new JabarisBanner());
        Permanent defender = harness.addToBattlefieldAndReturn(player2, new BenalishInfantry());
        Permanent attacker = addAttacker();
        harness.addMana(player1, ManaColor.RED, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(defender);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Choosing one of several artifacts destroys only that artifact")
    void destroysOnlyChosenArtifact() {
        Permanent chosenArtifact = addDefenderArtifact();
        Permanent otherArtifact = addDefenderArtifact();
        Permanent attacker = addAttacker();
        harness.addMana(player1, ManaColor.RED, 1);

        advanceToMayPayPrompt(chosenArtifact);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(chosenArtifact);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(otherArtifact);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
    }

    @Test
    @DisplayName("An artifact entering after target selection is not retargeted")
    void targetDoesNotChangeAfterTargetSelection() {
        Permanent originalArtifact = addDefenderArtifact();
        Permanent attacker = addAttacker();
        harness.addMana(player1, ManaColor.RED, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, originalArtifact.getId());

        gd.playerBattlefields.get(player2.getId()).remove(originalArtifact);
        Permanent replacementArtifact = addDefenderArtifact();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(replacementArtifact);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("A blocked attacker does not trigger the ability")
    void blockedNoTrigger() {
        addCreatureReady(player2, new BenalishInfantry());
        addAttacker();
        harness.addMana(player1, ManaColor.RED, 1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
