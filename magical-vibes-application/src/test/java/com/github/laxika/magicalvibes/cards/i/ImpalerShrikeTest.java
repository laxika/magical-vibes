package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndDrawCardsEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ImpalerShrikeTest extends BaseCardTest {

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
    @DisplayName("Impaler Shrike has MayEffect-wrapped combat damage trigger")
    void hasCorrectEffect() {
        ImpalerShrike card = new ImpalerShrike();

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst();
        assertThat(may.wrapped()).isInstanceOf(SacrificeSelfAndDrawCardsEffect.class);
        assertThat(((SacrificeSelfAndDrawCardsEffect) may.wrapped()).amount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Combat damage trigger presents may ability choice")
    void combatDamageTriggerPresentsMayChoice() {
        Permanent shrike = addReadyCreature(player1, new ImpalerShrike());
        shrike.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting may sacrifices Impaler Shrike and draws 3 cards")
    void sacrificeSelfAndDrawCards() {
        Permanent shrike = addReadyCreature(player1, new ImpalerShrike());
        shrike.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);
        // Resolve the triggered ability from the stack
        harness.passBothPriorities();

        // Impaler Shrike should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Impaler Shrike"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Impaler Shrike"));

        // Controller should have drawn 3 cards
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    @DisplayName("Declining the may ability keeps Impaler Shrike alive and draws no cards")
    void declineSacrifice() {
        Permanent shrike = addReadyCreature(player1, new ImpalerShrike());
        shrike.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.handleMayAbilityChosen(player1, false);

        // Impaler Shrike should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Impaler Shrike"));

        // No cards drawn
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("declines"));
    }

    @Test
    @DisplayName("No trigger when Impaler Shrike is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent shrike = addReadyCreature(player1, new ImpalerShrike());
        shrike.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Defender takes combat damage regardless of sacrifice choice")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        Permanent shrike = addReadyCreature(player1, new ImpalerShrike());
        shrike.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        harness.handleMayAbilityChosen(player1, false);

        // Impaler Shrike is 3/1, should deal 3 damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }
}
