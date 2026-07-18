package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.EachPermanentScope;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class DestroyAllPermanentsEffectHandlerTest {

    @Mock private BattlefieldEntryService battlefieldEntryService;
    @Mock private GraveyardService graveyardService;
    @Mock private DamagePreventionService damagePreventionService;
    @Mock private GameOutcomeService gameOutcomeService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private GameQueryService gameQueryService;
    @Mock private PredicateEvaluationService predicateEvaluationService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PlayerInputService playerInputService;
    @Mock private LifeSupport lifeSupport;
    @Mock private EffectHandlerRegistry effectHandlerRegistry;
    @InjectMocks private DestructionSupport destructionSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private DestroyAllPermanentsEffectHandler destroyAllPermanentsHandler;

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
        destroyAllPermanentsHandler = new DestroyAllPermanentsEffectHandler(destructionSupport, gameQueryService,
                predicateEvaluationService, effectHandlerRegistry, gameOutcomeService);

    }

    // ===== Helper methods =====

        private Card createCard(String name) {
            Card card = new Card();
            card.setName(name);
            return card;
        }

        private Card createCreatureCard(String name) {
            Card card = createCard(name);
            card.setType(CardType.CREATURE);
            card.setPower(2);
            card.setToughness(2);
            return card;
        }

        private Card createLandCard(String name) {
            Card card = createCard(name);
            card.setType(CardType.LAND);
            return card;
        }

        private Card createArtifactCard(String name, String manaCost) {
            Card card = createCard(name);
            card.setType(CardType.ARTIFACT);
            card.setManaCost(manaCost);
            return card;
        }

        private Permanent addPermanent(UUID playerId, Card card) {
            Permanent permanent = new Permanent(card);
            gd.playerBattlefields.get(playerId).add(permanent);
            return permanent;
        }

        private Permanent addCreature(UUID playerId, String name) {
            return addPermanent(playerId, createCreatureCard(name));
        }

        private StackEntry sorceryEntry(Card card, UUID controllerId, UUID targetId) {
            return new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId,
                    card.getName(), List.of(), 0, targetId, null);
        }

        private StackEntry instantEntry(Card card, UUID controllerId, UUID targetId) {
            return new StackEntry(StackEntryType.INSTANT_SPELL, card, controllerId,
                    card.getName(), List.of(), 0, targetId, null);
        }

        private StackEntry triggeredAbilityEntry(Card card, UUID controllerId, UUID targetId, UUID sourcePermanentId) {
            return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                    card.getName(), List.of(), targetId, sourcePermanentId);
        }

        private StackEntry activatedAbilityEntry(Card card, UUID controllerId, UUID targetId, UUID sourcePermanentId) {
            return new StackEntry(StackEntryType.ACTIVATED_ABILITY, card, controllerId,
                    card.getName(), List.of(), targetId, sourcePermanentId);
        }

        // =========================================================================
        // DestroyAllPermanentsEffect
        // =========================================================================

    @Test
            @DisplayName("Destroys all creatures on both sides")
            void destroysAllCreaturesOnBothSides() {
                Permanent bears = addCreature(player1Id, "Grizzly Bears");
                Permanent angel = addCreature(player2Id, "Serra Angel");

                Card wrathCard = createCard("Wrath of God");
                StackEntry entry = sorceryEntry(wrathCard, player1Id, null);
                PermanentPredicate filter = new PermanentIsCreaturePredicate();
                DestroyAllPermanentsEffect effect = new DestroyAllPermanentsEffect(filter, true);

                when(predicateEvaluationService.matchesPermanentPredicate(eq(bears), eq(filter), any())).thenReturn(true);
                when(predicateEvaluationService.matchesPermanentPredicate(eq(angel), eq(filter), any())).thenReturn(true);
                when(gameQueryService.hasKeyword(eq(gd), any(), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);

                destroyAllPermanentsHandler.resolve(gd, entry, effect);

                verify(permanentRemovalService).removePermanentToGraveyard(gd, bears);
                verify(permanentRemovalService).removePermanentToGraveyard(gd, angel);
                verify(gameBroadcastService).logAndBroadcast(gd, GameLog.text("Grizzly Bears is destroyed."));
                verify(gameBroadcastService).logAndBroadcast(gd, GameLog.text("Serra Angel is destroyed."));
            }

            @Test
            @DisplayName("Does not destroy non-creature permanents")
            void doesNotDestroyNonCreatures() {
                Card spellbookCard = createArtifactCard("Spellbook", "{0}");
                Permanent spellbook = addPermanent(player1Id, spellbookCard);
                Permanent bears = addCreature(player1Id, "Grizzly Bears");

                Card wrathCard = createCard("Wrath of God");
                StackEntry entry = sorceryEntry(wrathCard, player1Id, null);
                PermanentPredicate filter = new PermanentIsCreaturePredicate();
                DestroyAllPermanentsEffect effect = new DestroyAllPermanentsEffect(filter, true);

                when(predicateEvaluationService.matchesPermanentPredicate(eq(spellbook), eq(filter), any())).thenReturn(false);
                when(predicateEvaluationService.matchesPermanentPredicate(eq(bears), eq(filter), any())).thenReturn(true);
                when(gameQueryService.hasKeyword(eq(gd), any(), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);

                destroyAllPermanentsHandler.resolve(gd, entry, effect);

                verify(permanentRemovalService).removePermanentToGraveyard(gd, bears);
                verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, spellbook);
            }

            @Test
            @DisplayName("Indestructible creatures survive")
            void indestructibleCreaturesSurvive() {
                Permanent golem = addCreature(player2Id, "Indestructible Golem");
                Permanent bears = addCreature(player2Id, "Grizzly Bears");

                Card wrathCard = createCard("Wrath of God");
                StackEntry entry = sorceryEntry(wrathCard, player1Id, null);
                PermanentPredicate filter = new PermanentIsCreaturePredicate();
                DestroyAllPermanentsEffect effect = new DestroyAllPermanentsEffect(filter, true);

                when(predicateEvaluationService.matchesPermanentPredicate(eq(golem), eq(filter), any())).thenReturn(true);
                when(predicateEvaluationService.matchesPermanentPredicate(eq(bears), eq(filter), any())).thenReturn(true);
                when(gameQueryService.hasKeyword(gd, golem, Keyword.INDESTRUCTIBLE)).thenReturn(true);
                when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);

                destroyAllPermanentsHandler.resolve(gd, entry, effect);

                verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, golem);
                verify(permanentRemovalService).removePermanentToGraveyard(gd, bears);
            }

            @Test
            @DisplayName("Indestructible status is logged")
            void indestructibleIsLogged() {
                Permanent golem = addCreature(player2Id, "Indestructible Golem");

                Card wrathCard = createCard("Wrath of God");
                StackEntry entry = sorceryEntry(wrathCard, player1Id, null);
                PermanentPredicate filter = new PermanentIsCreaturePredicate();
                DestroyAllPermanentsEffect effect = new DestroyAllPermanentsEffect(filter, true);

                when(predicateEvaluationService.matchesPermanentPredicate(eq(golem), eq(filter), any())).thenReturn(true);
                when(gameQueryService.hasKeyword(gd, golem, Keyword.INDESTRUCTIBLE)).thenReturn(true);

                destroyAllPermanentsHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(gd, GameLog.text("Indestructible Golem is indestructible."));
            }

            @Test
            @DisplayName("Regenerated creature survives when cannotBeRegenerated is false")
            void regeneratedCreatureSurvives() {
                Permanent bears = addCreature(player1Id, "Grizzly Bears");
                Permanent elves = addCreature(player2Id, "Llanowar Elves");

                Card plagueWindCard = createCard("Plague Wind");
                StackEntry entry = sorceryEntry(plagueWindCard, player1Id, null);
                PermanentPredicate filter = new PermanentIsCreaturePredicate();
                DestroyAllPermanentsEffect effect = new DestroyAllPermanentsEffect(filter, false);

                when(predicateEvaluationService.matchesPermanentPredicate(eq(bears), eq(filter), any())).thenReturn(false);
                when(predicateEvaluationService.matchesPermanentPredicate(eq(elves), eq(filter), any())).thenReturn(true);
                when(gameQueryService.hasKeyword(gd, elves, Keyword.INDESTRUCTIBLE)).thenReturn(false);
                when(graveyardService.tryRegenerate(gd, elves)).thenReturn(true);

                destroyAllPermanentsHandler.resolve(gd, entry, effect);

                verify(graveyardService).tryRegenerate(gd, elves);
                verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, elves);
            }

            @Test
            @DisplayName("Regeneration is skipped when cannotBeRegenerated is true")
            void regenerationSkippedWhenCannotBeRegenerated() {
                Permanent bears = addCreature(player1Id, "Grizzly Bears");

                Card wrathCard = createCard("Wrath of God");
                StackEntry entry = sorceryEntry(wrathCard, player1Id, null);
                PermanentPredicate filter = new PermanentIsCreaturePredicate();
                DestroyAllPermanentsEffect effect = new DestroyAllPermanentsEffect(filter, true);

                when(predicateEvaluationService.matchesPermanentPredicate(eq(bears), eq(filter), any())).thenReturn(true);
                when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);

                destroyAllPermanentsHandler.resolve(gd, entry, effect);

                verify(graveyardService, never()).tryRegenerate(any(), any());
                verify(permanentRemovalService).removePermanentToGraveyard(gd, bears);
            }

            @Test
            @DisplayName("Only destroys opponents' creatures when filter excludes controller")
            void onlyDestroysOpponentsCreatures() {
                Permanent myBears = addCreature(player1Id, "Grizzly Bears");
                Permanent angel = addCreature(player2Id, "Serra Angel");
                Permanent elves = addCreature(player2Id, "Llanowar Elves");

                Card plagueWindCard = createCard("Plague Wind");
                StackEntry entry = sorceryEntry(plagueWindCard, player1Id, null);
                PermanentPredicate filter = new PermanentIsCreaturePredicate();
                DestroyAllPermanentsEffect effect = new DestroyAllPermanentsEffect(filter, false);

                // Filter excludes controller's creatures (Plague Wind behavior)
                when(predicateEvaluationService.matchesPermanentPredicate(eq(myBears), eq(filter), any())).thenReturn(false);
                when(predicateEvaluationService.matchesPermanentPredicate(eq(angel), eq(filter), any())).thenReturn(true);
                when(predicateEvaluationService.matchesPermanentPredicate(eq(elves), eq(filter), any())).thenReturn(true);
                when(gameQueryService.hasKeyword(eq(gd), any(), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);

                destroyAllPermanentsHandler.resolve(gd, entry, effect);

                verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, myBears);
                verify(permanentRemovalService).removePermanentToGraveyard(gd, angel);
                verify(permanentRemovalService).removePermanentToGraveyard(gd, elves);
            }

            @Test
            @DisplayName("Destruction is logged for each destroyed creature")
            void destructionIsLogged() {
                Permanent bears = addCreature(player1Id, "Grizzly Bears");
                Permanent elves = addCreature(player2Id, "Llanowar Elves");

                Card wrathCard = createCard("Wrath of God");
                StackEntry entry = sorceryEntry(wrathCard, player1Id, null);
                PermanentPredicate filter = new PermanentIsCreaturePredicate();
                DestroyAllPermanentsEffect effect = new DestroyAllPermanentsEffect(filter, true);

                when(predicateEvaluationService.matchesPermanentPredicate(eq(bears), eq(filter), any())).thenReturn(true);
                when(predicateEvaluationService.matchesPermanentPredicate(eq(elves), eq(filter), any())).thenReturn(true);
                when(gameQueryService.hasKeyword(eq(gd), any(), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);

                destroyAllPermanentsHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(gd, GameLog.text("Grizzly Bears is destroyed."));
                verify(gameBroadcastService).logAndBroadcast(gd, GameLog.text("Llanowar Elves is destroyed."));
            }

            @Test
            @DisplayName("TARGET_PLAYER scope destroys only the targeted player's matching permanents")
            void targetPlayerScopeDestroysOnlyTargetsPermanents() {
                Permanent myBears = addCreature(player1Id, "Grizzly Bears");
                Permanent angel = addCreature(player2Id, "Serra Angel");

                Card rainCard = createCard("Rain of Daggers");
                StackEntry entry = sorceryEntry(rainCard, player1Id, player2Id);
                PermanentPredicate filter = new PermanentIsCreaturePredicate();
                DestroyAllPermanentsEffect effect = new DestroyAllPermanentsEffect(filter,
                        EachPermanentScope.TARGET_PLAYER, null);

                when(predicateEvaluationService.matchesPermanentPredicate(eq(angel), eq(filter), any())).thenReturn(true);
                when(gameQueryService.hasKeyword(gd, angel, Keyword.INDESTRUCTIBLE)).thenReturn(false);

                destroyAllPermanentsHandler.resolve(gd, entry, effect);

                verify(permanentRemovalService).removePermanentToGraveyard(gd, angel);
                verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, myBears);
            }

            @Test
            @DisplayName("Rider resolves through its handler with the destroyed count on eventValue")
            void riderReceivesDestroyedCountOnEventValue() {
                Permanent bears = addCreature(player2Id, "Grizzly Bears");
                Permanent elves = addCreature(player2Id, "Llanowar Elves");
                Permanent golem = addCreature(player2Id, "Indestructible Golem");

                Card gustCard = createCard("Fracturing Gust");
                StackEntry entry = sorceryEntry(gustCard, player1Id, null);
                PermanentPredicate filter = new PermanentIsCreaturePredicate();
                GainLifeEffect rider = new GainLifeEffect(new EventValue());
                DestroyAllPermanentsEffect effect = new DestroyAllPermanentsEffect(filter, rider);

                when(predicateEvaluationService.matchesPermanentPredicate(any(), eq(filter), any())).thenReturn(true);
                when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);
                when(gameQueryService.hasKeyword(gd, elves, Keyword.INDESTRUCTIBLE)).thenReturn(false);
                when(gameQueryService.hasKeyword(gd, golem, Keyword.INDESTRUCTIBLE)).thenReturn(true);
                EffectHandler riderHandler = org.mockito.Mockito.mock(EffectHandler.class);
                when(effectHandlerRegistry.getHandler(rider)).thenReturn(riderHandler);

                destroyAllPermanentsHandler.resolve(gd, entry, effect);

                ArgumentCaptor<StackEntry> entryCaptor = ArgumentCaptor.forClass(StackEntry.class);
                verify(riderHandler).resolve(eq(gd), entryCaptor.capture(), eq(rider));
                assertThat(entryCaptor.getValue().getEventValue()).isEqualTo(2);
                assertThat(entryCaptor.getValue().getControllerId()).isEqualTo(player1Id);
                verify(gameOutcomeService).checkWinCondition(gd);
            }

            @Test
            @DisplayName("No rider dispatch when thenEffect is null")
            void noRiderDispatchWithoutThenEffect() {
                Permanent bears = addCreature(player1Id, "Grizzly Bears");

                Card wrathCard = createCard("Wrath of God");
                StackEntry entry = sorceryEntry(wrathCard, player1Id, null);
                PermanentPredicate filter = new PermanentIsCreaturePredicate();
                DestroyAllPermanentsEffect effect = new DestroyAllPermanentsEffect(filter, true);

                when(predicateEvaluationService.matchesPermanentPredicate(eq(bears), eq(filter), any())).thenReturn(true);
                when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);

                destroyAllPermanentsHandler.resolve(gd, entry, effect);

                verify(effectHandlerRegistry, never()).getHandler(any());
            }
}
