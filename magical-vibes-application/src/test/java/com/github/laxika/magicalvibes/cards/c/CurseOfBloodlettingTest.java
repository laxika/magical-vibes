package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DoubleDamageToEnchantedPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurseOfBloodlettingTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Curse of Bloodletting has correct effects")
    void hasCorrectEffects() {
        CurseOfBloodletting card = new CurseOfBloodletting();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(DoubleDamageToEnchantedPlayerEffect.class);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Can cast Curse of Bloodletting targeting a player")
    void canCastTargetingPlayer() {
        harness.setHand(player1, List.of(new CurseOfBloodletting()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving Curse of Bloodletting attaches it to the target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new CurseOfBloodletting()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curse of Bloodletting")
                        && p.isAttached()
                        && p.getAttachedTo().equals(player2.getId()));
    }

    // ===== Doubles spell damage to enchanted player =====

    @Test
    @DisplayName("Doubles spell damage dealt to the enchanted player")
    void doublesSpellDamageToEnchantedPlayer() {
        placeCurseOnPlayer(player1, player2);
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        // 3 damage doubled to 6
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Opponent's own curse on you doubles incoming damage regardless of who casts it")
    void doublesDamageFromAnySource() {
        // Curse controlled by player2 enchanting player1; player1 takes doubled damage
        placeCurseOnPlayer(player2, player1);
        harness.setHand(player2, List.of(new Blaze()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.forceActivePlayer(player2);
        harness.castSorcery(player2, 0, 2, player1.getId());
        harness.passBothPriorities();

        // 2 damage doubled to 4
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    // ===== Does NOT affect non-enchanted players =====

    @Test
    @DisplayName("Does not double damage dealt to a non-enchanted player")
    void doesNotDoubleDamageToNonEnchantedPlayer() {
        // Curse enchants player2, but damage is dealt to player1
        placeCurseOnPlayer(player1, player2);
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player1, 20);

        harness.castSorcery(player1, 0, 3, player1.getId());
        harness.passBothPriorities();

        // 3 damage, not doubled (player1 is not enchanted)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    // ===== Doubles combat damage =====

    @Test
    @DisplayName("Doubles unblocked combat damage to the enchanted player")
    void doublesCombatDamageToEnchantedPlayer() {
        placeCurseOnPlayer(player1, player2);
        harness.setLife(player2, 20);

        Permanent bear = new Permanent(new GrizzlyBears()); // 2/2
        bear.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bear);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(1)); // curse at index 0, bear at index 1

        // 2 combat damage doubled to 4
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    // ===== Stacks multiplicatively =====

    @Test
    @DisplayName("Two curses on the same player quadruple damage")
    void twoCursesQuadrupleDamage() {
        placeCurseOnPlayer(player1, player2);
        placeCurseOnPlayer(player1, player2);
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        // 3 damage * 2 * 2 = 12
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(8);
    }

    @Test
    @DisplayName("Stacks multiplicatively with Furnace of Rath")
    void stacksWithFurnaceOfRath() {
        placeCurseOnPlayer(player1, player2);
        harness.addToBattlefield(player1, new FurnaceOfRath());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        // 3 damage * 2 (Furnace) * 2 (Curse) = 12
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(8);
    }

    // ===== Removal =====

    @Test
    @DisplayName("Removing the curse stops the doubling")
    void removingCurseStopsDoubling() {
        Permanent cursePerm = placeCurseOnPlayer(player1, player2);
        harness.setLife(player2, 20);

        // Remove the curse before any damage
        gd.playerBattlefields.get(player1.getId()).remove(cursePerm);

        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castSorcery(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        // 2 damage, not doubled
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Zero damage =====

    @Test
    @DisplayName("Zero damage stays zero even with the curse")
    void zeroDamageNotDoubled() {
        placeCurseOnPlayer(player1, player2);
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ===== Helpers =====

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent cursePerm = new Permanent(new CurseOfBloodletting());
        cursePerm.setAttachedTo(enchantedPlayer.getId());
        gd.playerBattlefields.get(controller.getId()).add(cursePerm);
        return cursePerm;
    }
}
