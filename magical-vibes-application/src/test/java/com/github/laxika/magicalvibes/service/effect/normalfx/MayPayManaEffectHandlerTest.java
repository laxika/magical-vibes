package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TransformSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;

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
    @DisplayName("Substitutes dynamic generic mana while retaining fixed mana symbols")
    void substitutesDynamicGenericManaWithFixedSymbols() {
        Card card = createCard("Tomakul Phoenix");
        DrawCardEffect wrapped = new DrawCardEffect(1);
        MayPayManaEffect mayPayEffect = MayPayManaEffect.dynamic("{X}{R}", new Fixed(2), wrapped,
                "Pay {X}{R}?");
        StackEntry entry = createEntry(card, player1Id, List.of(mayPayEffect));

        resolveEffect(gd, entry, mayPayEffect);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isEqualTo("{2}{R}");
    }

    @Test
    @DisplayName("ANY_OTHER_PLAYER skips the player whose spell caused the trigger")
    void anyOtherPlayerSkipsTriggeringPlayer() {
        Card card = createCard("Ice Cave");
        DrawCardEffect wrapped = new DrawCardEffect(1);
        MayPayManaEffect mayPayEffect = new MayPayManaEffect("{1}", wrapped, "Pay {1}?",
                MayPayPayer.ANY_OTHER_PLAYER);
        StackEntry entry = createEntry(card, player1Id, List.of(mayPayEffect));
        // The cast trigger's entry: player2 cast the spell that triggered it.
        entry.setTriggeringSpell(new StackEntry(createCard("Triggering spell"), player2Id));
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
    @DisplayName("TARGET_PERMANENT_CONTROLLER uses the damaged creature's last known controller once it has died")
    void usesLastKnownControllerWhenTheDamagedCreatureHasDied() {
        Card card = createCard("Soul Charmer");
        MayPayManaEffect effect = new MayPayManaEffect("{2}", new DrawCardEffect(1), "Pay {2}?",
                MayPayPayer.TARGET_PERMANENT_CONTROLLER);
        UUID damagedCreatureId = UUID.randomUUID();
        StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), damagedCreatureId);
        entry.setTriggeringPermanentId(damagedCreatureId);
        entry.setTriggeringPermanentControllerId(player2Id);

        resolveEffect(gd, entry, effect);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player2Id);
    }

    @Test
    @DisplayName("TARGET_PERMANENT_CONTROLLER uses the damaged creature's current controller after a control change")
    void usesCurrentControllerWhenTheDamagedCreatureHasChangedControl() {
        Card card = createCard("Soul Charmer");
        MayPayManaEffect effect = new MayPayManaEffect("{2}", new DrawCardEffect(1), "Pay {2}?",
                MayPayPayer.TARGET_PERMANENT_CONTROLLER);
        UUID damagedCreatureId = UUID.randomUUID();
        StackEntry entry = createEntryWithTarget(card, player1Id, List.of(effect), damagedCreatureId);
        entry.setTriggeringPermanentId(damagedCreatureId);
        entry.setTriggeringPermanentControllerId(player2Id);
        when(gameQueryService.findPermanentController(gd, damagedCreatureId)).thenReturn(player1Id);

        resolveEffect(gd, entry, effect);

        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player1Id);
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

    @Test
    @DisplayName("A non-mana payment offered to any opponent asks the first opponent, carrying who is left to ask")
    void nonManaPaymentAsksTheFirstOpponent() {
        Card card = createCard("Vexing Devil");
        MayPayManaEffect payment = MayPayManaEffect.anyPlayerMay(MayPayPayer.ANY_OPPONENT,
                new DealDamageToPlayersEffect(4, Subjects.acceptedPlayers()), "Take 4?", new SacrificeSelfEffect(),
                null);
        StackEntry entry = createEntry(card, player1Id, List.of(payment));
        gd.activePlayerId = player1Id;

        resolveEffect(gd, entry, payment);

        assertThat(gd.resolvingMayEffectFromStack).isTrue();
        assertThat(gd.pendingMayAbilities).hasSize(1);
        assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player2Id);
        assertThat(gd.pendingMayAbilities.getFirst().manaCost()).isNull();
        MayPayManaEffect asking = (MayPayManaEffect) gd.pendingMayAbilities.getFirst().effects().getFirst();
        assertThat(asking.loop().remainingPayerIds()).containsExactly(player2Id);
        assertThat(asking.loop().acceptedPayerIds()).isEmpty();
    }

    @Test
    @DisplayName("A player who couldn't sacrifice isn't asked, and with nobody left \"if no one does\" is the next step")
    void playerWhoCannotPayIsSkippedAndTheElseEffectFollows() {
        Card card = createCard("Innocent Traveler");
        SacrificePermanentsEffect sacrifice = new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(),
                Subjects.acceptedPlayers());
        TransformSelfEffect transform = new TransformSelfEffect();
        MayPayManaEffect payment = MayPayManaEffect.anyPlayerMay(MayPayPayer.ANY_OPPONENT, sacrifice,
                "Sacrifice a creature?", null, transform);
        StackEntry entry = createEntry(card, player1Id, List.of(payment));
        entry.setResolvingEffectIndex(0);
        when(sacrificePermanentsEffectHandler.hasLegalSacrificeChoice(eq(gd), eq(entry), eq(sacrifice), eq(player2Id)))
                .thenReturn(false);

        resolveEffect(gd, entry, payment);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(entry.getEffectsToResolve()).containsExactly(payment, transform);
    }

    @Test
    @DisplayName("Paid as each accepts, an accepting player pays before the next player is asked, \"if a player does\" after it")
    void payingAsEachAcceptsPaysTheAcceptorFirst() {
        Card card = createCard("Dash Hopes");
        DealDamageToPlayersEffect damage = new DealDamageToPlayersEffect(4, Subjects.acceptedPlayers());
        DrawCardEffect ifAPlayerDoes = new DrawCardEffect(1);
        MayPayManaEffect payment = MayPayManaEffect.anyPlayerMay(MayPayPayer.ANY_PLAYER, damage, "Take 4?",
                ifAPlayerDoes, new SacrificeSelfEffect()).payingAsEachAccepts();
        MayPayManaEffect answered = payment.asking(List.of(player2Id), List.of(player1Id), List.of());
        StackEntry entry = createEntry(card, player1Id, List.of(answered));
        entry.setResolvingEffectIndex(0);

        resolveEffect(gd, entry, answered);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(entry.getAcceptedPlayerIds()).containsExactly(player1Id);
        assertThat(entry.getEffectsToResolve()).hasSize(4);
        assertThat(entry.getEffectsToResolve().subList(1, 3)).containsExactly(damage, ifAPlayerDoes);
        MayPayManaEffect next = (MayPayManaEffect) entry.getEffectsToResolve().get(3);
        assertThat(next.loop().remainingPayerIds()).containsExactly(player2Id);
        assertThat(next.loop().acceptedPayerIds()).isEmpty();
        assertThat(next.loop().anyPaid()).isTrue();
    }

    @Test
    @DisplayName("Paid as each accepts, a later acceptor pays without \"if a player does\" again, and no \"if no one does\" follows")
    void payingAsEachAcceptsRunsTheOutcomeOnce() {
        Card card = createCard("Dash Hopes");
        DealDamageToPlayersEffect damage = new DealDamageToPlayersEffect(4, Subjects.acceptedPlayers());
        SacrificeSelfEffect ifNoOneDoes = new SacrificeSelfEffect();
        MayPayManaEffect payment = MayPayManaEffect.anyPlayerMay(MayPayPayer.ANY_PLAYER, damage, "Take 4?",
                new DrawCardEffect(1), ifNoOneDoes).payingAsEachAccepts();
        MayPayManaEffect secondAccepted = payment.asking(List.of(), List.of(player2Id), List.of(), true);
        StackEntry entry = createEntry(card, player1Id, List.of(secondAccepted));
        entry.setResolvingEffectIndex(0);

        resolveEffect(gd, entry, secondAccepted);

        assertThat(entry.getEffectsToResolve()).hasSize(3);
        assertThat(entry.getEffectsToResolve().get(1)).isEqualTo(damage);
        MayPayManaEffect last = (MayPayManaEffect) entry.getEffectsToResolve().get(2);
        entry.setResolvingEffectIndex(2);

        resolveEffect(gd, entry, last);

        assertThat(gd.pendingMayAbilities).isEmpty();
        assertThat(entry.getEffectsToResolve()).hasSize(3).doesNotContain(ifNoOneDoes);
    }
}
