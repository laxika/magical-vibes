package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostColorSourceDamageThisTurnEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.BoostColorSourceDamageThisTurnEffectHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BoostColorSourceDamageThisTurnEffectHandlerTest extends AbstractDamageHandlerTest {

    private BoostColorSourceDamageThisTurnEffectHandler boostColorSourceDamageThisTurnHandler;

    @Override
    protected void setUpHandler() {
        boostColorSourceDamageThisTurnHandler = new BoostColorSourceDamageThisTurnEffectHandler(gameBroadcastService);
    }

    @Test
            @DisplayName("sets color source damage bonus for controller")
            void setsBonusForController() {
                Card card = createCard("The Flame of Keld");
                BoostColorSourceDamageThisTurnEffect effect = new BoostColorSourceDamageThisTurnEffect(CardColor.RED, 2);
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, player1Id,
                        "The Flame of Keld chapter III", new ArrayList<>(List.of(effect)), null);

                boostColorSourceDamageThisTurnHandler.resolve(gd, entry, effect);

                assertThat(gd.colorSourceDamageBonusThisTurn.get(player1Id).get(CardColor.RED)).isEqualTo(2);
            }

            @Test
            @DisplayName("stacks additively with existing bonus")
            void stacksAdditively() {
                gd.colorSourceDamageBonusThisTurn
                        .computeIfAbsent(player1Id, k -> new java.util.concurrent.ConcurrentHashMap<>())
                        .put(CardColor.RED, 2);

                Card card = createCard("Second Flame");
                BoostColorSourceDamageThisTurnEffect effect = new BoostColorSourceDamageThisTurnEffect(CardColor.RED, 3);
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, player1Id,
                        "Second Flame", new ArrayList<>(List.of(effect)), null);

                boostColorSourceDamageThisTurnHandler.resolve(gd, entry, effect);

                assertThat(gd.colorSourceDamageBonusThisTurn.get(player1Id).get(CardColor.RED)).isEqualTo(5);
            }

            @Test
            @DisplayName("does not affect other player's bonus")
            void doesNotAffectOtherPlayer() {
                Card card = createCard("The Flame of Keld");
                BoostColorSourceDamageThisTurnEffect effect = new BoostColorSourceDamageThisTurnEffect(CardColor.RED, 2);
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, player1Id,
                        "The Flame of Keld chapter III", new ArrayList<>(List.of(effect)), null);

                boostColorSourceDamageThisTurnHandler.resolve(gd, entry, effect);

                assertThat(gd.colorSourceDamageBonusThisTurn.getOrDefault(player2Id, java.util.Map.of()))
                        .doesNotContainKey(CardColor.RED);
            }
}
