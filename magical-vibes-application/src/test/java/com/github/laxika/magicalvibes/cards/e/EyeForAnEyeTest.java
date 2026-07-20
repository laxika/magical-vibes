package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EyeForAnEyeTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Eye for an Eye prompts for a source choice")
    void resolvingPromptsForSourceChoice() {
        castEyeForAnEye(player1);
        addReadyGoblin(player2);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Choosing a source records a one-shot reflection shield")
    void choosingSourceRecordsShield() {
        castEyeForAnEye(player1);
        Permanent goblin = addReadyGoblin(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        assertThat(gd.eyeForAnEyeShields)
                .anyMatch(s -> s.protectedPlayerId().equals(player1.getId())
                        && s.sourceId().equals(goblin.getId()));
    }

    @Test
    @DisplayName("Chosen source's damage still hits you and is reflected at its controller")
    void reflectsDamageToSourceController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castEyeForAnEye(player1);
        Permanent goblin = addReadyGoblin(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);

        // You still take the 2 damage; 2 is also reflected at the goblin's controller.
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 18);
        assertThat(gd.eyeForAnEyeShields).isEmpty();
    }

    @Test
    @DisplayName("A different source deals damage without reflection; the shield is untouched")
    void differentSourceNoReflection() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        castEyeForAnEye(player1);
        Permanent chosen = addReadyGoblin(player2);
        Permanent other = addReadyGoblin(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        assertThat(gd.eyeForAnEyeShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        castEyeForAnEye(player1);
        Permanent goblin = addReadyGoblin(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        assertThat(gd.eyeForAnEyeShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.eyeForAnEyeShields).isEmpty();
    }

    @Test
    @DisplayName("Answering the source choice resumes the parked resolution entry")
    void answeringSourceChoiceClearsParkedResolution() {
        castEyeForAnEye(player1);
        Permanent goblin = addReadyGoblin(player2);

        harness.passBothPriorities();
        assertThat(gd.pendingEffectResolutionEntry).isNotNull();

        harness.handlePermanentChosen(player1, goblin.getId());

        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
    }

    private void castEyeForAnEye(Player player) {
        harness.setHand(player, List.of(new EyeForAnEye()));
        harness.addMana(player, ManaColor.WHITE, 2);
        harness.castInstant(player, 0);
    }

    private Permanent addReadyGoblin(Player player) {
        Permanent perm = new Permanent(new GoblinPiker());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
