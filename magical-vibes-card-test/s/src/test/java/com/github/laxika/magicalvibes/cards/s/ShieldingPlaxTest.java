package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cytoshape;
import com.github.laxika.magicalvibes.cards.g.GlaringSpotlight;
import com.github.laxika.magicalvibes.cards.m.MistralCharger;
import com.github.laxika.magicalvibes.cards.o.Omnibian;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ShieldingPlax.class, MistralCharger.class, Cytoshape.class, Omnibian.class,
        GlaringSpotlight.class})
class ShieldingPlaxTest extends BaseCardTest {

    @Test
    @DisplayName("Shielding Plax draws a card when it enters attached to a creature")
    void drawsCardWhenItEnters() {
        Permanent creature = addReadyCreature(player1);
        MistralCharger libraryCard = new MistralCharger();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new ShieldingPlax()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(libraryCard.getId()));
        assertThat(harness.getGameData().playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof ShieldingPlax
                        && creature.getId().equals(permanent.getAttachedTo()));
    }

    @Test
    @DisplayName("Enchanted creature can't be targeted by an opponent's spell")
    void opponentSpellCannotTargetEnchantedCreature() {
        Permanent creature = addShieldingPlax(player1, player1);

        harness.setHand(player2, List.of(new Cytoshape()));
        addCytoshapeMana(player2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be targeted");
    }

    @Test
    @DisplayName("Enchanted creature can't be targeted by an opponent's ability")
    void opponentAbilityCannotTargetEnchantedCreature() {
        Permanent creature = addShieldingPlax(player1, player1);
        Permanent omnibian = addReadyOmnibian(player2);
        addOmnibianMana(player2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be targeted");
        assertThat(omnibian.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Enchanted creature can still be targeted by its controller's spell")
    void controllerCanTargetEnchantedCreature() {
        Permanent creature = addShieldingPlax(player1, player1);

        harness.setHand(player1, List.of(new Cytoshape()));
        addCytoshapeMana(player1);
        harness.castInstant(player1, 0, creature.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("The enchanted creature's controller is an opponent of the Aura's controller")
    void enchantedCreatureControllerCannotTargetWhenAuraIsControlledByOpponent() {
        Permanent creature = addShieldingPlax(player2, player1);

        harness.setHand(player2, List.of(new Cytoshape()));
        addCytoshapeMana(player2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be targeted");
    }

    @Test
    @DisplayName("The Aura's controller can target an enchanted creature controlled by an opponent")
    void auraControllerCanTargetOpponentCreature() {
        Permanent creature = addShieldingPlax(player2, player1);

        harness.setHand(player1, List.of(new Cytoshape()));
        addCytoshapeMana(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player1, 0, creature.getId());

        assertThat(harness.getGameData().stack).hasSize(1);
    }

    @Test
    @DisplayName("Glaring Spotlight does not bypass Shielding Plax's targeting restriction")
    void hexproofBypassDoesNotBypassShieldingPlax() {
        Permanent creature = addShieldingPlax(player1, player1);
        harness.addToBattlefield(player2, new GlaringSpotlight());

        harness.setHand(player2, List.of(new Cytoshape()));
        addCytoshapeMana(player2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castInstant(player2, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be targeted");
    }

    @Test
    @DisplayName("Enchanting an opponent's creature draws for the Aura's controller")
    void drawsForAuraControllerWhenEnchantingOpponentCreature() {
        Permanent creature = addReadyCreature(player2);
        MistralCharger libraryCard = new MistralCharger();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new ShieldingPlax()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(libraryCard);
        assertThat(findPermanent(player1, "Shielding Plax").getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("An Aura with a missing target does not enter or draw a card")
    void missingTargetPreventsEntryAndDraw() {
        Permanent creature = addReadyCreature(player1);
        MistralCharger libraryCard = new MistralCharger();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setHand(player1, List.of(new ShieldingPlax()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        gd.playerGraveyards.get(player1.getId()).add(creature.getCard());
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Shielding Plax");
        harness.assertInGraveyard(player1, "Shielding Plax");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Protection gained after activation makes an opponent's ability fail to resolve")
    void protectionInvalidatesAbilityAlreadyOnStack() {
        Permanent creature = addReadyCreature(player1);
        addReadyOmnibian(player2);
        addOmnibianMana(player2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player2, 0, null, creature.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new ShieldingPlax());
        aura.setAttachedTo(creature.getId());

        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addShieldingPlax(Player creatureController, Player auraController) {
        Permanent creature = addReadyCreature(creatureController);
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new ShieldingPlax());
        aura.setAttachedTo(creature.getId());
        return creature;
    }

    private Permanent addReadyCreature(Player player) {
        return addCreatureReady(player, new MistralCharger());
    }

    private Permanent addReadyOmnibian(Player player) {
        return addCreatureReady(player, new Omnibian());
    }

    private void addCytoshapeMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
    }

    private void addOmnibianMana(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.addMana(player, ManaColor.GREEN, 2);
        harness.addMana(player, ManaColor.BLUE, 1);
    }
}
