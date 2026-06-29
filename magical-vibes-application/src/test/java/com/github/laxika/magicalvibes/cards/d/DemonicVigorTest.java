package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandOnDeathEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DemonicVigorTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Demonic Vigor has static boost and enchanted creature death trigger")
    void hasCorrectEffects() {
        DemonicVigor card = new DemonicVigor();

        assertThat(card.isAura()).isTrue();
        assertThat(EffectResolution.needsTarget(card)).isTrue();
        assertThat(card.getTargetFilter()).isInstanceOf(PermanentPredicateTargetFilter.class);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(StaticBoostEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ENCHANTED_PERMANENT_PUT_INTO_GRAVEYARD).getFirst())
                .isInstanceOf(ReturnEnchantedCreatureToOwnerHandOnDeathEffect.class);
    }

    // ===== Static boost =====

    @Test
    @DisplayName("Enchanted creature gets +1/+1")
    void enchantedCreatureGetsBoost() {
        Permanent creature = addCreatureWithAura(player1, player1);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3); // 2 + 1
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3); // 2 + 1
    }

    // ===== Death trigger =====

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
    @DisplayName("Aura goes to graveyard when enchanted creature dies")
    void auraGoesToGraveyardWhenCreatureDies() {
        Permanent creature = addCreatureWithAura(player1, player1);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities(); // resolve Doom Blade
        harness.passBothPriorities(); // resolve trigger

        // Aura should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Demonic Vigor"));
        // Neither creature nor aura should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Demonic Vigor"));
    }

    @Test
    @DisplayName("No trigger when a different creature dies")
    void noTriggerWhenDifferentCreatureDies() {
        Permanent enchantedCreature = addCreatureWithAura(player1, player1);

        // Add a second creature (not enchanted)
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent otherCreature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId() != enchantedCreature.getId()
                        && !p.getCard().getName().equals("Demonic Vigor"))
                .findFirst().orElseThrow();

        // Opponent destroys the non-enchanted creature
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, otherCreature.getId());
        harness.passBothPriorities(); // resolve Doom Blade

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
    @DisplayName("Aura controller gets the creature returned even when enchanting opponent's creature")
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
        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve Doom Blade
        harness.passBothPriorities(); // resolve trigger

        // Creature should return to player2's hand (the owner), not player1
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getId().equals(creatureCard.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creatureCard.getId()));
    }

    // ===== Helpers =====

    /**
     * Places a Grizzly Bears (2/2) on the creature controller's battlefield and attaches
     * a Demonic Vigor controlled by the aura controller.
     *
     * @return the Grizzly Bears permanent
     */
    private Permanent addCreatureWithAura(Player creatureController, Player auraController) {
        harness.addToBattlefield(creatureController, new GrizzlyBears());
        Permanent creature = gd.playerBattlefields.get(creatureController.getId()).getFirst();

        DemonicVigor auraCard = new DemonicVigor();
        Permanent aura = new Permanent(auraCard);
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);

        return creature;
    }
}
