package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class SacrificeUnlessDiscardCardTypeEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Sacrifices immediately when no valid cards in hand")
            void sacrificesWhenNoValidCards() {
                Card card = createCard("Zombie Infestation");
                Permanent source = new Permanent(card);
                gd.playerBattlefields.get(player1Id).add(source);
                SacrificeUnlessDiscardCardTypeEffect effect = new SacrificeUnlessDiscardCardTypeEffect(CardType.CREATURE);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                // Hand has no creature cards
                Card landCard = createCard("Mountain");
                landCard.setType(CardType.LAND);
                gd.playerHands.get(player1Id).add(landCard);

                resolveEffect(gd, entry, effect);

                verify(permanentRemovalService).removePermanentToGraveyard(gd, source);
            }

            @Test
            @DisplayName("Presents may ability when valid cards are available")
            void presentsMayWhenValidCardsAvailable() {
                Card card = createCard("Zombie Infestation");
                Permanent source = new Permanent(card);
                gd.playerBattlefields.get(player1Id).add(source);
                SacrificeUnlessDiscardCardTypeEffect effect = new SacrificeUnlessDiscardCardTypeEffect(CardType.CREATURE);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));
                Card creatureCard = createCard("Grizzly Bears");
                creatureCard.setType(CardType.CREATURE);
                gd.playerHands.get(player1Id).add(creatureCard);

                resolveEffect(gd, entry, effect);

                assertThat(gd.pendingMayAbilities).isNotEmpty();
                verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
            }

            @Test
            @DisplayName("Does nothing when source already left battlefield and no valid cards")
            void doesNothingWhenSourceGoneAndNoValidCards() {
                Card card = createCard("Zombie Infestation");
                SacrificeUnlessDiscardCardTypeEffect effect = new SacrificeUnlessDiscardCardTypeEffect(CardType.CREATURE);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
            }
}
