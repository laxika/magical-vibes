package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.RedirectDrawsEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RedirectDrawsEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Sets up draw replacement mapping")
            void setsUpDrawReplacement() {
                Card card = createCard("Notion Thief");
                RedirectDrawsEffect effect = new RedirectDrawsEffect();
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                resolveEffect(gd, entry, new RedirectDrawsEffect());

                assertThat(gd.drawReplacementTargetToController).containsEntry(player2Id, player1Id);
            }

            @Test
            @DisplayName("Does nothing when target player not found")
            void doesNothingWhenTargetNotFound() {
                Card card = createCard("Notion Thief");
                RedirectDrawsEffect effect = new RedirectDrawsEffect();
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), UUID.randomUUID());

                resolveEffect(gd, entry, new RedirectDrawsEffect());

                assertThat(gd.drawReplacementTargetToController).isEmpty();
            }
}
