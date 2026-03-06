package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndDealDamageToDamagedPlayerEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FurnaceScampTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Furnace Scamp has MayEffect-wrapped combat damage trigger")
    void hasCorrectEffect() {
        FurnaceScamp card = new FurnaceScamp();

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst();
        assertThat(may.wrapped()).isInstanceOf(SacrificeSelfAndDealDamageToDamagedPlayerEffect.class);
        assertThat(((SacrificeSelfAndDealDamageToDamagedPlayerEffect) may.wrapped()).damage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Combat damage trigger presents may ability choice")
    void combatDamageTriggerPresentsMayChoice() {
        Permanent scamp = addReadyCreature(player1, new FurnaceScamp());
        scamp.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting may sacrifices Furnace Scamp and deals 3 damage to damaged player")
    void sacrificeSelfAndDealDamage() {
        harness.setLife(player2, 20);
        Permanent scamp = addReadyCreature(player1, new FurnaceScamp());
        scamp.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);
        // Resolve the triggered ability from the stack
        harness.passBothPriorities();

        // Furnace Scamp should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Furnace Scamp"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Furnace Scamp"));

        // Player 2 takes 1 combat damage + 3 trigger damage = 4 total
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Declining the may ability keeps Furnace Scamp alive and deals no extra damage")
    void declineSacrifice() {
        harness.setLife(player2, 20);
        Permanent scamp = addReadyCreature(player1, new FurnaceScamp());
        scamp.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        harness.handleMayAbilityChosen(player1, false);

        // Furnace Scamp should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Furnace Scamp"));

        // Player 2 takes only 1 combat damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("declines"));
    }

    @Test
    @DisplayName("No trigger when Furnace Scamp is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent scamp = addReadyCreature(player1, new FurnaceScamp());
        scamp.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }
}
