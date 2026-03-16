package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurseOfThePiercedHeartTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Curse of the Pierced Heart has correct effects")
    void hasCorrectEffects() {
        CurseOfThePiercedHeart card = new CurseOfThePiercedHeart();

        assertThat(card.getEffects(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(DealDamageToEnchantedPlayerEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Can cast Curse of the Pierced Heart targeting a player")
    void canCastTargetingPlayer() {
        harness.setHand(player1, List.of(new CurseOfThePiercedHeart()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving Curse of the Pierced Heart attaches it to target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new CurseOfThePiercedHeart()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curse of the Pierced Heart")
                        && p.isAttached()
                        && p.getAttachedTo().equals(player2.getId()));
    }

    // ===== Upkeep trigger deals damage =====

    @Test
    @DisplayName("Enchanted player takes 1 damage at their upkeep")
    void enchantedPlayerTakes1DamageAtUpkeep() {
        placeCurseOnPlayer(player1, player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    // ===== Trigger timing =====

    @Test
    @DisplayName("Trigger does NOT fire during curse controller's upkeep")
    void triggerDoesNotFireDuringCurseControllerUpkeep() {
        placeCurseOnPlayer(player1, player2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Multiple triggers =====

    @Test
    @DisplayName("Two curses deal 2 total damage at enchanted player's upkeep")
    void twoCursesDeal2Damage() {
        placeCurseOnPlayer(player1, player2);
        placeCurseOnPlayer(player1, player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve first trigger
        harness.passBothPriorities(); // resolve second trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    // ===== Removal =====

    @Test
    @DisplayName("No damage trigger after curse is removed")
    void noTriggerAfterRemoval() {
        Permanent cursePerm = placeCurseOnPlayer(player1, player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        // Remove the curse
        gd.playerBattlefields.get(player1.getId()).remove(cursePerm);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    // ===== Helpers =====

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent cursePerm = new Permanent(new CurseOfThePiercedHeart());
        cursePerm.setAttachedTo(enchantedPlayer.getId());
        gd.playerBattlefields.get(controller.getId()).add(cursePerm);
        return cursePerm;
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
