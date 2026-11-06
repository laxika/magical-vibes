package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

class SacrificePermanentThenEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Begins permanent choice when matching permanents exist")
            void beginsChoiceWithMatching() {
                Card card = createCard("Goblin Bombardment");
                PermanentPredicate filter = new PermanentTruePredicate();
                DrawCardEffect thenEffect = new DrawCardEffect(1);
                SacrificePermanentThenEffect effect = new SacrificePermanentThenEffect(filter, thenEffect, "a creature");
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                Permanent creature = new Permanent(createCard("Grizzly Bears"));
                gd.playerBattlefields.get(player1Id).add(creature);

                when(predicateEvaluationService.matchesPermanentPredicate(eq(creature), eq(filter), any())).thenReturn(true);

                resolveEffect(gd, entry, effect);

                verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id),
                        argThat(ids -> ids.contains(creature.getId())), any());
            }

            @Test
            @DisplayName("Logs and does nothing when no matching permanents")
            void noMatching() {
                Card card = createCard("Goblin Bombardment");
                PermanentPredicate filter = new PermanentTruePredicate();
                DrawCardEffect thenEffect = new DrawCardEffect(1);
                SacrificePermanentThenEffect effect = new SacrificePermanentThenEffect(filter, thenEffect, "a creature");
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                Permanent nonMatching = new Permanent(createCard("Mountain"));
                gd.playerBattlefields.get(player1Id).add(nonMatching);

                when(predicateEvaluationService.matchesPermanentPredicate(eq(nonMatching), eq(filter), any())).thenReturn(false);

                resolveEffect(gd, entry, effect);

                verify(playerInputService, never()).beginPermanentChoice(any(), any(), any(), any());
                verify(gameLogService).append(eq(gd), argThat((GameLogEntry logEntry) ->
                        logEntry.plainText().contains("no a creature to sacrifice")));
            }

            @Test
            @DisplayName("Passes the stack entry source to the permanent filter")
            void passesSourceContextToFilter() {
                Card card = createCard("Goblin Bombardment");
                PermanentPredicate filter = new PermanentTruePredicate();
                SacrificePermanentThenEffect effect = new SacrificePermanentThenEffect(filter, null, "a creature");
                StackEntry entry = createEntry(card, player1Id, List.of(effect));

                Permanent creature = new Permanent(createCard("Grizzly Bears"));
                gd.playerBattlefields.get(player1Id).add(creature);
                when(predicateEvaluationService.matchesPermanentPredicate(eq(creature), eq(filter), any()))
                        .thenReturn(true);

                resolveEffect(gd, entry, effect);

                verify(predicateEvaluationService).matchesPermanentPredicate(eq(creature), eq(filter),
                        argThat(context -> card.getId().equals(context.sourceCardId())
                                && player1Id.equals(context.sourceControllerId())));
            }

    @Test
    @DisplayName("Offers only the stack entry's granting permanent when the sacrifice is of the granting permanent")
    void sacrificingGrantingPermanentOffersOnlyTheEntrysGrantingPermanent() {
        Card card = createCard("Sunfire Torch");
        PermanentPredicate filter = new PermanentTruePredicate();
        SacrificePermanentThenEffect effect = new SacrificePermanentThenEffect(filter, new DrawCardEffect(1),
                "Sunfire Torch").sacrificingGrantingPermanent();
        Permanent granting = new Permanent(createCard("Sunfire Torch"));
        Permanent otherCopy = new Permanent(createCard("Sunfire Torch"));
        gd.playerBattlefields.get(player1Id).addAll(List.of(granting, otherCopy));
        StackEntry entry = createEntry(card, player1Id, List.of(effect));
        entry.setGrantingPermanentId(granting.getId());
        when(predicateEvaluationService.matchesPermanentPredicate(any(Permanent.class), eq(filter), any()))
                .thenReturn(true);

        resolveEffect(gd, entry, effect);

        verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id),
                eq(List.of(granting.getId())), any());
    }

    @Test
    @DisplayName("Never offers the stack entry's granting permanent when the sacrifice excludes it")
    void excludingGrantingPermanentNeverOffersTheEntrysGrantingPermanent() {
        Card card = createCard("Dire Blunderbuss");
        PermanentPredicate filter = new PermanentTruePredicate();
        SacrificePermanentThenEffect effect = new SacrificePermanentThenEffect(filter, new DrawCardEffect(1),
                "an artifact other than Dire Blunderbuss").excludingGrantingPermanent();
        Permanent granting = new Permanent(createCard("Dire Blunderbuss"));
        Permanent otherArtifact = new Permanent(createCard("Ornithopter"));
        gd.playerBattlefields.get(player1Id).addAll(List.of(granting, otherArtifact));
        StackEntry entry = createEntry(card, player1Id, List.of(effect));
        entry.setGrantingPermanentId(granting.getId());
        when(predicateEvaluationService.matchesPermanentPredicate(any(Permanent.class), eq(filter), any()))
                .thenReturn(true);

        resolveEffect(gd, entry, effect);

        verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id),
                eq(List.of(otherArtifact.getId())), any());
    }

    @Test
    @DisplayName("A sacrifice of the granting permanent fails loudly on an entry without one")
    void grantingPermanentSacrificeWithoutGrantingPermanentThrows() {
        Card card = createCard("Sunfire Torch");
        SacrificePermanentThenEffect effect = new SacrificePermanentThenEffect(new PermanentTruePredicate(),
                new DrawCardEffect(1), "Sunfire Torch").sacrificingGrantingPermanent();
        StackEntry entry = createEntry(card, player1Id, List.of(effect));

        assertThatThrownBy(() -> resolveEffect(gd, entry, effect))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("granted");
    }
}
