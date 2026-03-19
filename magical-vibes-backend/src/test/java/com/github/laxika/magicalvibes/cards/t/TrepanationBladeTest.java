package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.EquipEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilTypeMillAndBoostAttackerEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrepanationBladeTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Trepanation Blade has ON_ATTACK reveal-until-land effect")
    void hasOnAttackEffect() {
        TrepanationBlade card = new TrepanationBlade();

        assertThat(card.getEffects(EffectSlot.ON_ATTACK)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_ATTACK).getFirst())
                .isInstanceOf(RevealUntilTypeMillAndBoostAttackerEffect.class);
    }

    @Test
    @DisplayName("Trepanation Blade has equip {2} ability")
    void hasEquipAbility() {
        TrepanationBlade card = new TrepanationBlade();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{2}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getTargetFilter())
                .isInstanceOf(ControlledPermanentPredicateTargetFilter.class);
        assertThat(card.getActivatedAbilities().get(0).getTimingRestriction())
                .isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(EquipEffect.class);
    }

    // ===== Attack trigger: reveal until land, mill, and boost =====

    @Test
    @DisplayName("Reveals cards until a land is found, mills them all, and boosts equipped creature")
    void revealsUntilLandAndBoosts() {
        Permanent creature = addReadyCreature(player1);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        // Deck: 3 creatures then 1 land (4 cards revealed = +4/+0)
        setDeckWithLandAtPosition(player2, 3);

        declareAttackers(player1, List.of(0));

        // Trigger should be on the stack
        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Trepanation Blade"));

        // Resolve the trigger
        harness.passBothPriorities();

        // Creature base 2/2, should get +4/+0 (3 creatures + 1 land = 4 revealed)
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        // All 4 revealed cards should be in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Only reveals one card when the top card is a land")
    void topCardIsLand() {
        Permanent creature = addReadyCreature(player1);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        // Deck: land on top (1 card revealed = +1/+0)
        setDeckWithLandAtPosition(player2, 0);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        // Creature base 2/2, should get +1/+0 (just the land)
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        // 1 card in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Reveals entire library when no lands are present, all go to graveyard")
    void noLandsInLibrary() {
        Permanent creature = addReadyCreature(player1);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        // Deck: 5 creatures, no lands
        setDeckNoLands(player2, 5);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        // Creature base 2/2, should get +5/+0 (all 5 cards revealed)
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        // Entire library in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does nothing when defending player's library is empty")
    void emptyLibrary() {
        Permanent creature = addReadyCreature(player1);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        // Empty deck
        gd.playerDecks.put(player2.getId(), new ArrayList<>());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        // No boost
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        // No cards in graveyard
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Toughness is not boosted (only +1/+0 per card)")
    void toughnessNotBoosted() {
        Permanent creature = addReadyCreature(player1);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        setDeckWithLandAtPosition(player2, 2);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        // Power boosted: 2 + 3 = 5
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        // Toughness unchanged
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Remaining library is preserved after revealing until land")
    void remainingLibraryPreserved() {
        Permanent creature = addReadyCreature(player1);
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        // 2 creatures then 1 land then 5 more creatures = 8 total
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            deck.add(new GrizzlyBears());
        }
        deck.add(new Forest());
        for (int i = 0; i < 5; i++) {
            deck.add(new GrizzlyBears());
        }
        gd.playerDecks.put(player2.getId(), deck);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        // 3 cards revealed (2 creatures + 1 land), 5 remain in library
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    // ===== No trigger for unequipped creature =====

    @Test
    @DisplayName("Trigger does not fire when Blade is not attached to the attacker")
    void noTriggerWhenUnattached() {
        Permanent creature = addReadyCreature(player1);
        addBladeReady(player1); // Blade on battlefield but not attached

        setDeckWithLandAtPosition(player2, 3);

        declareAttackers(player1, List.of(0));

        // No triggered ability on the stack from the blade
        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getName().equals("Trepanation Blade"));
    }

    // ===== Re-equip =====

    @Test
    @DisplayName("Blade can be moved to another creature via equip")
    void canReEquipToAnotherCreature() {
        Permanent blade = addBladeReady(player1);
        Permanent creature1 = addReadyCreature(player1);
        Permanent creature2 = addReadyCreature(player1);

        blade.setAttachedTo(creature1.getId());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature2.getId());
    }

    // ===== Helpers =====

    private Permanent addBladeReady(Player player) {
        Permanent perm = new Permanent(new TrepanationBlade());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    /**
     * Creates a deck with {@code nonLandCount} creature cards followed by 1 land card.
     */
    private void setDeckWithLandAtPosition(Player player, int nonLandCount) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < nonLandCount; i++) {
            deck.add(new GrizzlyBears());
        }
        deck.add(new Forest());
        gd.playerDecks.put(player.getId(), deck);
    }

    /**
     * Creates a deck with only creature cards (no lands).
     */
    private void setDeckNoLands(Player player, int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new GrizzlyBears());
        }
        gd.playerDecks.put(player.getId(), deck);
    }

    private void declareAttackers(Player player, List<Integer> attackerIndices) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);
        gs.declareAttackers(gd, player, attackerIndices);
    }
}
