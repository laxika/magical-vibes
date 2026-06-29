package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EachPlayerDrawsCardEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Both players draw the specified number of cards")
            void bothPlayersDraw() {
                Card card = createCard("Howling Mine");
                EachPlayerDrawsCardEffect effect = new EachPlayerDrawsCardEffect(2);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(drawService, times(2)).resolveDrawCard(gd, player1Id);
                verify(drawService, times(2)).resolveDrawCard(gd, player2Id);
            }

            @Test
            @DisplayName("Each player draws 1 card")
            void eachPlayerDrawsOne() {
                Card card = createCard("Temple Bell");
                EachPlayerDrawsCardEffect effect = new EachPlayerDrawsCardEffect(1);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(drawService, times(1)).resolveDrawCard(gd, player1Id);
                verify(drawService, times(1)).resolveDrawCard(gd, player2Id);
            }
}
