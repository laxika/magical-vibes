package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.ManaColor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LoseLifeUnlessPaysEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Auto-applies life loss when can't pay")
            void autoAppliesLifeLossWhenCantPay() {
                Card card = createCard("Rhystic Study");
                LoseLifeUnlessPaysEffect effect = new LoseLifeUnlessPaysEffect(2, 1, null);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(true);

                resolveEffect(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(18);
            }

            @Test
            @DisplayName("No life loss when life can't change and can't pay")
            void noLifeLossWhenLifeCantChange() {
                Card card = createCard("Rhystic Study");
                LoseLifeUnlessPaysEffect effect = new LoseLifeUnlessPaysEffect(2, 1, null);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);

                when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(false);

                resolveEffect(gd, entry, effect);

                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
            }

            @Test
            @DisplayName("Presents may ability when player can pay")
            void presentsMayAbilityWhenCanPay() {
                Card card = createCard("Rhystic Study");
                LoseLifeUnlessPaysEffect effect = new LoseLifeUnlessPaysEffect(2, 1, null);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), player2Id);
                // Add enough mana to pay {1}
                gd.playerManaPools.get(player2Id).add(com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);

                resolveEffect(gd, entry, effect);

                assertThat(gd.pendingMayAbilities).isNotEmpty();
                assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player2Id);
                assertThat(gd.playerLifeTotals.get(player2Id)).isEqualTo(20);
            }
}
