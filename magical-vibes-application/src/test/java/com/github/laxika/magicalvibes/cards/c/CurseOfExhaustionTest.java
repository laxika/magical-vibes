package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.LimitSpellsForEnchantedPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CurseOfExhaustionTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Curse of Exhaustion has correct effects")
    void hasCorrectEffects() {
        CurseOfExhaustion card = new CurseOfExhaustion();

        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(LimitSpellsForEnchantedPlayerEffect.class);
        LimitSpellsForEnchantedPlayerEffect effect =
                (LimitSpellsForEnchantedPlayerEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.maxSpells()).isEqualTo(1);
    }

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
        gs.playCard(gd, player2, 0, 0, null, null);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Plains"));
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

    // ===== Helpers =====

    private Permanent placeCurseOnPlayer(Player controller, Player enchantedPlayer) {
        Permanent cursePerm = new Permanent(new CurseOfExhaustion());
        cursePerm.setAttachedTo(enchantedPlayer.getId());
        gd.playerBattlefields.get(controller.getId()).add(cursePerm);
        return cursePerm;
    }
}
