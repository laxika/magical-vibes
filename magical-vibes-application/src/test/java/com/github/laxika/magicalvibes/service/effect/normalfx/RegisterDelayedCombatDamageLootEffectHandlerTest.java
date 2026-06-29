package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedCombatDamageLootEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class RegisterDelayedCombatDamageLootEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Registers delayed loot trigger with correct values")
            void registersDelayedLoot() {
                Card card = createCard("Looter il-Kor");
                RegisterDelayedCombatDamageLootEffect effect = new RegisterDelayedCombatDamageLootEffect(1, 1);
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                resolveEffect(gd, entry, effect);

                assertThat(gd.pendingDelayedCombatDamageLoots).hasSize(1);
                GameData.DelayedCombatDamageLoot loot = gd.pendingDelayedCombatDamageLoots.getFirst();
                assertThat(loot.controllerId()).isEqualTo(player1Id);
                assertThat(loot.drawAmount()).isEqualTo(1);
                assertThat(loot.discardAmount()).isEqualTo(1);
            }
}
