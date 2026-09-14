package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.t.TaintedField;
import com.github.laxika.magicalvibes.cards.t.TerohsFaithful;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GhostlyWings.class, TerohsFaithful.class, TaintedField.class})
class GhostlyWingsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Ghostly Wings attaches it and grants +1/+1 and flying")
    void resolvingAttachesAndGrantsEffects() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TerohsFaithful());

        GhostlyWings wings = new GhostlyWings();
        harness.setHand(player1, List.of(wings));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(wings.getId())
                        && p.isAttached()
                        && creature.getId().equals(p.getAttachedTo()));
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Ghostly Wings affects only its enchanted creature")
    void affectsOnlyEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TerohsFaithful());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new TerohsFaithful());

        Permanent wings = harness.addToBattlefieldAndReturn(player1, new GhostlyWings());
        wings.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Discarding a card returns the enchanted creature to its owner's hand")
    void discardingBouncesEnchantedCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TerohsFaithful());

        Permanent wings = harness.addToBattlefieldAndReturn(player1, new GhostlyWings());
        wings.setAttachedTo(creature.getId());
        TaintedField discarded = new TaintedField();
        harness.setHand(player1, List.of(discarded));

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(wings.getCard().getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(creature.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(discarded.getId()));
    }

    @Test
    @DisplayName("The selected card is discarded when multiple cards are available")
    void discardsSelectedCard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TerohsFaithful());
        Permanent wings = harness.addToBattlefieldAndReturn(player1, new GhostlyWings());
        wings.setAttachedTo(creature.getId());
        TaintedField kept = new TaintedField();
        TaintedField discarded = new TaintedField();
        harness.setHand(player1, List.of(kept, discarded));

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, creature.getCard());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(discarded.getId()));
    }

    @Test
    @DisplayName("Returns the creature enchanted immediately before Ghostly Wings leaves")
    void returnsCreatureIfWingsLeavesBeforeResolution() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TerohsFaithful());
        Permanent wings = harness.addToBattlefieldAndReturn(player1, new GhostlyWings());
        wings.setAttachedTo(creature.getId());
        TaintedField discarded = new TaintedField();
        harness.setHand(player1, List.of(discarded));

        harness.activateAbility(player1, 1, null, null);
        harness.handleCardChosen(player1, 0);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().tryDestroyPermanent(gd, wings));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(creature.getCard().getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(wings.getCard().getId()));
    }

    @Test
    @DisplayName("The bounced creature returns to its owner's hand")
    void returnsToCreatureOwnersHand() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new TerohsFaithful());

        Permanent wings = harness.addToBattlefieldAndReturn(player1, new GhostlyWings());
        wings.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new TaintedField()));

        harness.activateAbility(player1, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card.getId().equals(creature.getCard().getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(creature.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot activate without a card to discard")
    void cannotActivateWithoutCardToDiscard() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new TerohsFaithful());

        Permanent wings = harness.addToBattlefieldAndReturn(player1, new GhostlyWings());
        wings.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        harness.addToBattlefield(player2, new TerohsFaithful());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new TaintedField());
        harness.setHand(player1, List.of(new GhostlyWings()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
