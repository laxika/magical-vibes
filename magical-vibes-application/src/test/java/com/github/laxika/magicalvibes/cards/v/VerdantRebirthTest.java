package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class VerdantRebirthTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Verdant Rebirth has correct effect structure")
    void hasCorrectEffects() {
        VerdantRebirth card = new VerdantRebirth();

        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0))
                .isInstanceOf(GrantEffectToTargetUntilEndOfTurnEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1))
                .isInstanceOf(DrawCardEffect.class);
    }

    // ===== Casting and resolution =====

    @Test
    @DisplayName("Casting Verdant Rebirth draws a card")
    void drawsACard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VerdantRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve Verdant Rebirth

        // Hand had 1 card (Verdant Rebirth), cast it (0), drew 1 card = 1 card in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Creature returns to owner's hand when it dies after Verdant Rebirth")
    void creatureReturnsToHandOnDeath() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card creatureCard = creature.getCard();

        // Cast Verdant Rebirth targeting the creature
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VerdantRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve Verdant Rebirth

        // Now destroy the creature with Doom Blade
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities(); // resolve Doom Blade — creature dies, trigger goes on stack
        harness.passBothPriorities(); // resolve return-to-hand trigger

        // Creature should be in player1's hand, not in the graveyard
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creatureCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("Creature does NOT return to hand if it dies after end of turn (effect expired)")
    void effectExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
        Card creatureCard = creature.getCard();

        // Cast Verdant Rebirth targeting the creature
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VerdantRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve Verdant Rebirth

        // The flag should be set
        assertThat(creature.getTemporaryTriggeredEffects(EffectSlot.ON_DEATH)).isNotEmpty();

        // Advance to end step and pass priorities — this triggers cleanup which resets "until end of turn" effects
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // The temporary effect should be cleared
        assertThat(creature.getTemporaryTriggeredEffects(EffectSlot.ON_DEATH)).isEmpty();

        // Now destroy the creature on the next turn
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities(); // resolve Doom Blade

        // Creature should be in graveyard, NOT in hand (effect expired)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("Verdant Rebirth on opponent's creature returns it to opponent's hand")
    void returnsOpponentCreatureToOwnersHand() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player2.getId()).getFirst();
        Card creatureCard = creature.getCard();

        // Player 1 casts Verdant Rebirth targeting player 2's creature
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VerdantRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve Verdant Rebirth

        // Player 1 destroys the creature
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve Doom Blade — creature dies, trigger goes on stack
        harness.passBothPriorities(); // resolve return-to-hand trigger

        // Creature should return to player 2's hand (the owner), not player 1
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(creatureCard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("Non-targeted creature does not get the return-to-hand ability")
    void nonTargetedCreatureDoesNotGetAbility() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature1 = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature2 = gd.playerBattlefields.get(player1.getId()).get(1);
        Card creature2Card = creature2.getCard();

        // Cast Verdant Rebirth targeting creature1 only
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VerdantRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, creature1.getId());
        harness.passBothPriorities(); // resolve Verdant Rebirth

        // Only creature1 should have the temporary death effect
        assertThat(creature1.getTemporaryTriggeredEffects(EffectSlot.ON_DEATH)).isNotEmpty();
        assertThat(creature2.getTemporaryTriggeredEffects(EffectSlot.ON_DEATH)).isEmpty();

        // Destroy creature2 — it should go to graveyard normally
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, creature2.getId());
        harness.passBothPriorities(); // resolve Doom Blade

        // creature2 should be in graveyard, not hand
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature2Card.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature2Card.getId()));
    }

    @Test
    @DisplayName("Verdant Rebirth goes to graveyard after resolution")
    void spellGoesToGraveyard() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VerdantRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve Verdant Rebirth

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Verdant Rebirth"));
    }
}
