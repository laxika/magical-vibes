package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CurseOfTheBloodyTomeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Curse of the Bloody Tome has correct effects")
    void hasCorrectEffects() {
        CurseOfTheBloodyTome card = new CurseOfTheBloodyTome();

        assertThat(card.isAura()).isTrue();
        assertThat(card.isEnchantPlayer()).isTrue();
        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getEffects(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(MillTargetPlayerEffect.class);
        MillTargetPlayerEffect effect = (MillTargetPlayerEffect) card.getEffects(EffectSlot.ENCHANTED_PLAYER_UPKEEP_TRIGGERED).getFirst();
        assertThat(effect.count()).isEqualTo(2);
    }

    // ===== Casting and resolving =====

    @Test
    @DisplayName("Casting Curse of the Bloody Tome targeting a player puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new CurseOfTheBloodyTome()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Resolving Curse of the Bloody Tome attaches it to target player")
    void resolvingAttachesToPlayer() {
        harness.setHand(player1, List.of(new CurseOfTheBloodyTome()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curse of the Bloody Tome")
                        && p.isAttached()
                        && p.getAttachedTo().equals(player2.getId()));
    }

    // ===== Upkeep mill trigger =====

    @Test
    @DisplayName("Enchanted player mills 2 cards at their upkeep")
    void enchantedPlayerMillsAtUpkeep() {
        Permanent auraPerm = new Permanent(new CurseOfTheBloodyTome());
        auraPerm.setAttachedTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();
        int graveyardSizeBefore = gd.playerGraveyards.get(player2.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 2);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardSizeBefore + 2);
    }

    @Test
    @DisplayName("Mill trigger does NOT fire during aura controller's upkeep")
    void millDoesNotFireDuringAuraControllerUpkeep() {
        Permanent auraPerm = new Permanent(new CurseOfTheBloodyTome());
        auraPerm.setAttachedTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Player1's deck should be unchanged
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore);
    }

    @Test
    @DisplayName("Mill accumulates over multiple upkeeps")
    void millAccumulatesOverUpkeeps() {
        Permanent auraPerm = new Permanent(new CurseOfTheBloodyTome());
        auraPerm.setAttachedTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore - 4);
    }

    // ===== Removal =====

    @Test
    @DisplayName("No mill after Curse is removed")
    void noMillAfterRemoval() {
        Permanent auraPerm = new Permanent(new CurseOfTheBloodyTome());
        auraPerm.setAttachedTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        // Remove the curse
        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        int deckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(deckSizeBefore);
    }

    // ===== Curse attached to player is not orphaned =====

    @Test
    @DisplayName("Curse attached to player is not removed as orphaned aura")
    void curseAttachedToPlayerNotOrphaned() {
        Permanent auraPerm = new Permanent(new CurseOfTheBloodyTome());
        auraPerm.setAttachedTo(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        // Advance through several steps to trigger SBA / orphan aura checks
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Curse should still be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curse of the Bloody Tome"));
    }

    // ===== Can curse self =====

    @Test
    @DisplayName("Can cast Curse targeting yourself")
    void canCurseSelf() {
        harness.setHand(player1, List.of(new CurseOfTheBloodyTome()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Curse of the Bloody Tome")
                        && p.getAttachedTo().equals(player1.getId()));
    }

    @Test
    @DisplayName("Self-cursed player mills at their own upkeep")
    void selfCursedPlayerMillsAtUpkeep() {
        Permanent auraPerm = new Permanent(new CurseOfTheBloodyTome());
        auraPerm.setAttachedTo(player1.getId());
        gd.playerBattlefields.get(player1.getId()).add(auraPerm);

        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 2);
    }

    // ===== Helpers =====

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
