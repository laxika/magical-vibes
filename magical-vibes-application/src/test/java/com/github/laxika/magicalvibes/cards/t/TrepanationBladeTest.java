package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AltarsReap;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TrepanationBlade.class, WalkingCorpse.class, Forest.class, Naturalize.class, AltarsReap.class})
class TrepanationBladeTest extends BaseCardTest {

    @Test
    @DisplayName("Trepanation Blade equips a creature for two mana")
    void equipsCreature() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Reveals cards until a land is found, mills them all, and boosts equipped creature")
    void revealsUntilLandAndBoosts() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
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
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
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
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
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
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        // Empty deck
        harness.setLibrary(player2, List.of());

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
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
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
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());

        // 2 creatures then 1 land then 5 more creatures = 8 total
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            deck.add(new WalkingCorpse());
        }
        deck.add(new Forest());
        for (int i = 0; i < 5; i++) {
            deck.add(new WalkingCorpse());
        }
        harness.setLibrary(player2, deck);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        // 3 cards revealed (2 creatures + 1 land), 5 remain in library
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
    }

    @Test
    @DisplayName("Trigger does not fire when Blade is not attached to the attacker")
    void noTriggerWhenUnattached() {
        addCreatureReady(player1, new WalkingCorpse());
        addBladeReady(player1); // Blade on battlefield but not attached

        setDeckWithLandAtPosition(player2, 3);

        declareAttackers(player1, List.of(0));

        // No triggered ability on the stack from the blade
        assertThat(gd.stack)
                .noneMatch(se -> se.getCard().getName().equals("Trepanation Blade"));
    }

    @Test
    @DisplayName("Blade can be moved to another creature via equip")
    void canReEquipToAnotherCreature() {
        Permanent blade = addBladeReady(player1);
        Permanent creature1 = addCreatureReady(player1, new WalkingCorpse());
        Permanent creature2 = addCreatureReady(player1, new WalkingCorpse());

        blade.setAttachedTo(creature1.getId());

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(blade.getAttachedTo()).isEqualTo(creature2.getId());
    }

    @Test
    @DisplayName("Destroying the Blade does not stop its attack trigger")
    void triggerResolvesAfterBladeDestroyed() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());
        setDeckWithLandAtPosition(player2, 2);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        harness.ensurePriority(player2);
        harness.castAndResolveInstant(player2, 0, blade.getId());
        harness.assertNotOnBattlefield(player1, "Trepanation Blade");
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> !(card instanceof Naturalize)).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Sacrificing the attacker does not stop revealing cards")
    void triggerResolvesAfterAttackerSacrificed() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());
        setDeckWithLandAtPosition(player2, 2);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setHand(player1, List.of(new AltarsReap()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(player1, List.of(0));
        harness.ensurePriority(player1);
        harness.castInstantWithSacrifice(player1, 0, null, creature.getId());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Walking Corpse");
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Unattaching the Blade does not stop the attacker's boost")
    void triggerResolvesAfterBladeUnattached() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());
        setDeckWithLandAtPosition(player2, 2);

        declareAttackers(player1, List.of(0));
        blade.setAttachedTo(null);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Moving the Blade still boosts the original attacker")
    void boostStaysWithOriginalAttacker() {
        Permanent attacker = addCreatureReady(player1, new WalkingCorpse());
        Permanent otherCreature = addCreatureReady(player1, new WalkingCorpse());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(attacker.getId());
        setDeckWithLandAtPosition(player2, 2);

        declareAttackers(player1, List.of(0));
        blade.setAttachedTo(otherCreature.getId());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The defending player reveals even when they control the Blade")
    void usesDefendingPlayerRatherThanBladeControllersOpponent() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent blade = addBladeReady(player2);
        blade.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest()));
        setDeckWithLandAtPosition(player2, 2);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("The power boost expires at the end of the turn")
    void boostExpiresAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent blade = addBladeReady(player1);
        blade.setAttachedTo(creature.getId());
        setDeckWithLandAtPosition(player2, 2);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);

        harness.passUntil(TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Equip cannot be activated with only one mana")
    void equipRequiresTwoMana() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(blade.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Equip cannot be activated during combat")
    void equipRequiresSorceryTiming() {
        Permanent blade = addBladeReady(player1);
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(blade.getAttachedTo()).isNull();
    }

    @Test
    @DisplayName("Each Blade reveals cards separately and adds its own boost")
    void multipleBladesTriggerIndependently() {
        Permanent creature = addCreatureReady(player1, new WalkingCorpse());
        Permanent firstBlade = addBladeReady(player1);
        Permanent secondBlade = addBladeReady(player1);
        firstBlade.setAttachedTo(creature.getId());
        secondBlade.setAttachedTo(creature.getId());
        harness.setLibrary(player2, List.of(
                new WalkingCorpse(), new Forest(),
                new WalkingCorpse(), new WalkingCorpse(), new Forest(),
                new WalkingCorpse()));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    private Permanent addBladeReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new TrepanationBlade());
    }

    /**
     * Creates a deck with {@code nonLandCount} creature cards followed by 1 land card.
     */
    private void setDeckWithLandAtPosition(Player player, int nonLandCount) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < nonLandCount; i++) {
            deck.add(new WalkingCorpse());
        }
        deck.add(new Forest());
        harness.setLibrary(player, deck);
    }

    /**
     * Creates a deck with only creature cards (no lands).
     */
    private void setDeckNoLands(Player player, int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new WalkingCorpse());
        }
        harness.setLibrary(player, deck);
    }
}
