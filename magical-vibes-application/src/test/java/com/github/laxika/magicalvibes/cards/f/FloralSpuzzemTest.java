package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.b.BronzeHorse;
import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({FloralSpuzzem.class, BronzeHorse.class, DurkwoodBoars.class})
class FloralSpuzzemTest extends BaseCardTest {

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new FloralSpuzzem());
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addDefenderArtifact() {
        return addCreatureReady(player2, new BronzeHorse());
    }

    private void advanceToUnblockedMay(Permanent target) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Accepting destroys the chosen artifact and prevents combat damage")
    void acceptDestroysArtifactAndPreventsDamage() {
        Permanent artifact = addDefenderArtifact();
        Permanent attacker = addAttacker();
        int defenderLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUnblockedMay(artifact);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defenderLifeBefore);
    }

    @Test
    @DisplayName("Declining leaves the artifact alone and does not prevent combat damage")
    void declineDoesNothing() {
        Permanent artifact = addDefenderArtifact();
        Permanent attacker = addAttacker();
        int defenderLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUnblockedMay(artifact);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defenderLifeBefore - 2);
    }

    @Test
    @DisplayName("An indestructible artifact survives and does not prevent combat damage")
    void indestructibleArtifactDoesNotSatisfyDestroyClause() {
        Permanent artifact = addDefenderArtifact();
        artifact.getGrantedKeywords().add(Keyword.INDESTRUCTIBLE);
        Permanent attacker = addAttacker();
        int defenderLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUnblockedMay(artifact);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defenderLifeBefore - 2);
    }

    @Test
    @DisplayName("Only artifacts controlled by the defending player are eligible")
    void onlyDefendingPlayerArtifactsAreEligible() {
        Permanent attacker = addAttacker();
        Permanent ownArtifact = addCreatureReady(player1, new BronzeHorse());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("A non-artifact permanent controlled by the defending player is not eligible")
    void nonArtifactIsNotEligible() {
        Permanent attacker = addAttacker();
        Permanent defenderCreature = addCreatureReady(player2, new DurkwoodBoars());
        int defenderLifeBefore = gd.playerLifeTotals.get(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(defenderCreature);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(defenderLifeBefore - 2);
    }

    @Test
    @DisplayName("A blocked attacker does not trigger the ability")
    void blockedDoesNotTrigger() {
        Permanent attacker = addAttacker();
        addCreatureReady(player2, new DurkwoodBoars());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).doesNotContain(attacker.getId());
    }
}
