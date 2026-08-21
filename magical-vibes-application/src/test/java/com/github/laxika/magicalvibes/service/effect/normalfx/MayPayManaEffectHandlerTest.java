package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
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
    @DisplayName("ANY_OTHER_PLAYER skips the player whose spell caused the trigger")
    void anyOtherPlayerSkipsTriggeringPlayer() {
        Card card = createCard("Ice Cave");
        DrawCardEffect wrapped = new DrawCardEffect(1);
        MayPayManaEffect mayPayEffect = new MayPayManaEffect("{1}", wrapped, "Pay {1}?",
                MayPayPayer.ANY_OTHER_PLAYER);
        StackEntry entry = createEntry(card, player1Id, List.of(mayPayEffect));
        entry.setActivePlayerId(player2Id);
        gd.activePlayerId = player2Id;

        resolveEffect(gd, entry, mayPayEffect);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player1Id);
        assertThat(gd.anyPlayerMayPayManaRemainingPlayers).isEmpty();
    }

    @Test
    @DisplayName("ENCHANTED_CONTROLLER payer prompts the stack entry's targetId, not the controller")
    void enchantedControllerPayerPromptsTargetPlayer() {
        Card card = createCard("Paralyze");
        DrawCardEffect wrapped = new DrawCardEffect(1);
        MayPayManaEffect mayPayEffect = new MayPayManaEffect("{4}", wrapped, "Pay {4}?", MayPayPayer.ENCHANTED_CONTROLLER);
        // controller = aura owner (player1), targetId = enchanted creature's controller (player2)
        StackEntry entry = createEntryWithTarget(card, player1Id, List.of(mayPayEffect), player2Id);

        resolveEffect(gd, entry, mayPayEffect);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player2Id);
    }

    @Test
    @DisplayName("TARGET_PERMANENT_CONTROLLER payer prompts the controller of the targeted permanent")
    void targetPermanentControllerPayerPromptsThatPermanentsController() {
        Card card = createCard("Chain Stasis");
        DrawCardEffect wrapped = new DrawCardEffect(1);
        MayPayManaEffect mayPayEffect = new MayPayManaEffect("{2}{U}", wrapped, "Pay {2}{U}?", MayPayPayer.TARGET_PERMANENT_CONTROLLER);
        UUID targetPermanentId = UUID.randomUUID();
        StackEntry entry = createEntryWithTarget(card, player1Id, List.of(mayPayEffect), targetPermanentId);
        when(gameQueryService.findPermanentController(gd, targetPermanentId)).thenReturn(player2Id);

        resolveEffect(gd, entry, mayPayEffect);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player2Id);
    }

    @Test
    @DisplayName("TARGET_PLAYER_OR_PERMANENT_CONTROLLER payer prompts a target player")
    void targetPlayerOrPermanentControllerPayerPromptsTargetPlayer() {
        Card card = createCard("Rhystic Lightning");
        DrawCardEffect wrapped = new DrawCardEffect(1);
        MayPayManaEffect mayPayEffect = new MayPayManaEffect("{2}", wrapped, "Pay {2}?",
                MayPayPayer.TARGET_PLAYER_OR_PERMANENT_CONTROLLER);
        StackEntry entry = createEntryWithTarget(card, player1Id, List.of(mayPayEffect), player2Id);

        resolveEffect(gd, entry, mayPayEffect);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player2Id);
    }

    @Test
    @DisplayName("TRIGGERING_PLAYER payer prompts the player whose action caused the trigger")
    void triggeringPlayerPayerPromptsTriggeringPlayer() {
        Card card = createCard("Unifying Theory");
        DrawCardEffect wrapped = new DrawCardEffect(1);
        MayPayManaEffect mayPayEffect = new MayPayManaEffect("{2}", wrapped, "Pay {2}?",
                MayPayPayer.TRIGGERING_PLAYER);
        StackEntry entry = createEntryWithTarget(card, player1Id, List.of(mayPayEffect), player2Id);

        resolveEffect(gd, entry, mayPayEffect);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player2Id);
    }

    @Test
    @DisplayName("An additional life cost is carried onto the pending may ability")
    void additionalLifeCostIsCarried() {
        Card card = createCard("Purgatory");
        DrawCardEffect wrapped = new DrawCardEffect(1);
        MayPayManaEffect mayPayEffect = new MayPayManaEffect("{4}", 2, wrapped, "Pay {4} and 2 life?");
        StackEntry entry = createEntry(card, player1Id, List.of(mayPayEffect));

        resolveEffect(gd, entry, mayPayEffect);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{4}");
        assertThat(gd.pendingMayAbilities.getFirst().additionalLifeCost()).isEqualTo(2);
        // Distinct from the "pay {M} or N life" cost, which stays untouched.
        assertThat(gd.pendingMayAbilities.getFirst().lifeCost()).isZero();
    }
}
