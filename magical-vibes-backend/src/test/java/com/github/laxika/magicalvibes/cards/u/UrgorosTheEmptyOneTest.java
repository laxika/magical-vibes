package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerRandomDiscardOrControllerDrawsEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UrgorosTheEmptyOneTest extends BaseCardTest {

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
    @DisplayName("Urgoros has TargetPlayerRandomDiscardOrControllerDrawsEffect on ON_COMBAT_DAMAGE_TO_PLAYER")
    void hasCorrectEffect() {
        UrgorosTheEmptyOne card = new UrgorosTheEmptyOne();

        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst())
                .isInstanceOf(TargetPlayerRandomDiscardOrControllerDrawsEffect.class);
    }

    @Test
    @DisplayName("Dealing combat damage forces opponent to discard a card at random when they have cards")
    void combatDamageTriggersRandomDiscard() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        int controllerHandSize = gd.playerHands.get(player1.getId()).size();

        Permanent urgoros = addReadyCreature(player1, new UrgorosTheEmptyOne());
        urgoros.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("discards") && log.contains("at random"));
        // Controller should NOT draw when opponent discarded
        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandSize);
    }

    @Test
    @DisplayName("Discards one card when opponent has multiple cards in hand")
    void discardsOneCardFromMultiple() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        Permanent urgoros = addReadyCreature(player1, new UrgorosTheEmptyOne());
        urgoros.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Controller draws a card when opponent has empty hand")
    void drawsCardWhenOpponentHandEmpty() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of());
        int controllerHandSize = gd.playerHands.get(player1.getId()).size();

        Permanent urgoros = addReadyCreature(player1, new UrgorosTheEmptyOne());
        urgoros.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandSize + 1);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("draws a card"));
    }

    @Test
    @DisplayName("No trigger when Urgoros is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        GameData gd = harness.getGameData();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        int handSizeBefore = gd.playerHands.get(player2.getId()).size();
        int controllerHandSize = gd.playerHands.get(player1.getId()).size();

        Permanent urgoros = addReadyCreature(player1, new UrgorosTheEmptyOne());
        urgoros.setAttacking(true);
        // Urgoros is 4/3, blocker needs to absorb all damage
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandSize);
    }

    @Test
    @DisplayName("Defender takes 4 combat damage from unblocked Urgoros")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new GrizzlyBears()));

        Permanent urgoros = addReadyCreature(player1, new UrgorosTheEmptyOne());
        urgoros.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Game advances after trigger resolves")
    void gameAdvancesAfterTrigger() {
        harness.setHand(player2, List.of(new GrizzlyBears()));

        Permanent urgoros = addReadyCreature(player1, new UrgorosTheEmptyOne());
        urgoros.setAttacking(true);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }
}
