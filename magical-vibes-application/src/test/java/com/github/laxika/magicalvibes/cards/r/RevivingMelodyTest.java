package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RevivingMelodyTest extends BaseCardTest {

    @Test
    void creatureModeReturnsCreatureToHand() {
        Card creature = new GrizzlyBears();
        Card enchantment = new Pacifism();
        harness.setGraveyard(player1, List.of(creature, enchantment));
        harness.setHand(player1, List.of(new RevivingMelody()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        List<UUID> selectedIds = new ArrayList<>(choice.validCardIds());
        harness.handleMultipleCardsChosen(player1, selectedIds);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Pacifism");
    }

    @Test
    void enchantmentModeReturnsEnchantmentToHand() {
        Card creature = new GrizzlyBears();
        Card enchantment = new Pacifism();
        harness.setGraveyard(player1, List.of(creature, enchantment));
        harness.setHand(player1, List.of(new RevivingMelody()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(enchantment.getId());
        harness.handleMultipleCardsChosen(player1, new ArrayList<>(choice.validCardIds()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Pacifism");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void bothModeReturnsCreatureAndEnchantmentToHand() {
        Card creature = new GrizzlyBears();
        Card enchantment = new Pacifism();
        harness.setGraveyard(player1, List.of(creature, enchantment));
        harness.setHand(player1, List.of(new RevivingMelody()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), enchantment.getId());
        assertThat(choice.maxCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, new ArrayList<>(choice.validCardIds()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Pacifism");
    }

    @Test
    void bothModeExcludesCardsThatAreNeitherCreaturesNorEnchantments() {
        Card creature = new GrizzlyBears();
        Card enchantment = new Pacifism();
        Card instant = new GiantGrowth();
        harness.setGraveyard(player1, List.of(creature, enchantment, instant));
        harness.setHand(player1, List.of(new RevivingMelody()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), enchantment.getId());
    }
}
