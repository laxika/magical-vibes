package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.Blaze;
import com.github.laxika.magicalvibes.cards.f.FurnaceOfRath;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SamiteHealer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CurseOfBloodletting.class, Blaze.class, FurnaceOfRath.class, GrizzlyBears.class, SamiteHealer.class})
class CurseOfBloodlettingTest extends BaseCardTest {

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

    @Test
    @DisplayName("Doubles unblocked combat damage to the enchanted player")
    void doublesCombatDamageToEnchantedPlayer() {
        placeCurseOnPlayer(player1, player2);
        harness.setLife(player2, 20);

        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()); // 2/2
        bear.setSummoningSick(false);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        gs.declareAttackers(gd, player1, List.of(1)); // curse at index 0, bear at index 1

        // 2 combat damage doubled to 4
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

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

    @Test
    @DisplayName("Can enchant yourself and double damage from your own spell")
    void selfEnchantmentDoublesOwnSourceDamage() {
        harness.setHand(player1, List.of(new CurseOfBloodletting(), new Blaze()));
        harness.addMana(player1, ManaColor.RED, 9);
        harness.setLife(player1, 20);

        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities();
        harness.castSorcery(player1, 0, 3, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Does not double damage to creatures controlled by the enchanted player")
    void doesNotDoubleDamageToEnchantedPlayersCreature() {
        placeCurseOnPlayer(player1, player2);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, 1, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bear);
        assertThat(bear.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Enchanted player chooses whether prevention applies before doubling")
    void preventionAndDoublingRequireAffectedPlayersChoice() {
        placeCurseOnPlayer(player1, player2);
        Permanent healer = harness.addToBattlefieldAndReturn(player2, new SamiteHealer());
        healer.setSummoningSick(false);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        // Preventing first yields zero damage; doubling first yields one damage.
        // Neither outcome may be imposed before the affected player chooses.
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        harness.handleListChoice(player2, "Prevent the next 1 damage to you");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Doubling before prevention deals one damage and consumes the shield")
    void doublingBeforePreventionDealsOneDamage() {
        placeCurseOnPlayer(player1, player2);
        Permanent healer = harness.addToBattlefieldAndReturn(player2, new SamiteHealer());
        healer.setSummoningSick(false);
        harness.setLife(player2, 20);
        harness.activateAbility(player2, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Blaze()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castSorcery(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        harness.handleListChoice(player2, "Multiply damage to enchanted player by 2");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent cursePerm = harness.addToBattlefieldAndReturn(controller, new CurseOfBloodletting());
        cursePerm.setAttachedTo(enchantedPlayer.getId());
        return cursePerm;
    }
}
