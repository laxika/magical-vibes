package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutChargeCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VedalkenInfuserTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Vedalken Infuser has upkeep triggered may ability targeting artifacts")
    void hasCorrectAbilityStructure() {
        VedalkenInfuser card = new VedalkenInfuser();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect mayEffect = (MayEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(mayEffect.wrapped()).isInstanceOf(PutChargeCounterOnTargetPermanentEffect.class);

        assertThat(card.getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
        PermanentPredicateTargetFilter filter = (PermanentPredicateTargetFilter) card.getTargetFilter();
        assertThat(filter.predicate()).isInstanceOf(PermanentIsArtifactPredicate.class);
    }

    // ===== Upkeep triggered ability =====

    @Test
    @DisplayName("Upkeep trigger may put a charge counter on target artifact")
    void upkeepTriggerMayPutChargeCounterOnArtifact() {
        Permanent infuser = addReadyInfuser(player1);
        Permanent artifact = addReadyArtifact(player1);

        triggerUpkeep(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities(); // resolve the effect

        assertThat(artifact.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining the may ability does not put a charge counter")
    void decliningMayAbilityDoesNotPutCounter() {
        Permanent infuser = addReadyInfuser(player1);
        Permanent artifact = addReadyArtifact(player1);

        triggerUpkeep(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(artifact.getChargeCounters()).isEqualTo(0);
    }

    @Test
    @DisplayName("Can target opponent's artifact")
    void canTargetOpponentArtifact() {
        Permanent infuser = addReadyInfuser(player1);
        Permanent opponentArtifact = addReadyArtifact(player2);

        triggerUpkeep(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, opponentArtifact.getId());
        harness.passBothPriorities();

        assertThat(opponentArtifact.getChargeCounters()).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple upkeep triggers accumulate charge counters on same artifact")
    void multipleUpkeepTriggersAccumulateCounters() {
        Permanent infuser = addReadyInfuser(player1);
        Permanent artifact = addReadyArtifact(player1);

        // First upkeep
        triggerUpkeep(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getChargeCounters()).isEqualTo(1);

        // Second upkeep
        triggerUpkeep(player1);
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(artifact.getChargeCounters()).isEqualTo(2);
    }

    @Test
    @DisplayName("May ability is still prompted even when no artifacts exist")
    void mayAbilityPromptedWhenNoArtifacts() {
        Permanent infuser = addReadyInfuser(player1);
        // No artifacts on any battlefield

        triggerUpkeep(player1);

        // The may ability is still prompted (player must choose yes/no)
        assertThat(gd.interaction.isAwaitingInput()).isTrue();

        // Accepting finds no valid targets
        harness.handleMayAbilityChosen(player1, true);

        // No targets available, no stack entry created
        assertThat(gd.stack).isEmpty();
    }

    // ===== Helper methods =====

    private Permanent addReadyInfuser(Player player) {
        VedalkenInfuser card = new VedalkenInfuser();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyArtifact(Player player) {
        LeoninScimitar card = new LeoninScimitar();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void triggerUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
