package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VerdantRebirth.class, RaptorCompanion.class, VanquishTheWeak.class})
class VerdantRebirthTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Verdant Rebirth draws a card")
    void drawsACard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VerdantRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        // Hand had 1 card (Verdant Rebirth), cast it (0), drew 1 card = 1 card in hand
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Creature returns to owner's hand when it dies after Verdant Rebirth")
    void creatureReturnsToHandOnDeath() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        Card creatureCard = creature.getCard();

        // Cast Verdant Rebirth targeting the creature
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VerdantRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        // Now destroy the creature with Vanquish the Weak
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new VanquishTheWeak()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, creature.getId());
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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        Card creatureCard = creature.getCard();

        // Cast Verdant Rebirth targeting the creature
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VerdantRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        // Advance to end step and pass priorities — this triggers cleanup which resets "until end of turn" effects
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Now destroy the creature on the next turn
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new VanquishTheWeak()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        // Creature should be in graveyard, NOT in hand (effect expired)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("Verdant Rebirth on opponent's creature returns it to opponent's hand")
    void returnsOpponentCreatureToOwnersHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        Card creatureCard = creature.getCard();

        // Player 1 casts Verdant Rebirth targeting player 2's creature
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VerdantRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        // Player 1 destroys the creature
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VanquishTheWeak()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, creature.getId());
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
        Permanent creature1 = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        Permanent creature2 = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        Card creature2Card = creature2.getCard();

        // Cast Verdant Rebirth targeting creature1 only
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VerdantRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, creature1.getId());

        // Destroy creature2 — it should go to graveyard normally
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new VanquishTheWeak()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, creature2.getId());

        // creature2 should be in graveyard, not hand
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature2Card.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature2Card.getId()));
    }

    @Test
    @DisplayName("Verdant Rebirth goes to graveyard after resolution")
    void spellGoesToGraveyard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VerdantRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.assertInGraveyard(player1, "Verdant Rebirth");
    }

    @Test
    @DisplayName("No card is drawn when the target dies before Verdant Rebirth resolves")
    void removedTargetPreventsDraw() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VerdantRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, creature.getId());

        harness.setHand(player2, List.of(new VanquishTheWeak()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Raptor Companion");
        harness.assertInGraveyard(player1, "Verdant Rebirth");
    }

    @Test
    @DisplayName("The granted ability still works during the end step")
    void returnsCreatureDyingDuringEndStep() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VerdantRebirth()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new VanquishTheWeak()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.assertInGraveyard(player1, "Raptor Companion");
        harness.assertNotInHand(player1, "Raptor Companion");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Raptor Companion");
        harness.assertNotInGraveyard(player1, "Raptor Companion");
    }
}
