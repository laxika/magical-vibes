package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEnchantedPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurseOfThirstTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Resolving Curse of Thirst attaches it to target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new CurseOfThirst()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curse of Thirst")
                        && p.isAttached()
                        && p.getAttachedTo().equals(player2.getId()));
    }

    // ===== Upkeep trigger deals damage equal to curse count =====

    @Test
    @DisplayName("Deals 1 damage when it is the only Curse attached")
    void deals1DamageWhenOnlyCurse() {
        placeCurseOfThirstOnPlayer(player1, player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Deals damage equal to the number of Curses attached to the player")
    void dealsDamageEqualToCurseCount() {
        placeCurseOfThirstOnPlayer(player1, player2);
        // Two more Curses (with no upkeep-damage triggers) attached to the same player → 3 total Curses
        placeCurseOnPlayer(player1, player2, new CurseOfExhaustion());
        placeCurseOnPlayer(player1, player2, new CurseOfEchoes());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        // Resolve every triggered upkeep ability that landed on the stack.
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        // Curse of Thirst deals 3 — one for each of the three Curses attached to the player.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    // ===== Trigger timing =====

    @Test
    @DisplayName("Trigger does NOT fire during curse controller's upkeep")
    void triggerDoesNotFireDuringCurseControllerUpkeep() {
        placeCurseOfThirstOnPlayer(player1, player2);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    // ===== Removal =====

    @Test
    @DisplayName("No damage trigger after curse is removed")
    void noTriggerAfterRemoval() {
        Permanent cursePerm = placeCurseOfThirstOnPlayer(player1, player2);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        gd.playerBattlefields.get(player1.getId()).remove(cursePerm);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    // ===== Helpers =====

    private Permanent placeCurseOfThirstOnPlayer(Player controller, Player enchantedPlayer) {
        return placeCurseOnPlayer(controller, enchantedPlayer, new CurseOfThirst());
    }

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer, com.github.laxika.magicalvibes.model.Card curse) {
        Permanent cursePerm = new Permanent(curse);
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
