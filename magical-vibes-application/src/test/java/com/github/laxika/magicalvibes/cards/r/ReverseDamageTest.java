package com.github.laxika.magicalvibes.cards.r;

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

class ReverseDamageTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Reverse Damage prompts for a source choice")
    void resolvingPromptsForSourceChoice() {
        castReverseDamage(player1);
        addReadyGoblin(player2);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Choosing a source records a one-shot life-gain prevention shield")
    void choosingSourceRecordsShield() {
        castReverseDamage(player1);
        Permanent goblin = addReadyGoblin(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        assertThat(gd.playerSourceNextDamageShields)
                .anyMatch(s -> s.playerId().equals(player1.getId())
                        && s.sourceId().equals(goblin.getId())
                        && s.gainLife());
    }

    @Test
    @DisplayName("Prevents the next damage from the chosen source and gains that much life")
    void preventsDamageAndGainsLife() {
        harness.setLife(player1, 20);
        castReverseDamage(player1);
        Permanent goblin = addReadyGoblin(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        goblin.setAttacking(true);
        resolveCombat(player2);

        // 2 damage prevented, 2 life gained
        harness.assertLife(player1, 22);
        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    @Test
    @DisplayName("A different source still deals damage; the shield is untouched")
    void differentSourceStillDealsDamage() {
        harness.setLife(player1, 20);
        castReverseDamage(player1);
        Permanent chosen = addReadyGoblin(player2);
        Permanent other = addReadyGoblin(player2);

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
        castReverseDamage(player1);
        Permanent goblin = addReadyGoblin(player2);

        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, goblin.getId());

        assertThat(gd.playerSourceNextDamageShields).isNotEmpty();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerSourceNextDamageShields).isEmpty();
    }

    private void castReverseDamage(Player player) {
        harness.setHand(player, List.of(new ReverseDamage()));
        harness.addMana(player, ManaColor.WHITE, 3);
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
