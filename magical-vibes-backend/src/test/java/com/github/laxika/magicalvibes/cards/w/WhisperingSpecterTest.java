package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WhisperingSpecterTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, Card card) {
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
    @DisplayName("Has MayEffect-wrapped combat damage trigger with correct effect type")
    void hasCorrectEffect() {
        WhisperingSpecter card = new WhisperingSpecter();

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isInstanceOf(MayEffect.class);
        MayEffect may = (MayEffect) card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst();
        assertThat(may.wrapped()).isInstanceOf(SacrificeSelfAndTargetDiscardsPerPoisonCounterEffect.class);
    }

    @Test
    @DisplayName("Combat damage trigger presents may ability choice")
    void combatDamageTriggerPresentsMayChoice() {
        Permanent specter = addReadyCreature(player1, new WhisperingSpecter());
        specter.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting may sacrifices Whispering Specter and forces discard equal to poison counters")
    void sacrificeSelfAndTargetDiscards() {
        // Give opponent 3 poison counters before combat
        gd.playerPoisonCounters.put(player2.getId(), 3);

        Permanent specter = addReadyCreature(player1, new WhisperingSpecter());
        specter.setAttacking(true);

        // Give player2 enough cards to discard
        harness.setHand(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));

        resolveCombat();

        // Infect deals damage as poison counters, so player2 now has 3 + 1 = 4 poison counters
        int poisonAfterCombat = gd.playerPoisonCounters.getOrDefault(player2.getId(), 0);

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);
        // Resolve the triggered ability from the stack
        harness.passBothPriorities();

        // Whispering Specter should be sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Whispering Specter"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Whispering Specter"));

        // Player2 should be prompted to discard cards equal to their poison counter count
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
    }

    @Test
    @DisplayName("Declining the may ability keeps Whispering Specter alive and no discard")
    void declineSacrifice() {
        gd.playerPoisonCounters.put(player2.getId(), 2);

        Permanent specter = addReadyCreature(player1, new WhisperingSpecter());
        specter.setAttacking(true);

        int handSizeBefore = gd.playerHands.get(player2.getId()).size();

        resolveCombat();

        harness.handleMayAbilityChosen(player1, false);

        // Whispering Specter should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Whispering Specter"));

        // No cards discarded
        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handSizeBefore);

        assertThat(gd.gameLog).anyMatch(log -> log.contains("declines"));
    }

    @Test
    @DisplayName("No trigger when Whispering Specter is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent specter = addReadyCreature(player1, new WhisperingSpecter());
        specter.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Infect combat damage gives poison counter then discard based on total")
    void infectCombatDamageGivesPoisonThenDiscard() {
        // No prior poison counters — infect combat damage will give 1
        Permanent specter = addReadyCreature(player1, new WhisperingSpecter());
        specter.setAttacking(true);
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        resolveCombat();

        // Accept the may ability
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        // Specter is sacrificed
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Whispering Specter"));

        // Player2 has 1 poison counter from infect combat damage, so they should discard 1 card
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
    }

    @Test
    @DisplayName("Sacrifice with no cards in opponent's hand does not error")
    void sacrificeWithEmptyOpponentHand() {
        gd.playerPoisonCounters.put(player2.getId(), 3);
        harness.setHand(player2, List.of());

        Permanent specter = addReadyCreature(player1, new WhisperingSpecter());
        specter.setAttacking(true);

        resolveCombat();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        // Specter should be sacrificed even though opponent has no cards
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Whispering Specter"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Whispering Specter"));
    }
}
