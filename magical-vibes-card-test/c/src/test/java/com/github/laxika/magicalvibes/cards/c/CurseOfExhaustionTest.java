package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HungerOfTheHowlpack;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CurseOfExhaustion.class, GrizzlyBears.class, Plains.class, HungerOfTheHowlpack.class})
class CurseOfExhaustionTest extends BaseCardTest {

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Resolving Curse of Exhaustion attaches it to the target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new CurseOfExhaustion()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curse of Exhaustion")
                        && p.isAttached()
                        && p.getAttachedTo().equals(player2.getId()));
    }

    // ===== Limits the enchanted player =====

    @Test
    @DisplayName("Enchanted player can cast their first spell")
    void allowsFirstSpell() {
        placeCurseOnPlayer(player1, player2);
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Enchanted player can't cast a second spell")
    void preventsSecondSpellForEnchantedPlayer() {
        placeCurseOnPlayer(player1, player2);
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player2, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Enchanted player can still play a land after casting a spell")
    void landsAreNotAffected() {
        placeCurseOnPlayer(player1, player2);
        harness.setHand(player2, List.of(new GrizzlyBears(), new Plains()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        // Play a land — not a spell, so it is unaffected by the curse
        harness.playLand(player2, 0);

        harness.assertOnBattlefield(player2, "Plains");
    }

    // ===== Does NOT limit other players =====

    @Test
    @DisplayName("Does not limit a player who is not enchanted")
    void doesNotLimitNonEnchantedPlayer() {
        // Curse enchants player2, but player1 (the controller) is not limited.
        placeCurseOnPlayer(player1, player2);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // Second spell for the non-enchanted player must succeed
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Removal =====

    @Test
    @DisplayName("Removing the curse restores normal casting")
    void removingCurseRestoresCasting() {
        Permanent cursePerm = placeCurseOnPlayer(player1, player2);
        harness.setHand(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        // Remove the curse
        gd.playerBattlefields.get(player1.getId()).remove(cursePerm);

        // Second spell should now be castable
        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Casting the Curse on yourself uses your one spell for the turn")
    void selfEnchantmentCountsItsOwnCast() {
        harness.setHand(player1, List.of(new CurseOfExhaustion(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Curse of Exhaustion").getAttachedTo())
                .isEqualTo(player1.getId());
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Spells cast before the Curse enters still count toward the limit")
    void countsSpellsCastBeforeCurseEnters() {
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        placeCurseOnPlayer(player2, player1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Multiple Curses still allow exactly one spell")
    void multipleCursesDoNotReduceLimitToZero() {
        placeCurseOnPlayer(player2, player1);
        placeCurseOnPlayer(player2, player1);
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("The limit applies to instants on another player's turn and resets next turn")
    void instantLimitResetsAcrossTurns() {
        placeCurseOnPlayer(player1, player2);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new HungerOfTheHowlpack(), new HungerOfTheHowlpack(),
                new HungerOfTheHowlpack()));
        harness.addMana(player2, ManaColor.GREEN, 3);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent cursePerm = harness.addToBattlefieldAndReturn(controller, new CurseOfExhaustion());
        cursePerm.setAttachedTo(enchantedPlayer.getId());
        return cursePerm;
    }
}
