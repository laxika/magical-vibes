package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.DamageBonusGrant;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostColorSourceDamageThisTurnEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BoostColorSourceDamageThisTurnEffectHandlerTest extends AbstractDamageHandlerTest {

    private BoostColorSourceDamageThisTurnEffectHandler boostColorSourceDamageThisTurnHandler;

    @Override
    protected void setUpHandler() {
        boostColorSourceDamageThisTurnHandler = new BoostColorSourceDamageThisTurnEffectHandler(gameLogService);
    }

    @Test
            @DisplayName("sets color source damage bonus for controller")
            void setsBonusForController() {
                Card card = createCard("The Flame of Keld");
                BoostColorSourceDamageThisTurnEffect effect = new BoostColorSourceDamageThisTurnEffect(CardColor.RED, 2);
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, player1Id,
                        "The Flame of Keld chapter III", new ArrayList<>(List.of(effect)), null);

                boostColorSourceDamageThisTurnHandler.resolve(gd, entry, effect);

                assertThat(gd.colorSourceDamageBonusThisTurn.get(player1Id))
                        .containsExactly(new DamageBonusGrant("The Flame of Keld", CardColor.RED, 2));
            }

            @Test
            @DisplayName("keeps each grant as its own effect")
            void keepsEachGrantSeparate() {
                GameData.addDamageBonusGrant(gd.colorSourceDamageBonusThisTurn, player1Id,
                        new DamageBonusGrant("The Flame of Keld", CardColor.RED, 2));

                Card card = createCard("Second Flame");
                BoostColorSourceDamageThisTurnEffect effect = new BoostColorSourceDamageThisTurnEffect(CardColor.RED, 3);
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, player1Id,
                        "Second Flame", new ArrayList<>(List.of(effect)), null);

                boostColorSourceDamageThisTurnHandler.resolve(gd, entry, effect);

                assertThat(gd.colorSourceDamageBonusThisTurn.get(player1Id)).containsExactly(
                        new DamageBonusGrant("The Flame of Keld", CardColor.RED, 2),
                        new DamageBonusGrant("Second Flame", CardColor.RED, 3));
            }

            @Test
            @DisplayName("does not affect other player's bonus")
            void doesNotAffectOtherPlayer() {
                Card card = createCard("The Flame of Keld");
                BoostColorSourceDamageThisTurnEffect effect = new BoostColorSourceDamageThisTurnEffect(CardColor.RED, 2);
                StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, player1Id,
                        "The Flame of Keld chapter III", new ArrayList<>(List.of(effect)), null);

                boostColorSourceDamageThisTurnHandler.resolve(gd, entry, effect);

                assertThat(gd.colorSourceDamageBonusThisTurn).doesNotContainKey(player2Id);
            }
}
