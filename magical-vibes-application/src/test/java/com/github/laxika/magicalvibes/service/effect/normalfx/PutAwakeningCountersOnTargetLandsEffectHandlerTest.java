package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.PutAwakeningCountersOnTargetLandsEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PutAwakeningCountersOnTargetLandsEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Begins multi-permanent choice when controller has lands")
            void beginsChoiceWithLands() {
                Card card = createCard("Embodiment of Insight");
                PutAwakeningCountersOnTargetLandsEffect effect = new PutAwakeningCountersOnTargetLandsEffect();
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                Card landCard = createCard("Forest");
                landCard.setType(CardType.LAND);
                Permanent land = new Permanent(landCard);
                gd.playerBattlefields.get(player1Id).add(land);

                resolveEffect(gd, entry, new PutAwakeningCountersOnTargetLandsEffect());

                verify(playerInputService).beginMultiPermanentChoice(eq(gd), eq(player1Id),
                        argThat(ids -> ids.size() == 1 && ids.contains(land.getId())), eq(1),
                        eq(new MultiPermanentChoiceContext.AwakeningCounterPlacement()), any());
            }

            @Test
            @DisplayName("Logs and does nothing when controller has no lands")
            void noLands() {
                Card card = createCard("Embodiment of Insight");
                PutAwakeningCountersOnTargetLandsEffect effect = new PutAwakeningCountersOnTargetLandsEffect();
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                // Only a non-land on battlefield
                Card creatureCard = createCard("Grizzly Bears");
                creatureCard.setType(CardType.CREATURE);
                Permanent creature = new Permanent(creatureCard);
                gd.playerBattlefields.get(player1Id).add(creature);

                resolveEffect(gd, entry, new PutAwakeningCountersOnTargetLandsEffect());

                verify(playerInputService, never()).beginMultiPermanentChoice(any(), any(), any(), any(int.class), any(), any());
                verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("no lands")));
            }
}
