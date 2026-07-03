package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerDiscardsEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TargetSpellControllerDiscardsEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Discards for the controller of the targeted spell")
            void discardsForSpellController() {
                Card sourceCard = createCard("Counterspell");
                Card targetSpellCard = createCard("Lightning Bolt");

                StackEntry targetSpell = new StackEntry(StackEntryType.INSTANT_SPELL, targetSpellCard,
                        player2Id, targetSpellCard.getName(), List.of());
                gd.stack.add(targetSpell);

                TargetSpellControllerDiscardsEffect effect = new TargetSpellControllerDiscardsEffect(1);
                StackEntry entry = createEntryWithTarget(sourceCard, player1Id, List.of(effect), targetSpellCard.getId());
                gd.playerHands.get(player2Id).add(createCard("Mountain"));

                resolveEffect(gd, entry, effect);

                assertThat(gd.discardCausedByOpponent).isTrue();
                verify(playerInputService).beginDiscardChoice(eq(gd), eq(player2Id), anyInt());
            }

            @Test
            @DisplayName("Does nothing when target spell not on stack")
            void doesNothingWhenTargetNotOnStack() {
                Card sourceCard = createCard("Counterspell");
                TargetSpellControllerDiscardsEffect effect = new TargetSpellControllerDiscardsEffect(1);
                StackEntry entry = createEntryWithTarget(sourceCard, player1Id, List.of(effect), UUID.randomUUID());

                resolveEffect(gd, entry, effect);

                verify(playerInputService, never()).beginDiscardChoice(any(), any(), anyInt());
            }

            @Test
            @DisplayName("Does nothing when target ID is null")
            void doesNothingWhenTargetIdNull() {
                Card sourceCard = createCard("Counterspell");
                TargetSpellControllerDiscardsEffect effect = new TargetSpellControllerDiscardsEffect(1);
                StackEntry entry = createEntry(sourceCard, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(playerInputService, never()).beginDiscardChoice(any(), any(), anyInt());
            }
}
