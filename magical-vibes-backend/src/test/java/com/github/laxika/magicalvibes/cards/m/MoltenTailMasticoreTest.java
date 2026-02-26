package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoltenTailMasticoreTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Has upkeep sacrifice-unless-discard effect")
    void hasUpkeepEffect() {
        MoltenTailMasticore card = new MoltenTailMasticore();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(SacrificeUnlessDiscardCardTypeEffect.class);
        SacrificeUnlessDiscardCardTypeEffect effect =
                (SacrificeUnlessDiscardCardTypeEffect) card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst();
        assertThat(effect.requiredType()).isNull(); // any card type
    }

    @Test
    @DisplayName("Has two activated abilities: damage and regenerate")
    void hasTwoActivatedAbilities() {
        MoltenTailMasticore card = new MoltenTailMasticore();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // Ability 0: {4}, Exile creature from graveyard: deal 4 damage
        var damageAbility = card.getActivatedAbilities().get(0);
        assertThat(damageAbility.getManaCost()).isEqualTo("{4}");
        assertThat(damageAbility.isRequiresTap()).isFalse();
        assertThat(damageAbility.getEffects()).hasSize(2);
        assertThat(damageAbility.getEffects().get(0)).isInstanceOf(ExileCardFromGraveyardCost.class);
        ExileCardFromGraveyardCost exileCost = (ExileCardFromGraveyardCost) damageAbility.getEffects().get(0);
        assertThat(exileCost.requiredType()).isEqualTo(CardType.CREATURE);
        assertThat(damageAbility.getEffects().get(1)).isInstanceOf(DealDamageToAnyTargetEffect.class);
        assertThat(((DealDamageToAnyTargetEffect) damageAbility.getEffects().get(1)).damage()).isEqualTo(4);

        // Ability 1: {2}: Regenerate
        var regenAbility = card.getActivatedAbilities().get(1);
        assertThat(regenAbility.getManaCost()).isEqualTo("{2}");
        assertThat(regenAbility.isRequiresTap()).isFalse();
        assertThat(regenAbility.getEffects()).hasSize(1);
        assertThat(regenAbility.getEffects().getFirst()).isInstanceOf(RegenerateEffect.class);
    }

    // ===== Upkeep — sacrifice unless discard =====

    @Test
    @DisplayName("Upkeep with card in hand — prompts may ability choice")
    void upkeepWithCardInHandPromptsMayAbility() {
        harness.addToBattlefield(player1, new MoltenTailMasticore());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    @Test
    @DisplayName("Accepting upkeep discard keeps Masticore alive")
    void acceptingUpkeepDiscardKeepsMasticore() {
        harness.addToBattlefield(player1, new MoltenTailMasticore());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Molten-Tail Masticore"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining upkeep discard sacrifices Masticore")
    void decliningUpkeepDiscardSacrificesMasticore() {
        harness.addToBattlefield(player1, new MoltenTailMasticore());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Molten-Tail Masticore"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Molten-Tail Masticore"));
    }

    @Test
    @DisplayName("Auto-sacrifices when controller has empty hand")
    void autoSacrificesWithEmptyHand() {
        harness.addToBattlefield(player1, new MoltenTailMasticore());
        harness.setHand(player1, List.of());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Molten-Tail Masticore"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Molten-Tail Masticore"));
    }

    // ===== Damage ability — exile creature from graveyard + deal 4 damage =====

    @Test
    @DisplayName("Damage ability prompts for graveyard exile cost choice when creature in graveyard")
    void damageAbilityPromptsForGraveyardExileCost() {
        harness.addToBattlefield(player1, new MoltenTailMasticore());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // Masticore is at index 0 on player1's battlefield; ability 0 = damage ability
        harness.activateAbility(player1, 0, 0, null, bearsId);

        assertThat(gd.interaction.awaitingInputType())
                .isEqualTo(AwaitingInput.ACTIVATED_ABILITY_GRAVEYARD_EXILE_COST_CHOICE);
    }

    @Test
    @DisplayName("Damage ability deals 4 damage to target creature after exiling creature card from graveyard")
    void damageAbilityDeals4DamageAfterExilingCreature() {
        harness.addToBattlefield(player1, new MoltenTailMasticore());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 0, null, bearsId);

        // Choose the creature card from graveyard to exile
        harness.handleGraveyardCardChosen(player1, 0);

        // Ability should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Creature card should be exiled from graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));

        // Resolve the ability
        harness.passBothPriorities();

        // Grizzly Bears (2/2) should be destroyed by 4 damage
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Damage ability can target a player")
    void damageAbilityCanTargetPlayer() {
        harness.addToBattlefield(player1, new MoltenTailMasticore());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, 0, 0, null, player2.getId());

        // Choose creature card to exile
        harness.handleGraveyardCardChosen(player1, 0);

        // Resolve
        harness.passBothPriorities();

        // Player 2 should have taken 4 damage (20 - 4 = 16)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Damage ability fails without creature card in graveyard")
    void damageAbilityFailsWithoutCreatureInGraveyard() {
        harness.addToBattlefield(player1, new MoltenTailMasticore());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of()); // empty graveyard
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Damage ability fails without enough mana")
    void damageAbilityFailsWithoutEnoughMana() {
        harness.addToBattlefield(player1, new MoltenTailMasticore());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 3); // not enough mana
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        // The graveyard exile cost prompt should happen first (before mana is checked)
        harness.activateAbility(player1, 0, 0, null, bearsId);
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Damage ability only allows exiling creature cards, not non-creature cards")
    void damageAbilityOnlyExilesCreatureCards() {
        harness.addToBattlefield(player1, new MoltenTailMasticore());
        harness.addToBattlefield(player2, new GrizzlyBears());
        // Put a non-creature card in the graveyard
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Graveyard exile cost only shows creature indices when mixed card types in graveyard")
    void graveyardExileCostShowsOnlyCreatureIndices() {
        harness.addToBattlefield(player1, new MoltenTailMasticore());
        harness.addToBattlefield(player2, new GrizzlyBears());
        // Graveyard: non-creature at index 0, creature at index 1
        harness.setGraveyard(player1, List.of(new Shock(), new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, 0, null, bearsId);

        // Should prompt graveyard exile cost choice
        assertThat(gd.interaction.awaitingInputType())
                .isEqualTo(AwaitingInput.ACTIVATED_ABILITY_GRAVEYARD_EXILE_COST_CHOICE);

        // Choose index 1 (creature card)
        harness.handleGraveyardCardChosen(player1, 1);

        // Llanowar Elves should be exiled
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
        // Shock should remain in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Shock"));
    }

    @Test
    @DisplayName("Mana is consumed when damage ability is activated")
    void damageAbilityConsumesMana() {
        harness.addToBattlefield(player1, new MoltenTailMasticore());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.handleGraveyardCardChosen(player1, 0);

        // 5 - 4 = 1 mana remaining
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    // ===== Regenerate ability =====

    @Test
    @DisplayName("Regenerate ability grants regeneration shield")
    void regenerateAbilityGrantsShield() {
        harness.addToBattlefield(player1, new MoltenTailMasticore());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        // Ability 1 = regenerate
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities(); // resolve the regeneration ability

        // Masticore should have a regeneration shield
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regenerate ability costs {2}")
    void regenerateAbilityCosts2Mana() {
        harness.addToBattlefield(player1, new MoltenTailMasticore());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 1, null, null);

        // 3 - 2 = 1 mana remaining
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regenerate ability fails without enough mana")
    void regenerateAbilityFailsWithoutMana() {
        harness.addToBattlefield(player1, new MoltenTailMasticore());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }
}
