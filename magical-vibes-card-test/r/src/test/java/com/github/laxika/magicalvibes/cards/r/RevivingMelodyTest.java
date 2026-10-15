package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AjanisPresence;
import com.github.laxika.magicalvibes.cards.g.GoldenHind;
import com.github.laxika.magicalvibes.cards.o.OakheartDryads;
import com.github.laxika.magicalvibes.cards.o.OppressiveRays;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RevivingMelody.class, GoldenHind.class, OppressiveRays.class, AjanisPresence.class, OakheartDryads.class})
class RevivingMelodyTest extends BaseCardTest {

    @Test
    void creatureModeReturnsCreatureToHand() {
        Card creature = new GoldenHind();
        Card enchantment = new OppressiveRays();
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

        harness.assertInHand(player1, "Golden Hind");
        harness.assertInGraveyard(player1, "Oppressive Rays");
    }

    @Test
    void enchantmentModeReturnsEnchantmentToHand() {
        Card creature = new GoldenHind();
        Card enchantment = new OppressiveRays();
        harness.setGraveyard(player1, List.of(creature, enchantment));
        harness.setHand(player1, List.of(new RevivingMelody()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 1);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(enchantment.getId());
        harness.handleMultipleCardsChosen(player1, new ArrayList<>(choice.validCardIds()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Oppressive Rays");
        harness.assertInGraveyard(player1, "Golden Hind");
    }

    @Test
    void bothModeReturnsCreatureAndEnchantmentToHand() {
        Card creature = new GoldenHind();
        Card enchantment = new OppressiveRays();
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

        harness.assertInHand(player1, "Golden Hind");
        harness.assertInHand(player1, "Oppressive Rays");
    }

    @Test
    void bothModeExcludesCardsThatAreNeitherCreaturesNorEnchantments() {
        Card creature = new GoldenHind();
        Card enchantment = new OppressiveRays();
        Card instant = new AjanisPresence();
        harness.setGraveyard(player1, List.of(creature, enchantment, instant));
        harness.setHand(player1, List.of(new RevivingMelody()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 2);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(creature.getId(), enchantment.getId());
    }

    @Test
    void bothModeRejectsTwoNonEnchantmentCreatures() {
        Card first = new GoldenHind();
        Card second = new GoldenHind();
        Card enchantment = new OppressiveRays();
        harness.setGraveyard(player1, List.of(first, second, enchantment));
        harness.setHand(player1, List.of(new RevivingMelody()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModeRejectsTwoNonCreatureEnchantments() {
        Card creature = new GoldenHind();
        Card first = new OppressiveRays();
        Card second = new OppressiveRays();
        harness.setGraveyard(player1, List.of(creature, first, second));
        harness.setHand(player1, List.of(new RevivingMelody()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModeCannotOmitEnchantmentTarget() {
        Card creature = new GoldenHind();
        Card enchantment = new OppressiveRays();
        harness.setGraveyard(player1, List.of(creature, enchantment));
        harness.setHand(player1, List.of(new RevivingMelody()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModeCannotOmitBothTargets() {
        harness.setGraveyard(player1, List.of(new GoldenHind(), new OppressiveRays()));
        harness.setHand(player1, List.of(new RevivingMelody()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 2);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModesCanTargetSameEnchantmentCreature() {
        Card creature = new OakheartDryads();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new RevivingMelody()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), creature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Oakheart Dryads");
        harness.assertNotInGraveyard(player1, "Oakheart Dryads");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void bothModeStillReturnsRemainingLegalTarget() {
        Card creature = new GoldenHind();
        Card enchantment = new OppressiveRays();
        harness.setGraveyard(player1, List.of(creature, enchantment));
        harness.setHand(player1, List.of(new RevivingMelody()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 2);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), enchantment.getId()));
        harness.setGraveyard(player1, List.of(enchantment));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Oppressive Rays");
        harness.assertNotInHand(player1, "Golden Hind");
    }

    @Test
    void creatureModeRequiresCreatureInOwnGraveyard() {
        harness.setGraveyard(player1, List.of(new OppressiveRays()));
        harness.setGraveyard(player2, List.of(new GoldenHind()));
        harness.setHand(player1, List.of(new RevivingMelody()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void enchantmentModeRequiresEnchantmentInOwnGraveyard() {
        harness.setGraveyard(player1, List.of(new GoldenHind()));
        harness.setGraveyard(player2, List.of(new OppressiveRays()));
        harness.setHand(player1, List.of(new RevivingMelody()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void bothModeRequiresBothKindsOfTarget() {
        harness.setGraveyard(player1, List.of(new GoldenHind(), new GoldenHind()));
        harness.setHand(player1, List.of(new RevivingMelody()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 2))
                .isInstanceOf(IllegalStateException.class);
    }
}
