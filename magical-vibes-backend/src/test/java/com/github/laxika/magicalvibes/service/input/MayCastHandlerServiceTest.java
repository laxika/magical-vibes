package com.github.laxika.magicalvibes.service.input;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.turn.TurnProgressionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MayCastHandlerServiceTest {

    @Mock private InputCompletionService inputCompletionService;
    @Mock private GameQueryService gameQueryService;
    @Mock private GraveyardService graveyardService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PlayerInputService playerInputService;
    @Mock private TurnProgressionService turnProgressionService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private TriggerCollectionService triggerCollectionService;
    @Mock private BattlefieldEntryService battlefieldEntryService;
    @Mock private ExileService exileService;

    @InjectMocks
    private MayCastHandlerService svc;

    private GameData gd;
    private Player player1;
    private Player player2;

    private static final UUID PLAYER1_ID = UUID.randomUUID();
    private static final UUID PLAYER2_ID = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        player1 = new Player(PLAYER1_ID, "Alice");
        player2 = new Player(PLAYER2_ID, "Bob");

        gd = new GameData(UUID.randomUUID(), "test-game", PLAYER1_ID, "Alice");
        gd.playerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.orderedPlayerIds.addAll(List.of(PLAYER1_ID, PLAYER2_ID));
        gd.playerIdToName.put(PLAYER1_ID, "Alice");
        gd.playerIdToName.put(PLAYER2_ID, "Bob");
        gd.playerBattlefields.put(PLAYER1_ID, new ArrayList<>());
        gd.playerBattlefields.put(PLAYER2_ID, new ArrayList<>());
        gd.playerGraveyards.put(PLAYER1_ID, new ArrayList<>());
        gd.playerGraveyards.put(PLAYER2_ID, new ArrayList<>());
        gd.playerDecks.put(PLAYER1_ID, new ArrayList<>());
        gd.playerDecks.put(PLAYER2_ID, new ArrayList<>());
        gd.playerHands.put(PLAYER1_ID, new ArrayList<>());
        gd.playerHands.put(PLAYER2_ID, new ArrayList<>());
    }

    // ===== Helper methods =====

    private Card createSorcery(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.SORCERY);
        card.setManaCost("1U");
        return card;
    }

    private Card createInstant(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost("R");
        return card;
    }

    private Card createCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("1G");
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Card createLand(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.LAND);
        return card;
    }

    private Card createArtifact(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        card.setManaCost("2");
        return card;
    }

    private PendingMayAbility abilityFor(Card card) {
        return new PendingMayAbility(card, PLAYER1_ID, List.of(), "May cast " + card.getName());
    }

    // ===== buildValidSpellTargets =====

    @Nested
    @DisplayName("buildValidSpellTargets")
    class BuildValidSpellTargets {

        @Test
        @DisplayName("Returns only player IDs for player-only targeting effects")
        void returnsOnlyPlayerIdsForPlayerOnlyTargeting() {
            List<CardEffect> effects = List.of(new MillTargetPlayerEffect(5));
            Card card = createSorcery("Tome Scour");

            List<UUID> targets = svc.buildValidSpellTargets(gd, card, effects);

            assertThat(targets).containsExactlyInAnyOrder(PLAYER1_ID, PLAYER2_ID);
        }

        @Test
        @DisplayName("Returns only permanent IDs for permanent-only targeting effects")
        void returnsOnlyPermanentIdsForPermanentOnlyTargeting() {
            Card card = createInstant("Shock");
            List<CardEffect> effects = List.of(new DealDamageToAnyTargetEffect(2));

            Permanent creature = new Permanent(createCreature("Bear"));
            gd.playerBattlefields.get(PLAYER1_ID).add(creature);
            when(gameQueryService.isCreature(gd, creature)).thenReturn(true);

            List<UUID> targets = svc.buildValidSpellTargets(gd, card, effects);

            // DealDamageToTargetCreatureOrPlayerEffect has both canTargetPermanent and canTargetPlayer true
            assertThat(targets).contains(creature.getId(), PLAYER1_ID, PLAYER2_ID);
        }

        @Test
        @DisplayName("Does not include permanents when no effect can target permanents")
        void doesNotIncludePermanentsWhenNoEffectTargetsPermanents() {
            Card card = createSorcery("Mill Spell");
            List<CardEffect> effects = List.of(new MillTargetPlayerEffect(3));

            Permanent creature = new Permanent(createCreature("Bear"));
            gd.playerBattlefields.get(PLAYER1_ID).add(creature);

            List<UUID> targets = svc.buildValidSpellTargets(gd, card, effects);

            assertThat(targets).doesNotContain(creature.getId());
            assertThat(targets).containsExactlyInAnyOrder(PLAYER1_ID, PLAYER2_ID);
        }

        @Test
        @DisplayName("Filters permanents by PermanentPredicateTargetFilter when present")
        void filtersPermanentsByPredicateFilter() {
            Card card = createInstant("Shatter");
            PermanentPredicateTargetFilter filter =
                    new PermanentPredicateTargetFilter(new PermanentIsArtifactPredicate(), "Target must be an artifact");
            CardEffect effect = new CardEffect() {
                @Override public boolean canTargetPermanent() { return true; }
            };
            card.target(filter).addEffect(EffectSlot.SPELL, effect);
            List<CardEffect> effects = List.of(effect);

            Permanent artifact = new Permanent(createArtifact("Sol Ring"));
            Permanent creature = new Permanent(createCreature("Bear"));
            gd.playerBattlefields.get(PLAYER1_ID).add(artifact);
            gd.playerBattlefields.get(PLAYER1_ID).add(creature);

            when(gameQueryService.matchesPermanentPredicate(eq(gd), eq(artifact), any()))
                    .thenReturn(true);
            when(gameQueryService.matchesPermanentPredicate(eq(gd), eq(creature), any()))
                    .thenReturn(false);

            List<UUID> targets = svc.buildValidSpellTargets(gd, card, effects);

            assertThat(targets).containsExactly(artifact.getId());
        }

        @Test
        @DisplayName("Returns empty list when no valid targets exist")
        void returnsEmptyWhenNoValidTargets() {
            Card card = createInstant("Lightning Bolt");
            CardEffect effect = new CardEffect() {
                @Override public boolean canTargetPermanent() { return true; }
            };
            List<CardEffect> effects = List.of(effect);

            // No permanents on battlefield, no canTargetPlayer
            List<UUID> targets = svc.buildValidSpellTargets(gd, card, effects);

            assertThat(targets).isEmpty();
        }

        @Test
        @DisplayName("Only adds creatures in default fallback (no target filter)")
        void onlyAddsCreaturesInDefaultFallback() {
            Card card = createInstant("Bolt");
            CardEffect effect = new CardEffect() {
                @Override public boolean canTargetPermanent() { return true; }
            };
            List<CardEffect> effects = List.of(effect);

            Permanent creature = new Permanent(createCreature("Bear"));
            Permanent nonCreature = new Permanent(createArtifact("Mox"));
            gd.playerBattlefields.get(PLAYER1_ID).add(creature);
            gd.playerBattlefields.get(PLAYER1_ID).add(nonCreature);

            when(gameQueryService.isCreature(gd, creature)).thenReturn(true);
            when(gameQueryService.isCreature(gd, nonCreature)).thenReturn(false);

            List<UUID> targets = svc.buildValidSpellTargets(gd, card, effects);

            assertThat(targets).containsExactly(creature.getId());
        }

        @Test
        @DisplayName("Includes permanents when PermanentPredicateTargetFilter is set even if no effect has canTargetPermanent")
        void includesPermanentsWhenFilterSetEvenWithoutEffectFlag() {
            Card card = createInstant("Targeted Spell");
            PermanentPredicateTargetFilter filter =
                    new PermanentPredicateTargetFilter(new PermanentIsArtifactPredicate(), "Target must be an artifact");
            // Use target() to register the filter on the card, but use a plain DrawCardEffect in the effects list
            card.target(filter).addEffect(EffectSlot.SPELL, new DrawCardEffect(1));

            // Effect doesn't declare canTargetPermanent, but the card has a PermanentPredicateTargetFilter
            List<CardEffect> effects = List.of(new DrawCardEffect(1));

            Permanent artifact = new Permanent(createArtifact("Sol Ring"));
            gd.playerBattlefields.get(PLAYER1_ID).add(artifact);

            when(gameQueryService.matchesPermanentPredicate(eq(gd), eq(artifact), any()))
                    .thenReturn(true);

            List<UUID> targets = svc.buildValidSpellTargets(gd, card, effects);

            assertThat(targets).contains(artifact.getId());
        }
    }

    // ===== handleCastFromLibraryChoice =====

    @Nested
    @DisplayName("handleCastFromLibraryChoice")
    class HandleCastFromLibraryChoice {

        @Test
        @DisplayName("Declining logs and calls processMayAbilitiesThenAutoPass")
        void decliningLogsAndProcesses() {
            Card card = createSorcery("Divination");
            PendingMayAbility ability = abilityFor(card);

            svc.handleCastFromLibraryChoice(gd, player1, false, ability);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), anyString());
            verify(inputCompletionService).processMayAbilitiesThenAutoPass(gd);
        }

        @Test
        @DisplayName("Card no longer on top of library logs and does not put on stack")
        void cardNoLongerOnTopLogsAndSkips() {
            Card card = createSorcery("Divination");
            Card differentCard = createSorcery("Other Spell");
            gd.playerDecks.get(PLAYER1_ID).add(differentCard);
            PendingMayAbility ability = abilityFor(card);

            svc.handleCastFromLibraryChoice(gd, player1, true, ability);

            assertThat(gd.stack).isEmpty();
            verify(inputCompletionService).processMayAbilitiesThenAutoPass(gd);
        }

        @Test
        @DisplayName("Non-targeted sorcery is put on stack and removed from library")
        void nonTargetedSorceryPutOnStack() {
            Card card = createSorcery("Divination");
            card.addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
            gd.playerDecks.get(PLAYER1_ID).add(card);
            PendingMayAbility ability = abilityFor(card);

            svc.handleCastFromLibraryChoice(gd, player1, true, ability);

            assertThat(gd.playerDecks.get(PLAYER1_ID)).isEmpty();
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
            assertThat(gd.stack.getFirst().getCard()).isEqualTo(card);
            verify(triggerCollectionService).checkSpellCastTriggers(gd, card, PLAYER1_ID, false);
            verify(inputCompletionService).processMayAbilitiesThenAutoPass(gd);
        }

        @Test
        @DisplayName("Non-targeted instant uses INSTANT_SPELL type")
        void nonTargetedInstantUsesInstantType() {
            Card card = createInstant("Think Twice");
            card.addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
            gd.playerDecks.get(PLAYER1_ID).add(card);
            PendingMayAbility ability = abilityFor(card);

            svc.handleCastFromLibraryChoice(gd, player1, true, ability);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        }

        @Test
        @DisplayName("Non-targeted spell clears priorityPassedBy")
        void nonTargetedSpellClearsPriorityPassedBy() {
            Card card = createSorcery("Divination");
            card.addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
            gd.playerDecks.get(PLAYER1_ID).add(card);
            gd.priorityPassedBy.add(PLAYER1_ID);
            PendingMayAbility ability = abilityFor(card);

            svc.handleCastFromLibraryChoice(gd, player1, true, ability);

            assertThat(gd.priorityPassedBy).isEmpty();
        }

        @Test
        @DisplayName("Targeted spell with valid targets begins permanent choice and does not call processMayAbilities")
        void targetedSpellWithTargetsBeginsChoice() {
            Card card = createSorcery("Tome Scour");
            card.addEffect(EffectSlot.SPELL, new MillTargetPlayerEffect(5));
            gd.playerDecks.get(PLAYER1_ID).add(card);
            PendingMayAbility ability = abilityFor(card);

            svc.handleCastFromLibraryChoice(gd, player1, true, ability);

            verify(playerInputService).beginPermanentChoice(eq(gd), eq(PLAYER1_ID), anyList(), anyString());
            // Should NOT call processMayAbilitiesThenAutoPass since we return early
            verify(inputCompletionService, never()).processMayAbilitiesThenAutoPass(gd);
        }

        @Test
        @DisplayName("Targeted spell with no valid targets puts card back on library")
        void targetedSpellNoTargetsPutsCardBack() {
            Card card = createInstant("Targeted Spell");
            CardEffect permanentOnly = new CardEffect() {
                @Override public boolean canTargetPermanent() { return true; }
            };
            card.addEffect(EffectSlot.SPELL, permanentOnly);
            gd.playerDecks.get(PLAYER1_ID).add(card);
            PendingMayAbility ability = abilityFor(card);

            // No permanents on battlefield
            svc.handleCastFromLibraryChoice(gd, player1, true, ability);

            assertThat(gd.playerDecks.get(PLAYER1_ID)).containsExactly(card);
            assertThat(gd.stack).isEmpty();
            verify(inputCompletionService).processMayAbilitiesThenAutoPass(gd);
        }
    }

    // ===== handlePlayFromLibraryOrExileChoice =====

    @Nested
    @DisplayName("handlePlayFromLibraryOrExileChoice")
    class HandlePlayFromLibraryOrExileChoice {

        @Test
        @DisplayName("Declining exiles the card from library")
        void decliningExilesCard() {
            Card card = createSorcery("Exiled Spell");
            gd.playerDecks.get(PLAYER1_ID).add(card);
            PendingMayAbility ability = abilityFor(card);

            svc.handlePlayFromLibraryOrExileChoice(gd, player1, false, ability);

            verify(exileService).exileCard(gd, PLAYER1_ID, card);
            assertThat(gd.playerDecks.get(PLAYER1_ID)).isEmpty();
            verify(inputCompletionService).processMayAbilitiesThenAutoPass(gd);
        }

        @Test
        @DisplayName("Card no longer on top returns early")
        void cardNoLongerOnTopReturnsEarly() {
            Card card = createSorcery("Gone Spell");
            Card differentCard = createSorcery("Different");
            gd.playerDecks.get(PLAYER1_ID).add(differentCard);
            PendingMayAbility ability = abilityFor(card);

            svc.handlePlayFromLibraryOrExileChoice(gd, player1, true, ability);

            assertThat(gd.stack).isEmpty();
            verify(inputCompletionService).processMayAbilitiesThenAutoPass(gd);
        }

        @Test
        @DisplayName("Land is played onto battlefield and increments land count")
        void landPlayedOntoBattlefield() {
            Card land = createLand("Forest");
            gd.playerDecks.get(PLAYER1_ID).add(land);
            PendingMayAbility ability = abilityFor(land);

            svc.handlePlayFromLibraryOrExileChoice(gd, player1, true, ability);

            assertThat(gd.playerDecks.get(PLAYER1_ID)).isEmpty();
            verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(PLAYER1_ID), any(Permanent.class));
            assertThat(gd.landsPlayedThisTurn.getOrDefault(PLAYER1_ID, 0)).isEqualTo(1);
            verify(inputCompletionService).processMayAbilitiesThenAutoPass(gd);
        }

        @Test
        @DisplayName("Non-targeted sorcery is put on stack")
        void nonTargetedSorceryPutOnStack() {
            Card card = createSorcery("Divination");
            card.addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
            gd.playerDecks.get(PLAYER1_ID).add(card);
            PendingMayAbility ability = abilityFor(card);

            svc.handlePlayFromLibraryOrExileChoice(gd, player1, true, ability);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
            verify(triggerCollectionService).checkSpellCastTriggers(gd, card, PLAYER1_ID, false);
        }

        @Test
        @DisplayName("Targeted spell with no valid targets exiles the card")
        void targetedSpellNoTargetsExilesCard() {
            Card card = createInstant("No Targets");
            CardEffect permanentOnly = new CardEffect() {
                @Override public boolean canTargetPermanent() { return true; }
            };
            card.addEffect(EffectSlot.SPELL, permanentOnly);
            gd.playerDecks.get(PLAYER1_ID).add(card);
            PendingMayAbility ability = abilityFor(card);

            svc.handlePlayFromLibraryOrExileChoice(gd, player1, true, ability);

            verify(exileService).exileCard(gd, PLAYER1_ID, card);
            assertThat(gd.stack).isEmpty();
        }

        @Test
        @DisplayName("Targeted spell with valid targets begins permanent choice")
        void targetedSpellWithTargetsBeginsChoice() {
            Card card = createSorcery("Tome Scour");
            card.addEffect(EffectSlot.SPELL, new MillTargetPlayerEffect(5));
            gd.playerDecks.get(PLAYER1_ID).add(card);
            PendingMayAbility ability = abilityFor(card);

            svc.handlePlayFromLibraryOrExileChoice(gd, player1, true, ability);

            verify(playerInputService).beginPermanentChoice(eq(gd), eq(PLAYER1_ID), anyList(), anyString());
            verify(inputCompletionService, never()).processMayAbilitiesThenAutoPass(gd);
        }
    }

    // ===== handleCastFromGraveyardChoice =====

    @Nested
    @DisplayName("handleCastFromGraveyardChoice")
    class HandleCastFromGraveyardChoice {

        @BeforeEach
        void allowGraveyardCasting() {
            org.mockito.Mockito.lenient().when(gameQueryService.canPlayersCastSpellsFromGraveyards(gd)).thenReturn(true);
        }

        private CastTargetInstantOrSorceryFromGraveyardEffect opponentGraveyardFree() {
            return new CastTargetInstantOrSorceryFromGraveyardEffect(GraveyardSearchScope.OPPONENT_GRAVEYARD, true);
        }

        private CastTargetInstantOrSorceryFromGraveyardEffect allGraveyardsFree() {
            return new CastTargetInstantOrSorceryFromGraveyardEffect(GraveyardSearchScope.ALL_GRAVEYARDS, true);
        }

        private CastTargetInstantOrSorceryFromGraveyardEffect controllerGraveyardPaid() {
            return new CastTargetInstantOrSorceryFromGraveyardEffect(GraveyardSearchScope.CONTROLLERS_GRAVEYARD, false);
        }

        @Test
        @DisplayName("Declining logs and calls processMayAbilitiesThenAutoPass")
        void decliningLogsAndProcesses() {
            Card card = createSorcery("Tome Scour");
            PendingMayAbility ability = abilityFor(card);

            svc.handleCastFromGraveyardChoice(gd, player1, false, ability, opponentGraveyardFree());

            verify(gameBroadcastService).logAndBroadcast(eq(gd), anyString());
            verify(inputCompletionService).processMayAbilitiesThenAutoPass(gd);
            verifyNoInteractions(permanentRemovalService);
        }

        @Test
        @DisplayName("Card no longer in graveyard logs and does not put on stack")
        void cardNoLongerInGraveyardLogsAndSkips() {
            Card card = createSorcery("Gone Spell");
            PendingMayAbility ability = abilityFor(card);
            when(gameQueryService.findCardInGraveyardById(gd, card.getId())).thenReturn(null);

            svc.handleCastFromGraveyardChoice(gd, player1, true, ability, opponentGraveyardFree());

            assertThat(gd.stack).isEmpty();
            verify(inputCompletionService).processMayAbilitiesThenAutoPass(gd);
        }

        @Test
        @DisplayName("Invalid scope logs and does not cast")
        void invalidScopeLogsAndSkips() {
            Card card = createSorcery("Tome Scour");
            PendingMayAbility ability = abilityFor(card);
            when(gameQueryService.findCardInGraveyardById(gd, card.getId())).thenReturn(card);
            // Card is in player1's own graveyard, but scope is OPPONENT_GRAVEYARD
            when(gameQueryService.findGraveyardOwnerById(gd, card.getId())).thenReturn(PLAYER1_ID);

            svc.handleCastFromGraveyardChoice(gd, player1, true, ability, opponentGraveyardFree());

            assertThat(gd.stack).isEmpty();
            verifyNoInteractions(permanentRemovalService);
            verify(inputCompletionService).processMayAbilitiesThenAutoPass(gd);
        }

        @Test
        @DisplayName("OPPONENT_GRAVEYARD scope accepts card from opponent's graveyard")
        void opponentScopeAcceptsOpponentGraveyard() {
            Card card = createSorcery("Divination");
            card.addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
            PendingMayAbility ability = abilityFor(card);
            when(gameQueryService.findCardInGraveyardById(gd, card.getId())).thenReturn(card);
            when(gameQueryService.findGraveyardOwnerById(gd, card.getId())).thenReturn(PLAYER2_ID);

            svc.handleCastFromGraveyardChoice(gd, player1, true, ability, opponentGraveyardFree());

            verify(permanentRemovalService).removeCardFromGraveyardById(gd, card.getId());
            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("CONTROLLERS_GRAVEYARD scope accepts card from controller's graveyard")
        void controllerScopeAcceptsControllerGraveyard() {
            Card card = createSorcery("Divination");
            card.addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
            PendingMayAbility ability = abilityFor(card);
            when(gameQueryService.findCardInGraveyardById(gd, card.getId())).thenReturn(card);
            when(gameQueryService.findGraveyardOwnerById(gd, card.getId())).thenReturn(PLAYER1_ID);

            svc.handleCastFromGraveyardChoice(gd, player1, true, ability, controllerGraveyardPaid());

            verify(permanentRemovalService).removeCardFromGraveyardById(gd, card.getId());
            assertThat(gd.stack).hasSize(1);
        }

        @Test
        @DisplayName("CONTROLLERS_GRAVEYARD scope rejects card from opponent's graveyard")
        void controllerScopeRejectsOpponentGraveyard() {
            Card card = createSorcery("Tome Scour");
            PendingMayAbility ability = abilityFor(card);
            when(gameQueryService.findCardInGraveyardById(gd, card.getId())).thenReturn(card);
            when(gameQueryService.findGraveyardOwnerById(gd, card.getId())).thenReturn(PLAYER2_ID);

            svc.handleCastFromGraveyardChoice(gd, player1, true, ability, controllerGraveyardPaid());

            assertThat(gd.stack).isEmpty();
            verifyNoInteractions(permanentRemovalService);
        }

        @Test
        @DisplayName("Non-targeted spell is removed from graveyard and put on stack")
        void nonTargetedSpellPutOnStack() {
            Card card = createSorcery("Divination");
            card.addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
            PendingMayAbility ability = abilityFor(card);
            when(gameQueryService.findCardInGraveyardById(gd, card.getId())).thenReturn(card);
            when(gameQueryService.findGraveyardOwnerById(gd, card.getId())).thenReturn(PLAYER2_ID);

            svc.handleCastFromGraveyardChoice(gd, player1, true, ability, allGraveyardsFree());

            verify(permanentRemovalService).removeCardFromGraveyardById(gd, card.getId());
            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
            assertThat(gd.stack.getFirst().getCard()).isEqualTo(card);
            verify(triggerCollectionService).checkSpellCastTriggers(gd, card, PLAYER1_ID, false);
            verify(inputCompletionService).processMayAbilitiesThenAutoPass(gd);
        }

        @Test
        @DisplayName("Non-targeted instant uses INSTANT_SPELL type")
        void nonTargetedInstantUsesInstantType() {
            Card card = createInstant("Opt");
            card.addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
            PendingMayAbility ability = abilityFor(card);
            when(gameQueryService.findCardInGraveyardById(gd, card.getId())).thenReturn(card);
            when(gameQueryService.findGraveyardOwnerById(gd, card.getId())).thenReturn(PLAYER2_ID);

            svc.handleCastFromGraveyardChoice(gd, player1, true, ability, opponentGraveyardFree());

            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        }

        @Test
        @DisplayName("Non-targeted spell clears priorityPassedBy")
        void nonTargetedSpellClearsPriorityPassedBy() {
            Card card = createSorcery("Divination");
            card.addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
            PendingMayAbility ability = abilityFor(card);
            when(gameQueryService.findCardInGraveyardById(gd, card.getId())).thenReturn(card);
            when(gameQueryService.findGraveyardOwnerById(gd, card.getId())).thenReturn(PLAYER2_ID);
            gd.priorityPassedBy.add(PLAYER1_ID);

            svc.handleCastFromGraveyardChoice(gd, player1, true, ability, allGraveyardsFree());

            assertThat(gd.priorityPassedBy).isEmpty();
        }

        @Test
        @DisplayName("Targeted spell with valid targets begins permanent choice")
        void targetedSpellWithTargetsBeginsChoice() {
            Card card = createSorcery("Tome Scour");
            card.addEffect(EffectSlot.SPELL, new MillTargetPlayerEffect(5));
            PendingMayAbility ability = abilityFor(card);
            when(gameQueryService.findCardInGraveyardById(gd, card.getId())).thenReturn(card);
            when(gameQueryService.findGraveyardOwnerById(gd, card.getId())).thenReturn(PLAYER2_ID);

            svc.handleCastFromGraveyardChoice(gd, player1, true, ability, opponentGraveyardFree());

            verify(playerInputService).beginPermanentChoice(eq(gd), eq(PLAYER1_ID), anyList(), anyString());
            verify(inputCompletionService, never()).processMayAbilitiesThenAutoPass(gd);
        }

        @Test
        @DisplayName("Targeted spell with no valid targets puts card back in graveyard")
        void targetedSpellNoTargetsPutsCardBackInGraveyard() {
            Card card = createInstant("No Targets");
            CardEffect permanentOnly = new CardEffect() {
                @Override public boolean canTargetPermanent() { return true; }
            };
            card.addEffect(EffectSlot.SPELL, permanentOnly);
            PendingMayAbility ability = abilityFor(card);
            when(gameQueryService.findCardInGraveyardById(gd, card.getId())).thenReturn(card);
            when(gameQueryService.findGraveyardOwnerById(gd, card.getId())).thenReturn(PLAYER2_ID);

            svc.handleCastFromGraveyardChoice(gd, player1, true, ability, opponentGraveyardFree());

            verify(graveyardService).addCardToGraveyard(gd, PLAYER2_ID, card);
            assertThat(gd.stack).isEmpty();
            verify(inputCompletionService).processMayAbilitiesThenAutoPass(gd);
        }

        @Test
        @DisplayName("Player-only targeting spell does not offer permanents as targets (the original bug)")
        void playerOnlyTargetingDoesNotOfferPermanents() {
            Card card = createSorcery("Tome Scour");
            card.addEffect(EffectSlot.SPELL, new MillTargetPlayerEffect(5));
            PendingMayAbility ability = abilityFor(card);
            when(gameQueryService.findCardInGraveyardById(gd, card.getId())).thenReturn(card);
            when(gameQueryService.findGraveyardOwnerById(gd, card.getId())).thenReturn(PLAYER2_ID);

            // Add a creature on the battlefield — should NOT be offered as a target
            Permanent creature = new Permanent(createCreature("Phantom Warrior"));
            gd.playerBattlefields.get(PLAYER1_ID).add(creature);

            svc.handleCastFromGraveyardChoice(gd, player1, true, ability, opponentGraveyardFree());

            // Verify the target list passed to beginPermanentChoice only contains player IDs
            @SuppressWarnings("unchecked")
            var targetsCaptor = org.mockito.ArgumentCaptor.forClass(List.class);
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(PLAYER1_ID), targetsCaptor.capture(), anyString());

            @SuppressWarnings("unchecked")
            List<UUID> capturedTargets = (List<UUID>) targetsCaptor.getValue();
            assertThat(capturedTargets).containsExactlyInAnyOrder(PLAYER1_ID, PLAYER2_ID);
            assertThat(capturedTargets).doesNotContain(creature.getId());
        }
    }
}
