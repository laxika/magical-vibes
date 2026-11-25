package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntry;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MayPayManaEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

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
