package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PentagramOfTheAgesTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability prompts for a source choice")
    void activatingPromptsForSourceChoice() {
        addReadyPentagram(player1);
        addReadyGoblin(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Choosing a source records a one-shot prevention shield with no life gain")
    void choosingSourceRecordsShield() {
        addReadyPentagram(player1);
        Permanent goblin = addReadyGoblin(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId())
                        && s.sourceId().equals(goblin.getId())
                        && !s.gainLife());
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen source without gaining life")
    void preventsDamageWithoutGainingLife() {
        harness.setLife(player1, 20);
        addReadyPentagram(player1);
        Permanent goblin = addReadyGoblin(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);

        // 2 damage prevented, no life gained (would be 22 under Reverse Damage)
        harness.assertLife(player1, 20);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("A different source still deals damage; the shield is untouched")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        addReadyPentagram(player1);
        Permanent chosen = addReadyGoblin(player2);
        Permanent other = addReadyGoblin(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());

        other.setAttacking(true);
        resolveCombat(player2);

        harness.assertLife(player1, 18);
        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.sourceId().equals(chosen.getId()));
    }

    @Test
    @DisplayName("Shield is cleared at end of turn")
    void shieldClearedAtEndOfTurn() {
        addReadyPentagram(player1);
        Permanent goblin = addReadyGoblin(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("Answering the source choice resumes the parked resolution entry")
    void answeringSourceChoiceClearsParkedResolution() {
        addReadyPentagram(player1);
        Permanent goblin = addReadyGoblin(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gd.pendingEffectResolutionEntry).isNotNull();

        harness.handlePermanentChosen(player1, goblin.getId());

        assertThat(gd.pendingEffectResolutionEntry).isNull();
        assertThat(gd.deferPlayerLossCheck).isFalse();
    }

    @Test
    @DisplayName("Lethal damage dealt after the source choice still ends the game")
    void lethalDamageAfterSourceChoiceEndsGame() {
        harness.setLife(player2, 2);
        addReadyPentagram(player1);
        Permanent attacker = addReadyGoblin(player1);
        Permanent source = addReadyGoblin(player2);
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, source.getId());

        attacker.setAttacking(true);
        resolveCombat(player1);

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    private Permanent addReadyPentagram(Player player) {
        Permanent perm = new Permanent(new PentagramOfTheAges());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
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
