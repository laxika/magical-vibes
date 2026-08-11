package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class MayEffectHandlerTest extends AbstractPlayerInteractionHandlerTest {

    @Test
            @DisplayName("Sets resolvingMayEffectFromStack flag and adds pending may ability")
            void setsFlagAndAddsPendingMay() {
                Card card = createCard("Ob Nixilis");
                DrawCardEffect wrapped = new DrawCardEffect(1);
                MayEffect mayEffect = new MayEffect(wrapped, "Draw a card?");
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(mayEffect), player2Id);

                resolveEffect(gd, entry, mayEffect);

                assertThat(gd.resolvingMayEffectFromStack).isTrue();
                assertThat(gd.pendingMayAbilities).hasSize(1);
                assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player1Id);
                assertThat(gd.pendingMayAbilities.getFirst().effects()).containsExactly(wrapped);
            }

            @Test
            @DisplayName("Preserves target and source permanent IDs in pending may")
            void preservesTargetAndSourceIds() {
                Card card = createCard("Ob Nixilis");
                DrawCardEffect wrapped = new DrawCardEffect(1);
                MayEffect mayEffect = new MayEffect(wrapped, "Draw a card?");
                UUID sourcePermanentId = UUID.randomUUID();
                StackEntry entry = createTriggeredEntryWithTarget(card, player1Id, List.of(mayEffect), player2Id, sourcePermanentId);

                resolveEffect(gd, entry, mayEffect);

                assertThat(gd.pendingMayAbilities.getFirst().targetCardId()).isEqualTo(player2Id);
                assertThat(gd.pendingMayAbilities.getFirst().sourcePermanentId()).isEqualTo(sourcePermanentId);
            }

            @Test
            @DisplayName("Offers a MayEffect with a decline branch to the target permanent's controller")
            void offersChoiceToTargetPermanentController() {
                Card card = createCard("Blazing Salvo");
                DrawCardEffect accepted = new DrawCardEffect(1);
                GainLifeEffect declined = new GainLifeEffect(1);
                MayEffect mayEffect = new MayEffect(accepted, "Choose?", declined,
                        MayChoicePlayer.TARGET_PERMANENT_CONTROLLER);
                UUID targetPermanentId = UUID.randomUUID();
                when(gameQueryService.findPermanentController(gd, targetPermanentId)).thenReturn(player2Id);
                StackEntry entry = createEntryWithTarget(card, player1Id, List.of(mayEffect), targetPermanentId);

                resolveEffect(gd, entry, mayEffect);

                assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player2Id);
                assertThat(gd.pendingMayAbilities.getFirst().effects()).containsExactly(accepted);
            }

            @Test
            @DisplayName("Chooses the controller of the targeted spell")
            void choosesTargetSpellController() {
                Card source = createCard("Vex");
                Card targetSpell = createCard("Grizzly Bears");
                StackEntry targetEntry = createEntry(targetSpell, player2Id, List.of());
                gd.stack.add(targetEntry);

                MayEffect mayEffect = new MayEffect(
                        new DrawCardEffect(1),
                        "Draw a card?",
                        null,
                        MayChoicePlayer.TARGET_SPELL_CONTROLLER);
                StackEntry entry = createEntryWithTarget(source, player1Id, List.of(mayEffect), targetSpell.getId());

                resolveEffect(gd, entry, mayEffect);

                assertThat(gd.pendingMayAbilities.getFirst().controllerId()).isEqualTo(player2Id);
            }
}
