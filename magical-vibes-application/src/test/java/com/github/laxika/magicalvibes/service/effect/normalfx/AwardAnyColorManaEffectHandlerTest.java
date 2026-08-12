package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.ManaSpendRestriction;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AwardAnyColorManaEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
    @DisplayName("Sends color choice to controller")
    void sendsColorChoice() {
        Card card = createCard("Birds of Paradise");
        AwardAnyColorManaEffect effect = new AwardAnyColorManaEffect(1);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        resolveEffect(gd, entry, effect);

        verify(interactionHandlerRegistry).begin(eq(gd), any(PendingInteraction.ColorChoice.class));
    }

    @Test
    @DisplayName("The instant/sorcery-copy restriction also registers the delayed copy trigger")
    void registersSpellCopyTrigger() {
        Card card = createCard("Unexpected Windfall");
        AwardAnyColorManaEffect effect =
                new AwardAnyColorManaEffect(2, ManaSpendRestriction.INSTANT_SORCERY_COPY);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        resolveEffect(gd, entry, effect);

        verify(interactionHandlerRegistry).begin(eq(gd), any(PendingInteraction.ColorChoice.class));
        assertThat(gd.pendingNextInstantSorceryCopyCount.get(player1Id)).isEqualTo(1);
    }

    @Test
    @DisplayName("A chosen-subtype restriction prompts for nothing without a source permanent")
    void chosenSubtypeWithoutSourceAddsNothing() {
        Card card = createCard("Pillar of Origins");
        AwardAnyColorManaEffect effect =
                new AwardAnyColorManaEffect(1, ManaSpendRestriction.CHOSEN_SUBTYPE_CREATURE);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        resolveEffect(gd, entry, effect);

        verify(interactionHandlerRegistry, never()).begin(eq(gd), any(PendingInteraction.ColorChoice.class));
    }

    @Test
    @DisplayName("A chosen-subtype spell-or-ability restriction prompts for nothing without a source permanent")
    void chosenSubtypeSpellOrAbilityWithoutSourceAddsNothing() {
        Card card = createCard("Eclipsed Realms");
        AwardAnyColorManaEffect effect =
                new AwardAnyColorManaEffect(1, ManaSpendRestriction.CHOSEN_SUBTYPE_SPELL_OR_ABILITY);
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        resolveEffect(gd, entry, effect);

        verify(interactionHandlerRegistry, never()).begin(eq(gd), any(PendingInteraction.ColorChoice.class));
    }
}
