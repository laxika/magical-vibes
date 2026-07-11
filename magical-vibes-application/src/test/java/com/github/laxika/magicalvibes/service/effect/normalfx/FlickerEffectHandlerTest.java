package com.github.laxika.magicalvibes.service.effect.normalfx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link FlickerEffectHandler}'s delayed ({@code AT_STEP}) TARGET and SELF paths.
 * The IMMEDIATE and TARGET_PLAYERS_PERMANENTS paths are covered by the behavioral card tests
 * (Daydream, Siren's Ruse, Sudden Disappearance).
 */
@ExtendWith(MockitoExtension.class)
class FlickerEffectHandlerTest {

    @Mock private GraveyardService graveyardService;
    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private PlayerInputService playerInputService;
    @Mock private TriggerCollectionService triggerCollectionService;
    @Mock private BattlefieldEntryService battlefieldEntryService;
    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private DrawService drawService;
    @Mock private AmountEvaluationService amountEvaluationService;
    @InjectMocks
    private ExileSupport exileSupport;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private FlickerEffectHandler handler;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        handler = new FlickerEffectHandler(exileSupport, gameQueryService, predicateEvaluationService,
                gameBroadcastService, permanentRemovalService, battlefieldEntryService,
                drawService, amountEvaluationService);
    }

    private Card createCreatureCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    @Nested
    @DisplayName("TARGET, return at end step")
    class TargetAtStep {

        private StackEntry createEntry(Card sourceCard, UUID controllerId, UUID targetId) {
            return new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, sourceCard, controllerId, sourceCard.getName(),
                    List.of(FlickerEffect.exileTargetReturnAtEndStep()), 0, targetId, null);
        }

        @Test
        @DisplayName("Exiles target permanent and adds a pending exile return")
        void exilesAndSchedulesReturn() {
            Card targetCard = createCreatureCard("Grizzly Bears");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCreatureCard("Glimmerpoint Stag");

            StackEntry entry = createEntry(sourceCard, player1Id, target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1Id);

            handler.resolve(gd, entry, entry.getEffectsToResolve().getFirst());

            verify(permanentRemovalService).removePermanentToExile(gd, target);
            assertThat(gd.getDelayedActions(PendingExileReturn.class))
                    .anyMatch(per -> per.card().getName().equals("Grizzly Bears")
                            && per.controllerId().equals(player1Id));
        }

        @Test
        @DisplayName("Does nothing when target is removed before resolution")
        void fizzlesWhenTargetRemoved() {
            UUID targetId = UUID.randomUUID();
            Card sourceCard = createCreatureCard("Glimmerpoint Stag");

            StackEntry entry = createEntry(sourceCard, player1Id, targetId);

            when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(null);

            handler.resolve(gd, entry, entry.getEffectsToResolve().getFirst());

            verify(permanentRemovalService, never()).removePermanentToExile(any(), any());
            assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
        }

        @Test
        @DisplayName("Stolen creature's pending return uses original owner")
        void stolenCreatureUsesOriginalOwner() {
            Card targetCard = createCreatureCard("Grizzly Bears");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCreatureCard("Glimmerpoint Stag");

            gd.stolenCreatures.put(target.getId(), player2Id);

            StackEntry entry = createEntry(sourceCard, player1Id, target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1Id);

            handler.resolve(gd, entry, entry.getEffectsToResolve().getFirst());

            assertThat(gd.getDelayedActions(PendingExileReturn.class))
                    .anyMatch(per -> per.card().getName().equals("Grizzly Bears")
                            && per.controllerId().equals(player2Id));
        }

        @Test
        @DisplayName("Logs exile and return message")
        void logsExileAndReturn() {
            Card targetCard = createCreatureCard("Grizzly Bears");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCreatureCard("Glimmerpoint Stag");

            StackEntry entry = createEntry(sourceCard, player1Id, target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1Id);

            handler.resolve(gd, entry, entry.getEffectsToResolve().getFirst());

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Grizzly Bears is exiled. It will return at the beginning of the next end step."));
        }
    }

    @Nested
    @DisplayName("SELF, return at end step")
    class SelfAtStep {

        private StackEntry createEntry(Card sourceCard, UUID controllerId, UUID sourcePermanentId) {
            return new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY, sourceCard, controllerId, sourceCard.getName(),
                    List.of(FlickerEffect.exileSelfReturnAtEndStep()), null, sourcePermanentId);
        }

        @Test
        @DisplayName("Exiles self and adds a pending exile return for the controller")
        void exilesSelfAndSchedulesReturn() {
            Card sourceCard = createCreatureCard("Argent Sphinx");
            Permanent source = new Permanent(sourceCard);

            StackEntry entry = createEntry(sourceCard, player1Id, source.getId());

            when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);

            handler.resolve(gd, entry, entry.getEffectsToResolve().getFirst());

            verify(permanentRemovalService).removePermanentToExile(gd, source);
            assertThat(gd.getDelayedActions(PendingExileReturn.class))
                    .anyMatch(per -> per.card().getName().equals("Argent Sphinx")
                            && per.controllerId().equals(player1Id));
        }

        @Test
        @DisplayName("Does nothing when the source permanent is removed before resolution")
        void fizzlesWhenSourceRemoved() {
            UUID sourceId = UUID.randomUUID();
            Card sourceCard = createCreatureCard("Argent Sphinx");

            StackEntry entry = createEntry(sourceCard, player1Id, sourceId);

            when(gameQueryService.findPermanentById(gd, sourceId)).thenReturn(null);

            handler.resolve(gd, entry, entry.getEffectsToResolve().getFirst());

            verify(permanentRemovalService, never()).removePermanentToExile(any(), any());
            assertThat(gd.getDelayedActions(PendingExileReturn.class)).isEmpty();
        }

        @Test
        @DisplayName("Logs exile and return message")
        void logsExileAndReturn() {
            Card sourceCard = createCreatureCard("Argent Sphinx");
            Permanent source = new Permanent(sourceCard);

            StackEntry entry = createEntry(sourceCard, player1Id, source.getId());

            when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);

            handler.resolve(gd, entry, entry.getEffectsToResolve().getFirst());

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Argent Sphinx is exiled. It will return at the beginning of the next end step."));
        }
    }
}
