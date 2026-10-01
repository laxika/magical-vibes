package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.DrudgeReavers;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EternitySnare.class, DrudgeReavers.class, PrismaticLens.class})
class EternitySnareTest extends BaseCardTest {

    @Test
    @DisplayName("When Eternity Snare enters, its controller draws a card")
    void drawsCardWhenItEnters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DrudgeReavers());
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new EternitySnare()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = enchantOpponentCreature();
        creature.tap();

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Only the enchanted creature stays tapped during its controller's untap step")
    void onlyEnchantedCreatureDoesNotUntap() {
        Permanent enchantedCreature = harness.addToBattlefieldAndReturn(player2, new DrudgeReavers());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player2, new DrudgeReavers());
        enchantedCreature.tap();
        otherCreature.tap();

        harness.setHand(player1, List.of(new EternitySnare()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castEnchantment(player1, 0, enchantedCreature.getId());
        resolveAllTriggers();

        advanceToUpkeep(player2);

        assertThat(enchantedCreature.isTapped()).isTrue();
        assertThat(otherCreature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Eternity Snare cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new PrismaticLens());

        harness.setHand(player1, List.of(new EternitySnare()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent enchantOpponentCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new DrudgeReavers());

        harness.setHand(player1, List.of(new EternitySnare()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        return creature;
    }
}
