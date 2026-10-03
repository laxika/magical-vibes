package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.FuneralCharm;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({DemonicVigor.class, GrizzlyBears.class, DoomBlade.class, Naturalize.class, Unsummon.class, FuneralCharm.class})
class DemonicVigorTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addCreatureWithAura(player1, player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3); // 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3); // 2 + 1
    }

    @Test
    @DisplayName("When enchanted creature dies, it returns to owner's hand")
    void creatureReturnsToHandWhenDestroyed() {
        Permanent creature = addCreatureWithAura(player1, player1);
        Card creatureCard = creature.getCard();

        // Opponent destroys the enchanted creature with Doom Blade
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities(); // resolve return-to-hand trigger

        // Creature should be in player1's hand, not in the graveyard
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creatureCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("Aura goes to graveyard when enchanted creature dies")
    void auraGoesToGraveyardWhenCreatureDies() {
        Permanent creature = addCreatureWithAura(player1, player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities(); // resolve trigger

        // Aura should be in graveyard
        harness.assertInGraveyard(player1, "Demonic Vigor");
        // Neither creature nor aura should be on the battlefield
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Demonic Vigor");
    }

    @Test
    @DisplayName("No trigger when a different creature dies")
    void noTriggerWhenDifferentCreatureDies() {
        Permanent enchantedCreature = addCreatureWithAura(player1, player1);

        // Add a second creature (not enchanted)
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        // Opponent destroys the non-enchanted creature
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player2, 0, otherCreature.getId());

        // The non-enchanted creature should be in graveyard, not hand
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(otherCreature.getCard().getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(otherCreature.getCard().getId()));
        // Enchanted creature should still be on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(enchantedCreature.getId()));
    }

    @Test
    @DisplayName("Opponent's enchanted creature returns to its owner's hand")
    void returnsCreatureToOwnerWhenEnchantingOpponent() {
        // Player 1 controls the aura, Player 2 controls the creature
        Permanent creature = addCreatureWithAura(player2, player1);
        Card creatureCard = creature.getCard();

        // Player 1 destroys the creature
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve trigger

        // Creature should return to player2's hand (the owner), not player1
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(creatureCard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creatureCard.getId()));
    }

    @Test
    @DisplayName("Casting the Aura attaches it and boosts only its target")
    void castingAuraBoostsOnlyTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent other = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DemonicVigor()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Demonic Vigor");
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, other)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, other)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing the Aura removes the boost and prevents its later death trigger")
    void removedAuraDoesNotReturnCreature() {
        Permanent creature = addCreatureWithAura(player1, player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Naturalize(), new DoomBlade()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player2, 0, harness.getPermanentId(player1, "Demonic Vigor"));

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature.getCard());
    }

    @Test
    @DisplayName("Returning the enchanted creature to hand does not trigger the Aura")
    void bounceDoesNotTriggerReturn() {
        Permanent creature = addCreatureWithAura(player1, player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player2, 0, creature.getId());

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).contains(creature.getCard());
        harness.assertInGraveyard(player1, "Demonic Vigor");
    }

    @Test
    @DisplayName("An Aura spell whose target dies does not return that creature")
    void creatureDiesBeforeAuraResolves() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DemonicVigor()));
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castEnchantment(player1, 0, creature.getId());
        gs.passPriority(gd, player1);

        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature.getCard());
        harness.assertInGraveyard(player1, "Demonic Vigor");
    }

    @Test
    @DisplayName("A pending death trigger cannot return the card after it leaves and reenters the graveyard")
    void pendingTriggerDoesNotReturnNewGraveyardObject() {
        Permanent creature = addCreatureWithAura(player1, player1);
        Permanent secondAura = harness.addToBattlefieldAndReturn(player1, new DemonicVigor());
        secondAura.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new DoomBlade(), new FuneralCharm()));
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castAndResolveInstant(player2, 0, creature.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature.getCard());
        assertThat(gd.stack).hasSize(1);

        harness.castInstant(player2, 0, 0, player1.getId());
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature.getCard());
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature.getCard());
    }

    /**
     * Places a Grizzly Bears (2/2) on the creature controller's battlefield and attaches
     * a Demonic Vigor controlled by the aura controller.
     *
     * @return the Grizzly Bears permanent
     */
    private Permanent addCreatureWithAura(Player creatureController, Player auraController) {
        Permanent creature = harness.addToBattlefieldAndReturn(creatureController, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new DemonicVigor());
        aura.setAttachedTo(creature.getId());

        return creature;
    }
}
