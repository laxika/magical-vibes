package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeasonOfRenewal.class, GrizzlyBears.class, Pacifism.class})
class SeasonOfRenewalTest extends BaseCardTest {

    @Test
    @DisplayName("Creature mode returns a creature card to hand")
    void creatureModeReturnsCreature() {
        Card creature = new GrizzlyBears();
        Card enchantment = new Pacifism();
        harness.setGraveyard(player1, List.of(creature, enchantment));
        castSeasonOfRenewal(0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, new ArrayList<>(choice.validCardIds()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Pacifism");
    }

    @Test
    @DisplayName("Enchantment mode returns an enchantment card to hand")
    void enchantmentModeReturnsEnchantment() {
        Card creature = new GrizzlyBears();
        Card enchantment = new Pacifism();
        harness.setGraveyard(player1, List.of(creature, enchantment));
        castSeasonOfRenewal(1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(enchantment.getId());
        harness.handleMultipleCardsChosen(player1, new ArrayList<>(choice.validCardIds()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Pacifism");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Both mode returns a creature and an enchantment card")
    void bothModeReturnsBothCards() {
        Card creature = new GrizzlyBears();
        Card enchantment = new Pacifism();
        harness.setGraveyard(player1, List.of(creature, enchantment));
        castSeasonOfRenewal(2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), enchantment.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, new ArrayList<>(choice.validCardIds()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Pacifism");
    }

    private void castSeasonOfRenewal(int mode) {
        harness.setHand(player1, List.of(new SeasonOfRenewal()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castModalInstant(player1, 0, mode, List.of());
    }
}
