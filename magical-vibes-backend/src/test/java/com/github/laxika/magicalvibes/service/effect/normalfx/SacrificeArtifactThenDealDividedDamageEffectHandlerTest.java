package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactThenDealDividedDamageEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SacrificeArtifactThenDealDividedDamageEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Begins permanent choice when controller has artifacts")
            void beginsChoiceWithArtifacts() {
                Card card = createCard("Shrapnel Blast");
                SacrificeArtifactThenDealDividedDamageEffect effect = new SacrificeArtifactThenDealDividedDamageEffect(5);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.pendingETBDamageAssignments = new HashMap<>(Map.of(player2Id, 5));

                Permanent artifact = new Permanent(createCard("Sol Ring"));
                gd.playerBattlefields.get(player1Id).add(artifact);

                when(gameQueryService.isArtifact(artifact)).thenReturn(true);

                resolveEffect(gd, entry, effect);

                verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id), any(), any());
            }

            @Test
            @DisplayName("Logs and clears assignments when no artifacts available")
            void noArtifacts() {
                Card card = createCard("Shrapnel Blast");
                SacrificeArtifactThenDealDividedDamageEffect effect = new SacrificeArtifactThenDealDividedDamageEffect(5);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                gd.pendingETBDamageAssignments = new HashMap<>(Map.of(player2Id, 5));

                resolveEffect(gd, entry, effect);

                verify(playerInputService, never()).beginPermanentChoice(any(), any(), any(), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                        msg.contains("no artifacts")));
                assertThat(gd.pendingETBDamageAssignments).isEmpty();
            }
}
