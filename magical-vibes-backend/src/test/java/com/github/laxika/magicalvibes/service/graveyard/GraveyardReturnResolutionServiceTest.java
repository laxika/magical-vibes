package com.github.laxika.magicalvibes.service.graveyard;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardIsAuraPredicate;
import com.github.laxika.magicalvibes.model.filter.CardKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.LifeResolutionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GraveyardReturnResolutionServiceTest {

    @Mock
    private BattlefieldEntryService battlefieldEntryService;

    @Mock
    private PermanentRemovalService permanentRemovalService;

    @Mock
    private LegendRuleService legendRuleService;

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameBroadcastService gameBroadcastService;

    @Mock
    private PlayerInputService playerInputService;

    @Mock
    private LifeResolutionService lifeResolutionService;

    @Mock
    private ExileService exileService;

    @InjectMocks
    private GraveyardReturnResolutionService service;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

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
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerExiledCards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerExiledCards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    // =========================================================================
    // describeFilter — static utility method
    // =========================================================================

    @Nested
    @DisplayName("describeFilter")
    class DescribeFilterTests {

        @Test
        @DisplayName("null predicate returns 'card'")
        void nullPredicateReturnsCard() {
            assertThat(CardPredicateUtils.describeFilter(null)).isEqualTo("card");
        }

        @Test
        @DisplayName("CardTypePredicate(CREATURE) returns 'creature card'")
        void cardTypePredicateCreature() {
            CardPredicate predicate = new CardTypePredicate(CardType.CREATURE);
            assertThat(CardPredicateUtils.describeFilter(predicate)).isEqualTo("creature card");
        }

        @Test
        @DisplayName("CardTypePredicate(ARTIFACT) returns 'artifact card'")
        void cardTypePredicateArtifact() {
            CardPredicate predicate = new CardTypePredicate(CardType.ARTIFACT);
            assertThat(CardPredicateUtils.describeFilter(predicate)).isEqualTo("artifact card");
        }

        @Test
        @DisplayName("CardSubtypePredicate returns subtype display name + 'card'")
        void cardSubtypePredicate() {
            CardPredicate predicate = new CardSubtypePredicate(CardSubtype.ZOMBIE);
            assertThat(CardPredicateUtils.describeFilter(predicate)).isEqualTo("Zombie card");
        }

        @Test
        @DisplayName("CardKeywordPredicate returns 'card with <keyword>'")
        void cardKeywordPredicate() {
            CardPredicate predicate = new CardKeywordPredicate(Keyword.INFECT);
            assertThat(CardPredicateUtils.describeFilter(predicate)).isEqualTo("card with infect");
        }

        @Test
        @DisplayName("CardKeywordPredicate with multi-word keyword uses spaces")
        void cardKeywordPredicateMultiWord() {
            CardPredicate predicate = new CardKeywordPredicate(Keyword.FIRST_STRIKE);
            assertThat(CardPredicateUtils.describeFilter(predicate)).isEqualTo("card with first strike");
        }

        @Test
        @DisplayName("CardIsAuraPredicate returns 'Aura card'")
        void cardIsAuraPredicate() {
            CardPredicate predicate = new CardIsAuraPredicate();
            assertThat(CardPredicateUtils.describeFilter(predicate)).isEqualTo("Aura card");
        }

        @Test
        @DisplayName("CardAllOfPredicate combines descriptions merging 'card with' parts")
        void cardAllOfPredicateMerges() {
            CardPredicate predicate = new CardAllOfPredicate(List.of(
                    new CardTypePredicate(CardType.CREATURE),
                    new CardKeywordPredicate(Keyword.INFECT)
            ));
            assertThat(CardPredicateUtils.describeFilter(predicate))
                    .isEqualTo("creature card with infect");
        }

        @Test
        @DisplayName("CardAnyOfPredicate with same suffix merges to 'X or Y card'")
        void cardAnyOfPredicateMergesSuffix() {
            CardPredicate predicate = new CardAnyOfPredicate(List.of(
                    new CardTypePredicate(CardType.ARTIFACT),
                    new CardTypePredicate(CardType.CREATURE)
            ));
            assertThat(CardPredicateUtils.describeFilter(predicate))
                    .isEqualTo("artifact or creature card");
        }

        @Test
        @DisplayName("CardAnyOfPredicate with mixed predicates joins with 'or'")
        void cardAnyOfPredicateMixed() {
            CardPredicate predicate = new CardAnyOfPredicate(List.of(
                    new CardTypePredicate(CardType.CREATURE),
                    new CardIsAuraPredicate()
            ));
            assertThat(CardPredicateUtils.describeFilter(predicate))
                    .isEqualTo("creature or Aura card");
        }
    }

    // =========================================================================
    // ReturnTargetCardsFromGraveyardToHandEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveReturnTargetCardsFromGraveyardToHand")
    class ReturnTargetCardsToHandTests {

        @Test
        @DisplayName("Returns multiple targeted cards from graveyard to hand")
        void returnsMultipleTargetedCardsToHand() {
            Card creature1 = createCard("Grizzly Bears");
            Card creature2 = createCard("Llanowar Elves");
            gd.playerGraveyards.get(player1Id).addAll(List.of(creature1, creature2));

            ReturnTargetCardsFromGraveyardToHandEffect effect =
                    new ReturnTargetCardsFromGraveyardToHandEffect(null, 2);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Morbid Plunder"),
                    player1Id, "Morbid Plunder", List.of(effect),
                    List.of(creature1.getId(), creature2.getId()));

            when(gameQueryService.findCardInGraveyardById(gd, creature1.getId())).thenReturn(creature1);
            when(gameQueryService.findCardInGraveyardById(gd, creature2.getId())).thenReturn(creature2);

            service.resolveReturnTargetCardsFromGraveyardToHand(gd, entry, effect);

            assertThat(gd.playerGraveyards.get(player1Id)).isEmpty();
            assertThat(gd.playerHands.get(player1Id)).extracting(Card::getName)
                    .containsExactlyInAnyOrder("Grizzly Bears", "Llanowar Elves");
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("Grizzly Bears") && msg.contains("Llanowar Elves")
                            && msg.contains("graveyard to hand")));
        }

        @Test
        @DisplayName("Does nothing when no targets are selected")
        void doesNothingWhenNoTargets() {
            ReturnTargetCardsFromGraveyardToHandEffect effect =
                    new ReturnTargetCardsFromGraveyardToHandEffect(null, 2);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Morbid Plunder"),
                    player1Id, "Morbid Plunder", List.of(effect),
                    List.of());

            service.resolveReturnTargetCardsFromGraveyardToHand(gd, entry, effect);

            verify(gameBroadcastService, never()).logAndBroadcast(any(), any());
        }

        @Test
        @DisplayName("Silently skips cards no longer in graveyard")
        void skipsCardsNoLongerInGraveyard() {
            Card creature1 = createCard("Grizzly Bears");
            Card creature2 = createCard("Llanowar Elves");
            // Only creature1 is still in graveyard
            gd.playerGraveyards.get(player1Id).add(creature1);

            ReturnTargetCardsFromGraveyardToHandEffect effect =
                    new ReturnTargetCardsFromGraveyardToHandEffect(null, 2);
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Morbid Plunder"),
                    player1Id, "Morbid Plunder", List.of(effect),
                    List.of(creature1.getId(), creature2.getId()));

            when(gameQueryService.findCardInGraveyardById(gd, creature1.getId())).thenReturn(creature1);
            when(gameQueryService.findCardInGraveyardById(gd, creature2.getId())).thenReturn(null);

            service.resolveReturnTargetCardsFromGraveyardToHand(gd, entry, effect);

            assertThat(gd.playerHands.get(player1Id)).extracting(Card::getName)
                    .containsExactly("Grizzly Bears");
            assertThat(gd.playerGraveyards.get(player1Id)).isEmpty();
        }
    }

    // =========================================================================
    // PutTargetCardsFromGraveyardOnTopOfLibraryEffect
    // =========================================================================

    @Nested
    @DisplayName("resolvePutTargetCardsFromGraveyardOnTopOfLibrary")
    class PutCardsOnTopOfLibraryTests {

        @Test
        @DisplayName("Moves targeted cards from graveyard to top of library")
        void movesCardsToTopOfLibrary() {
            Card artifact1 = createCard("Leonin Scimitar");
            Card artifact2 = createCard("Rod of Ruin");
            gd.playerGraveyards.get(player1Id).addAll(List.of(artifact1, artifact2));

            PutTargetCardsFromGraveyardOnTopOfLibraryEffect effect =
                    new PutTargetCardsFromGraveyardOnTopOfLibraryEffect(new CardTypePredicate(CardType.ARTIFACT));
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, createCard("Frantic Salvage"),
                    player1Id, "Frantic Salvage", List.of(effect),
                    List.of(artifact1.getId(), artifact2.getId()));

            when(gameQueryService.findCardInGraveyardById(gd, artifact1.getId())).thenReturn(artifact1);
            when(gameQueryService.findCardInGraveyardById(gd, artifact2.getId())).thenReturn(artifact2);

            service.resolvePutTargetCardsFromGraveyardOnTopOfLibrary(gd, entry, effect);

            assertThat(gd.playerGraveyards.get(player1Id)).isEmpty();
            assertThat(gd.playerDecks.get(player1Id)).extracting(Card::getName)
                    .containsExactlyInAnyOrder("Leonin Scimitar", "Rod of Ruin");
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("on top of their library") && msg.contains("from graveyard")));
        }

        @Test
        @DisplayName("Silently skips cards no longer in graveyard")
        void skipsCardsNoLongerInGraveyard() {
            Card artifact = createCard("Leonin Scimitar");
            // Card is no longer in graveyard

            PutTargetCardsFromGraveyardOnTopOfLibraryEffect effect =
                    new PutTargetCardsFromGraveyardOnTopOfLibraryEffect(new CardTypePredicate(CardType.ARTIFACT));
            StackEntry entry = new StackEntry(StackEntryType.INSTANT_SPELL, createCard("Frantic Salvage"),
                    player1Id, "Frantic Salvage", List.of(effect),
                    List.of(artifact.getId()));

            when(gameQueryService.findCardInGraveyardById(gd, artifact.getId())).thenReturn(null);

            service.resolvePutTargetCardsFromGraveyardOnTopOfLibrary(gd, entry, effect);

            assertThat(gd.playerDecks.get(player1Id)).isEmpty();
            verify(gameBroadcastService, never()).logAndBroadcast(any(), any());
        }
    }

    // =========================================================================
    // PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect
    // =========================================================================

    @Nested
    @DisplayName("resolvePutCreatureFromOpponentGraveyardWithExile")
    class PutFromOpponentGraveyardWithExileTests {

        @Test
        @DisplayName("Puts creature from opponent's graveyard onto battlefield with haste")
        void putsCreatureWithHaste() {
            Card target = createCard("Grizzly Bears");

            PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect effect =
                    new PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Gruesome Encore"),
                    player1Id, "Gruesome Encore", List.of(effect), 0,
                    target.getId(), null);

            when(gameQueryService.findCardInGraveyardById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findGraveyardOwnerById(gd, target.getId())).thenReturn(player2Id);
            when(battlefieldEntryService.snapshotEnterTappedTypes(gd)).thenReturn(Set.of());
            lenient().when(gameQueryService.isCreature(eq(gd), any(Permanent.class))).thenReturn(true);

            service.resolvePutCreatureFromOpponentGraveyardWithExile(gd, entry);

            verify(permanentRemovalService).removeCardFromGraveyardById(gd, target.getId());
            verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player1Id),
                    argThat(p -> p.getGrantedKeywords().contains(Keyword.HASTE)
                            && p.isExileIfLeavesBattlefield()),
                    eq(Set.of()));
            assertThat(gd.pendingTokenExilesAtEndStep).isNotEmpty();
            assertThat(gd.stolenCreatures).isNotEmpty();
            assertThat(gd.permanentControlStolenCreatures).isNotEmpty();
        }

        @Test
        @DisplayName("Fizzles if target card is no longer in graveyard")
        void fizzlesIfTargetGone() {
            UUID targetId = UUID.randomUUID();

            PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect effect =
                    new PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Gruesome Encore"),
                    player1Id, "Gruesome Encore", List.of(effect), 0,
                    targetId, null);

            when(gameQueryService.findCardInGraveyardById(gd, targetId)).thenReturn(null);

            service.resolvePutCreatureFromOpponentGraveyardWithExile(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg -> msg.contains("fizzles")));
            verify(battlefieldEntryService, never()).putPermanentOntoBattlefield(
                    any(), any(), any(Permanent.class), any());
        }

        @Test
        @DisplayName("Fizzles if target is in controller's own graveyard")
        void fizzlesIfTargetInOwnGraveyard() {
            Card target = createCard("Grizzly Bears");

            PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect effect =
                    new PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Gruesome Encore"),
                    player1Id, "Gruesome Encore", List.of(effect), 0,
                    target.getId(), null);

            when(gameQueryService.findCardInGraveyardById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findGraveyardOwnerById(gd, target.getId())).thenReturn(player1Id);

            service.resolvePutCreatureFromOpponentGraveyardWithExile(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    argThat(msg -> msg.contains("fizzles") && msg.contains("not in opponent's graveyard")));
            verify(battlefieldEntryService, never()).putPermanentOntoBattlefield(
                    any(), any(), any(Permanent.class), any());
        }
    }

    // =========================================================================
    // ExileTargetPlayerGraveyardEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveExileTargetPlayerGraveyard")
    class ExileTargetPlayerGraveyardTests {

        @Test
        @DisplayName("Exiles all cards from target player's graveyard")
        void exilesEntireGraveyard() {
            Card creature = createCard("Grizzly Bears");
            Card artifact = createCard("Leonin Scimitar");
            gd.playerGraveyards.get(player2Id).addAll(List.of(creature, artifact));

            ExileTargetPlayerGraveyardEffect effect = new ExileTargetPlayerGraveyardEffect();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Nihil Spellbomb"),
                    player1Id, "Nihil Spellbomb", List.of(effect), 0,
                    player2Id, null);

            service.resolveExileTargetPlayerGraveyard(gd, entry);

            assertThat(gd.playerGraveyards.get(player2Id)).isEmpty();
            assertThat(gd.playerExiledCards.get(player2Id))
                    .extracting(Card::getName)
                    .containsExactlyInAnyOrder("Grizzly Bears", "Leonin Scimitar");
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("exiled") && msg.contains("2 cards")));
        }

        @Test
        @DisplayName("Logs message when targeting empty graveyard")
        void logsMessageForEmptyGraveyard() {
            ExileTargetPlayerGraveyardEffect effect = new ExileTargetPlayerGraveyardEffect();
            StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, createCard("Nihil Spellbomb"),
                    player1Id, "Nihil Spellbomb", List.of(effect), 0,
                    player2Id, null);

            service.resolveExileTargetPlayerGraveyard(gd, entry);

            assertThat(gd.playerExiledCards.get(player2Id)).isEmpty();
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("already empty")));
        }
    }

    // =========================================================================
    // ExileCardsFromGraveyardEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveExileCardsFromGraveyard")
    class ExileCardsFromGraveyardTests {

        @Test
        @DisplayName("Exiles targeted cards still in graveyards")
        void exilesTargetedCards() {
            Card creature = createCard("Grizzly Bears");
            Card artifact = createCard("Leonin Scimitar");
            gd.playerGraveyards.get(player1Id).add(creature);
            gd.playerGraveyards.get(player2Id).add(artifact);

            ExileCardsFromGraveyardEffect effect = new ExileCardsFromGraveyardEffect(2, 0);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Scavenging Ooze"),
                    player1Id, "Scavenging Ooze", List.of(effect),
                    List.of(creature.getId(), artifact.getId()));

            when(gameQueryService.findCardInGraveyardById(gd, creature.getId())).thenReturn(creature);
            when(gameQueryService.findCardInGraveyardById(gd, artifact.getId())).thenReturn(artifact);

            service.resolveExileCardsFromGraveyard(gd, entry, effect);

            verify(exileService).exileCard(gd, player1Id, creature);
            verify(exileService).exileCard(gd, player2Id, artifact);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("exiles") && msg.contains("Grizzly Bears")
                            && msg.contains("Leonin Scimitar")));
        }

        @Test
        @DisplayName("Gains life after exiling when lifeGain is positive")
        void gainsLifeAfterExiling() {
            Card creature = createCard("Grizzly Bears");
            gd.playerGraveyards.get(player1Id).add(creature);

            ExileCardsFromGraveyardEffect effect = new ExileCardsFromGraveyardEffect(1, 3);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Scavenging Ooze"),
                    player1Id, "Scavenging Ooze", List.of(effect),
                    List.of(creature.getId()));

            when(gameQueryService.findCardInGraveyardById(gd, creature.getId())).thenReturn(creature);

            service.resolveExileCardsFromGraveyard(gd, entry, effect);

            verify(exileService).exileCard(gd, player1Id, creature);
            verify(lifeResolutionService).applyGainLife(gd, player1Id, 3);
        }

        @Test
        @DisplayName("Skips cards no longer in graveyard")
        void skipsCardsNoLongerInGraveyard() {
            UUID goneCardId = UUID.randomUUID();

            ExileCardsFromGraveyardEffect effect = new ExileCardsFromGraveyardEffect(1, 0);
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Scavenging Ooze"),
                    player1Id, "Scavenging Ooze", List.of(effect),
                    List.of(goneCardId));

            when(gameQueryService.findCardInGraveyardById(gd, goneCardId)).thenReturn(null);

            service.resolveExileCardsFromGraveyard(gd, entry, effect);

            verify(exileService, never()).exileCard(any(), any(), any());
            verify(gameBroadcastService, never()).logAndBroadcast(any(), any());
        }
    }

    // =========================================================================
    // ReturnCardFromGraveyardEffect — pre-targeted to hand
    // =========================================================================

    @Nested
    @DisplayName("resolveReturnCardFromGraveyard — pre-targeted")
    class PreTargetedReturnTests {

        @Test
        @DisplayName("Returns pre-targeted creature card from graveyard to hand")
        void returnsPreTargetedCreatureToHand() {
            Card creature = createCard("Grizzly Bears");

            ReturnCardFromGraveyardEffect effect = ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.HAND)
                    .targetGraveyard(true)
                    .build();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Recover"),
                    player1Id, "Recover", new ArrayList<>(List.of(effect)),
                    creature.getId(), Zone.GRAVEYARD);

            when(gameQueryService.findCardInGraveyardById(gd, creature.getId())).thenReturn(creature);

            service.resolveReturnCardFromGraveyard(gd, entry, effect);

            verify(permanentRemovalService).removeCardFromGraveyardById(gd, creature.getId());
            assertThat(gd.playerHands.get(player1Id)).extracting(Card::getName)
                    .containsExactly("Grizzly Bears");
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("Grizzly Bears") && msg.contains("graveyard to hand")));
        }

        @Test
        @DisplayName("Gains life equal to mana value when effect specifies it")
        void gainsLifeEqualToManaValue() {
            Card creature = createCard("Grizzly Bears");
            creature.setManaCost("{1}{G}");

            ReturnCardFromGraveyardEffect effect = ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.HAND)
                    .targetGraveyard(true)
                    .gainLifeEqualToManaValue(true)
                    .build();
            StackEntry entry = new StackEntry(StackEntryType.TRIGGERED_ABILITY, createCard("Razor Hippogriff"),
                    player1Id, "Razor Hippogriff", new ArrayList<>(List.of(effect)),
                    creature.getId(), Zone.GRAVEYARD);

            when(gameQueryService.findCardInGraveyardById(gd, creature.getId())).thenReturn(creature);

            service.resolveReturnCardFromGraveyard(gd, entry, effect);

            verify(permanentRemovalService).removeCardFromGraveyardById(gd, creature.getId());
            verify(lifeResolutionService).applyGainLife(gd, player1Id, 2);
        }

        @Test
        @DisplayName("Fizzles when pre-targeted card is no longer in graveyard")
        void fizzlesWhenTargetGone() {
            UUID targetId = UUID.randomUUID();

            ReturnCardFromGraveyardEffect effect = ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.HAND)
                    .targetGraveyard(true)
                    .build();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Recover"),
                    player1Id, "Recover", new ArrayList<>(List.of(effect)),
                    targetId, Zone.GRAVEYARD);

            when(gameQueryService.findCardInGraveyardById(gd, targetId)).thenReturn(null);

            service.resolveReturnCardFromGraveyard(gd, entry, effect);

            assertThat(gd.playerHands.get(player1Id)).isEmpty();
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("fizzles")));
        }
    }

    // =========================================================================
    // ReturnCardFromGraveyardEffect — search all graveyards
    // =========================================================================

    @Nested
    @DisplayName("resolveReturnCardFromGraveyard — all graveyards search")
    class AllGraveyardsSearchTests {

        @Test
        @DisplayName("Prompts graveyard choice when matching cards exist in any graveyard")
        void promptsChoiceWhenMatchingCardsExist() {
            Card creature = createCard("Grizzly Bears");
            Card artifact = createCard("Leonin Scimitar");
            gd.playerGraveyards.get(player1Id).add(creature);
            gd.playerGraveyards.get(player2Id).add(artifact);

            CardPredicate filter = new CardAnyOfPredicate(List.of(
                    new CardTypePredicate(CardType.ARTIFACT),
                    new CardTypePredicate(CardType.CREATURE)
            ));
            ReturnCardFromGraveyardEffect effect = ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.BATTLEFIELD)
                    .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                    .filter(filter)
                    .build();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Beacon of Unrest"),
                    player1Id, "Beacon of Unrest", new ArrayList<>(List.of(effect)));

            when(gameQueryService.matchesCardPredicate(eq(creature), eq(filter), any())).thenReturn(true);
            when(gameQueryService.matchesCardPredicate(eq(artifact), eq(filter), any())).thenReturn(true);

            service.resolveReturnCardFromGraveyard(gd, entry, effect);

            verify(playerInputService).beginGraveyardChoice(eq(gd), eq(player1Id), any(), any());
        }

        @Test
        @DisplayName("Logs message and removes shuffle effect when no matching cards in any graveyard")
        void logsMessageWhenNoMatchingCards() {
            CardPredicate filter = new CardAnyOfPredicate(List.of(
                    new CardTypePredicate(CardType.ARTIFACT),
                    new CardTypePredicate(CardType.CREATURE)
            ));
            ReturnCardFromGraveyardEffect effect = ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.BATTLEFIELD)
                    .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                    .filter(filter)
                    .build();
            ShuffleIntoLibraryEffect shuffleEffect = new ShuffleIntoLibraryEffect();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Beacon of Unrest"),
                    player1Id, "Beacon of Unrest", new ArrayList<>(List.of(effect, shuffleEffect)));

            service.resolveReturnCardFromGraveyard(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no ") && msg.contains("in any graveyard")));
            // Shuffle effect should be removed when no valid targets
            assertThat(entry.getEffectsToResolve()).noneMatch(e -> e instanceof ShuffleIntoLibraryEffect);
        }

        @Test
        @DisplayName("Does not prompt when all graveyard cards fail predicate")
        void doesNotPromptWhenNoCardsMatchPredicate() {
            Card creature = createCard("Grizzly Bears");
            gd.playerGraveyards.get(player1Id).add(creature);

            CardPredicate filter = new CardTypePredicate(CardType.ARTIFACT);
            ReturnCardFromGraveyardEffect effect = ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.BATTLEFIELD)
                    .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                    .filter(filter)
                    .build();
            StackEntry entry = new StackEntry(StackEntryType.SORCERY_SPELL, createCard("Beacon of Unrest"),
                    player1Id, "Beacon of Unrest", new ArrayList<>(List.of(effect)));

            when(gameQueryService.matchesCardPredicate(eq(creature), eq(filter), any())).thenReturn(false);

            service.resolveReturnCardFromGraveyard(gd, entry, effect);

            verify(playerInputService, never()).beginGraveyardChoice(any(), any(), any(), any());
            verify(gameBroadcastService).logAndBroadcast(eq(gd), argThat(msg ->
                    msg.contains("no ") && msg.contains("in any graveyard")));
        }
    }
}
