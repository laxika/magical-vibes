package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MayPayManaEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Sets resolvingMayEffectFromStack flag and adds pending may with mana cost")
            void setsFlagAndAddsPendingMayWithMana() {
                Card card = createCard("Rhystic Study");
                DrawCardEffect wrapped = new DrawCardEffect(1);
                MayPayManaEffect mayPayEffect = new MayPayManaEffect("{1}", wrapped, "Pay {1}?");
                StackEntry entry = createEntry(card, player1Id, List.of(mayPayEffect));

                resolveEffect(gd, entry, mayPayEffect);

                assertThat(gd.resolvingMayEffectFromStack).isTrue();
                assertThat(gd.pendingMayAbilities).hasSize(1);
                assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{1}");
                assertThat(gd.pendingMayAbilities.getFirst().effects()).containsExactly(wrapped);
                // Default: the ability's controller is the one prompted to pay.
                assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player1Id);
            }

    @Test
    @DisplayName("payerIsEnchantedController prompts the stack entry's targetId, not the controller")
    void payerIsEnchantedControllerPromptsTargetPlayer() {
        Card card = createCard("Paralyze");
        DrawCardEffect wrapped = new DrawCardEffect(1);
        MayPayManaEffect mayPayEffect = new MayPayManaEffect("{4}", wrapped, "Pay {4}?", true);
        // controller = aura owner (player1), targetId = enchanted creature's controller (player2)
        StackEntry entry = createEntryWithTarget(card, player1Id, List.of(mayPayEffect), player2Id);

        resolveEffect(gd, entry, mayPayEffect);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player2Id);
    }
}
