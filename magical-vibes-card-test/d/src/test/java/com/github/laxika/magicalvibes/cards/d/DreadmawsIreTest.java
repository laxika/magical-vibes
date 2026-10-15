package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DreadmawsIre.class, FountainOfYouth.class, GrizzlyBears.class})
class DreadmawsIreTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts an attacking creature and destroys an artifact controlled by the damaged player")
    void boostsAttackerAndDestroysDefendingPlayersArtifact() {
        Permanent attacker = addAttackingCreature();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        castDreadmawsIre(attacker);

        assertThat(attacker.getEffectivePower()).isEqualTo(4);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(4);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isTrue();

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(artifact.getId());

        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Fountain of Youth");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact);
    }

    @Test
    @DisplayName("The boost, trample, and granted trigger expire at end of turn")
    void effectsExpireAtEndOfTurn() {
        Permanent attacker = addAttackingCreature();

        castDreadmawsIre(attacker);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
    }

    @Test
    @DisplayName("Cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new DreadmawsIre()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The spell does not resolve if its target stops attacking")
    void targetStopsAttackingBeforeResolution() {
        Permanent attacker = addAttackingCreature();
        harness.setHand(player1, List.of(new DreadmawsIre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, attacker.getId());

        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
        assertThat(attacker.hasKeyword(Keyword.TRAMPLE)).isFalse();
        harness.assertInGraveyard(player1, "Dreadmaw's Ire");
    }

    @Test
    @DisplayName("Combat damage with no artifact controlled by the damaged player has no legal target")
    void noArtifactControlledByDamagedPlayer() {
        Permanent attacker = addAttackingCreature();
        Permanent ownArtifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());

        castDreadmawsIre(attacker);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ownArtifact);
    }

    @Test
    @DisplayName("An opponent's attacker gains the ability and its controller chooses the artifact")
    void canTargetOpponentsAttacker() {
        harness.forceActivePlayer(player2);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        Permanent attackersArtifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        castDreadmawsIre(attacker);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(artifact.getId());
        harness.handlePermanentChosen(player2, artifact.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fountain of Youth");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(attackersArtifact);
    }

    @Test
    @DisplayName("An artifact that leaves the damaged player's control becomes an illegal target")
    void artifactChangesControllerBeforeTriggerResolves() {
        Permanent attacker = addAttackingCreature();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        castDreadmawsIre(attacker);
        resolveCombat();
        harness.handlePermanentChosen(player1, artifact.getId());

        gd.playerBattlefields.get(player2.getId()).remove(artifact);
        gd.playerBattlefields.get(player1.getId()).add(artifact);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(artifact);
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card instanceof FountainOfYouth);
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card instanceof FountainOfYouth);
    }

    private Permanent addAttackingCreature() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        return attacker;
    }

    private void castDreadmawsIre(Permanent attacker) {
        harness.setHand(player1, List.of(new DreadmawsIre()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, attacker.getId());
    }
}
