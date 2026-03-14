package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfAndReturnAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndReturnAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.ImprintDyingCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.service.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExileResolutionServiceTest extends BaseCardTest {

    @Mock
    private GraveyardService graveyardService;
    @Mock
    private GameQueryService gameQueryService;
    @Mock
    private GameBroadcastService gameBroadcastService;
    @Mock
    private PermanentRemovalService permanentRemovalService;
    @Mock
    private PlayerInputService playerInputService;
    @Mock
    private CardViewFactory cardViewFactory;
    @Mock
    private TriggerCollectionService triggerCollectionService;
    @Mock
    private BattlefieldEntryService battlefieldEntryService;
    @Spy
    private ExileService exileService = new ExileService();

    @InjectMocks
    private ExileResolutionService exileResolutionService;

    private static Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.ARTIFACT);
        return card;
    }

    private static Card createCreatureCard(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Permanent addPermanent(UUID playerId, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(playerId).add(perm);
        return perm;
    }

    private StackEntry createSingleTargetEntry(Card sourceCard, UUID controllerId, UUID targetPermanentId) {
        return new StackEntry(
                StackEntryType.SORCERY_SPELL, sourceCard, controllerId, sourceCard.getName(),
                List.of(new ExileTargetPermanentEffect()), 0, targetPermanentId, null
        );
    }

    private StackEntry createMultiTargetEntry(Card sourceCard, UUID controllerId, List<UUID> targetIds) {
        return new StackEntry(
                StackEntryType.INSTANT_SPELL, sourceCard, controllerId, sourceCard.getName(),
                List.of(new ExileTargetPermanentEffect()), 0, targetIds
        );
    }

    // =========================================================================
    // ExileTargetPermanentEffect — single target
    // =========================================================================

    @Nested
    @DisplayName("resolveExileTargetPermanent — single target")
    class ResolveExileTargetPermanentSingle {

        @Test
        @DisplayName("Exiles target permanent and logs the exile")
        void exilesTargetPermanent() {
            Card targetCard = createCard("Spellbook");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCard("Revoke Existence");

            StackEntry entry = createSingleTargetEntry(sourceCard, player1.getId(), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);

            exileResolutionService.resolveExileTargetPermanent(gd, entry);

            verify(permanentRemovalService).removePermanentToExile(gd, target);
            verify(gameBroadcastService).logAndBroadcast(eq(gd), eq("Spellbook is exiled."));
            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Does nothing when target permanent is removed before resolution")
        void fizzlesWhenTargetRemoved() {
            UUID targetId = UUID.randomUUID();
            Card sourceCard = createCard("Revoke Existence");

            StackEntry entry = createSingleTargetEntry(sourceCard, player1.getId(), targetId);

            when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(null);

            exileResolutionService.resolveExileTargetPermanent(gd, entry);

            verify(permanentRemovalService, never()).removePermanentToExile(any(), any());
            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }
    }

    // =========================================================================
    // ExileTargetPermanentEffect — multi-target
    // =========================================================================

    @Nested
    @DisplayName("resolveExileTargetPermanent — multi-target")
    class ResolveExileTargetPermanentMulti {

        @Test
        @DisplayName("Exiles two target artifacts")
        void exilesTwoTargetArtifacts() {
            Card targetCard1 = createCard("Spellbook");
            Permanent target1 = new Permanent(targetCard1);
            Card targetCard2 = createCard("Spellbook");
            Permanent target2 = new Permanent(targetCard2);
            Card sourceCard = createCard("Into the Core");

            StackEntry entry = createMultiTargetEntry(sourceCard, player1.getId(),
                    List.of(target1.getId(), target2.getId()));

            when(gameQueryService.findPermanentById(gd, target1.getId())).thenReturn(target1);
            when(gameQueryService.findPermanentById(gd, target2.getId())).thenReturn(target2);

            exileResolutionService.resolveExileTargetPermanent(gd, entry);

            verify(permanentRemovalService).removePermanentToExile(gd, target1);
            verify(permanentRemovalService).removePermanentToExile(gd, target2);
            verify(gameBroadcastService, times(2)).logAndBroadcast(eq(gd), eq("Spellbook is exiled."));
            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Skips targets that were removed before resolution and exiles the rest")
        void skipsRemovedTargets() {
            UUID removedId = UUID.randomUUID();
            Card targetCard = createCard("Spellbook");
            Permanent target2 = new Permanent(targetCard);
            Card sourceCard = createCard("Into the Core");

            StackEntry entry = createMultiTargetEntry(sourceCard, player1.getId(),
                    List.of(removedId, target2.getId()));

            when(gameQueryService.findPermanentById(gd, removedId)).thenReturn(null);
            when(gameQueryService.findPermanentById(gd, target2.getId())).thenReturn(target2);

            exileResolutionService.resolveExileTargetPermanent(gd, entry);

            verify(permanentRemovalService).removePermanentToExile(gd, target2);
            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }
    }

    // =========================================================================
    // ExileTargetPermanentAndReturnAtEndStepEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveExileTargetPermanentAndReturnAtEndStep")
    class ResolveExileTargetPermanentAndReturnAtEndStep {

        private StackEntry createEntry(Card sourceCard, UUID controllerId, UUID targetPermanentId) {
            return new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, sourceCard, controllerId, sourceCard.getName(),
                    List.of(new ExileTargetPermanentAndReturnAtEndStepEffect()), 0, targetPermanentId, null
            );
        }

        @Test
        @DisplayName("Exiles target permanent and adds a pending exile return")
        void exilesAndSchedulesReturn() {
            Card targetCard = createCreatureCard("Grizzly Bears");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCreatureCard("Glimmerpoint Stag");

            StackEntry entry = createEntry(sourceCard, player1.getId(), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1.getId());

            exileResolutionService.resolveExileTargetPermanentAndReturnAtEndStep(gd, entry);

            verify(permanentRemovalService).removePermanentToExile(gd, target);
            assertThat(gd.pendingExileReturns)
                    .anyMatch(per -> per.card().getName().equals("Grizzly Bears")
                            && per.controllerId().equals(player1.getId()));
        }

        @Test
        @DisplayName("Does nothing when target is removed before resolution")
        void fizzlesWhenTargetRemoved() {
            UUID targetId = UUID.randomUUID();
            Card sourceCard = createCreatureCard("Glimmerpoint Stag");

            StackEntry entry = createEntry(sourceCard, player1.getId(), targetId);

            when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(null);

            exileResolutionService.resolveExileTargetPermanentAndReturnAtEndStep(gd, entry);

            verify(permanentRemovalService, never()).removePermanentToExile(any(), any());
            assertThat(gd.pendingExileReturns).isEmpty();
        }

        @Test
        @DisplayName("Stolen creature's pending return uses original owner")
        void stolenCreatureUsesOriginalOwner() {
            Card targetCard = createCreatureCard("Grizzly Bears");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCreatureCard("Glimmerpoint Stag");

            // Mark target as stolen from player2
            gd.stolenCreatures.put(target.getId(), player2.getId());

            StackEntry entry = createEntry(sourceCard, player1.getId(), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1.getId());

            exileResolutionService.resolveExileTargetPermanentAndReturnAtEndStep(gd, entry);

            assertThat(gd.pendingExileReturns)
                    .anyMatch(per -> per.card().getName().equals("Grizzly Bears")
                            && per.controllerId().equals(player2.getId()));
        }

        @Test
        @DisplayName("Logs exile and return message")
        void logsExileAndReturn() {
            Card targetCard = createCreatureCard("Grizzly Bears");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCreatureCard("Glimmerpoint Stag");

            StackEntry entry = createEntry(sourceCard, player1.getId(), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1.getId());

            exileResolutionService.resolveExileTargetPermanentAndReturnAtEndStep(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Grizzly Bears is exiled. It will return at the beginning of the next end step."));
        }
    }

    // =========================================================================
    // ExileSelfAndReturnAtEndStepEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveExileSelfAndReturnAtEndStep")
    class ResolveExileSelfAndReturnAtEndStep {

        private StackEntry createEntry(Card sourceCard, UUID controllerId, UUID sourcePermanentId) {
            return new StackEntry(
                    StackEntryType.ACTIVATED_ABILITY, sourceCard, controllerId, sourceCard.getName(),
                    List.of(new ExileSelfAndReturnAtEndStepEffect()), null, sourcePermanentId
            );
        }

        @Test
        @DisplayName("Exiles self and adds a pending exile return for the controller")
        void exilesSelfAndSchedulesReturn() {
            Card sourceCard = createCreatureCard("Argent Sphinx");
            Permanent source = new Permanent(sourceCard);

            StackEntry entry = createEntry(sourceCard, player1.getId(), source.getId());

            when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);

            exileResolutionService.resolveExileSelfAndReturnAtEndStep(gd, entry);

            verify(permanentRemovalService).removePermanentToExile(gd, source);
            assertThat(gd.pendingExileReturns)
                    .anyMatch(per -> per.card().getName().equals("Argent Sphinx")
                            && per.controllerId().equals(player1.getId()));
        }

        @Test
        @DisplayName("Does nothing when the source permanent is removed before resolution")
        void fizzlesWhenSourceRemoved() {
            UUID sourceId = UUID.randomUUID();
            Card sourceCard = createCreatureCard("Argent Sphinx");

            StackEntry entry = createEntry(sourceCard, player1.getId(), sourceId);

            when(gameQueryService.findPermanentById(gd, sourceId)).thenReturn(null);

            exileResolutionService.resolveExileSelfAndReturnAtEndStep(gd, entry);

            verify(permanentRemovalService, never()).removePermanentToExile(any(), any());
            assertThat(gd.pendingExileReturns).isEmpty();
        }

        @Test
        @DisplayName("Logs exile and return message")
        void logsExileAndReturn() {
            Card sourceCard = createCreatureCard("Argent Sphinx");
            Permanent source = new Permanent(sourceCard);

            StackEntry entry = createEntry(sourceCard, player1.getId(), source.getId());

            when(gameQueryService.findPermanentById(gd, source.getId())).thenReturn(source);

            exileResolutionService.resolveExileSelfAndReturnAtEndStep(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Argent Sphinx is exiled. It will return at the beginning of the next end step."));
        }
    }

    // =========================================================================
    // ExileTargetPermanentUntilSourceLeavesEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveExileTargetPermanentUntilSourceLeaves")
    class ResolveExileTargetPermanentUntilSourceLeaves {

        private StackEntry createEntry(Card sourceCard, UUID controllerId, UUID targetPermanentId) {
            return new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, sourceCard, controllerId, sourceCard.getName(),
                    List.of(new ExileTargetPermanentUntilSourceLeavesEffect()), 0, targetPermanentId, null
            );
        }

        @Test
        @DisplayName("Exiles target permanent and tracks return on source leave")
        void exilesTargetAndTracksReturn() {
            Card targetCard = createCard("Spellbook");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCreatureCard("Leonin Relic-Warder");
            Permanent source = addPermanent(player1.getId(), sourceCard);

            StackEntry entry = createEntry(sourceCard, player1.getId(), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2.getId());

            exileResolutionService.resolveExileTargetPermanentUntilSourceLeaves(gd, entry);

            verify(permanentRemovalService).removePermanentToExile(gd, target);
            assertThat(gd.exileReturnOnPermanentLeave).isNotEmpty();
            assertThat(gd.exileReturnOnPermanentLeave.get(source.getId()))
                    .isNotNull()
                    .satisfies(per -> {
                        assertThat(per.card().getName()).isEqualTo("Spellbook");
                        assertThat(per.controllerId()).isEqualTo(player2.getId());
                    });
        }

        @Test
        @DisplayName("Does nothing when target permanent is removed before resolution")
        void fizzlesWhenTargetRemoved() {
            UUID targetId = UUID.randomUUID();
            Card sourceCard = createCreatureCard("Leonin Relic-Warder");

            StackEntry entry = createEntry(sourceCard, player1.getId(), targetId);

            when(gameQueryService.findPermanentById(gd, targetId)).thenReturn(null);

            exileResolutionService.resolveExileTargetPermanentUntilSourceLeaves(gd, entry);

            verify(permanentRemovalService, never()).removePermanentToExile(any(), any());
            assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
        }

        @Test
        @DisplayName("Still exiles target when source has left battlefield but without return tracking")
        void exilesWithoutTrackingWhenSourceGone() {
            Card targetCard = createCard("Spellbook");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCreatureCard("Leonin Relic-Warder");
            // Source is NOT on battlefield

            StackEntry entry = createEntry(sourceCard, player1.getId(), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2.getId());

            exileResolutionService.resolveExileTargetPermanentUntilSourceLeaves(gd, entry);

            verify(permanentRemovalService).removePermanentToExile(gd, target);
            assertThat(gd.exileReturnOnPermanentLeave).isEmpty();
        }

        @Test
        @DisplayName("Stolen permanent tracks original owner for return")
        void stolenPermanentUsesOriginalOwner() {
            Card targetCard = createCard("Spellbook");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCreatureCard("Leonin Relic-Warder");
            Permanent source = addPermanent(player1.getId(), sourceCard);

            // Mark target as stolen from player2
            gd.stolenCreatures.put(target.getId(), player2.getId());

            StackEntry entry = createEntry(sourceCard, player1.getId(), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player1.getId());

            exileResolutionService.resolveExileTargetPermanentUntilSourceLeaves(gd, entry);

            assertThat(gd.exileReturnOnPermanentLeave.get(source.getId()))
                    .isNotNull()
                    .satisfies(per -> {
                        assertThat(per.card().getName()).isEqualTo("Spellbook");
                        assertThat(per.controllerId()).isEqualTo(player2.getId());
                    });
        }

        @Test
        @DisplayName("Logs exile message with source name")
        void logsExile() {
            Card targetCard = createCard("Spellbook");
            Permanent target = new Permanent(targetCard);
            Card sourceCard = createCreatureCard("Leonin Relic-Warder");
            addPermanent(player1.getId(), sourceCard);

            StackEntry entry = createEntry(sourceCard, player1.getId(), target.getId());

            when(gameQueryService.findPermanentById(gd, target.getId())).thenReturn(target);
            when(gameQueryService.findPermanentController(gd, target.getId())).thenReturn(player2.getId());

            exileResolutionService.resolveExileTargetPermanentUntilSourceLeaves(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Spellbook is exiled by Leonin Relic-Warder."));
        }
    }

    // =========================================================================
    // ImprintDyingCreatureEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveImprintDyingCreature")
    class ResolveImprintDyingCreature {

        @Test
        @DisplayName("Imprints a dying creature onto source permanent")
        void imprintsDyingCreature() {
            Card vatCard = createCard("Mimic Vat");
            Permanent vatPerm = new Permanent(vatCard);

            Card dyingCard = createCreatureCard("Grizzly Bears");
            gd.playerGraveyards.get(player2.getId()).add(dyingCard);

            ImprintDyingCreatureEffect effect = new ImprintDyingCreatureEffect(dyingCard.getId());
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, vatCard, player1.getId(), "Mimic Vat trigger",
                    List.of(effect), vatPerm.getId(), (UUID) null
            );

            when(gameQueryService.findPermanentById(gd, vatPerm.getId())).thenReturn(vatPerm);

            exileResolutionService.resolveImprintDyingCreature(gd, entry, effect);

            // Dying card moved from graveyard to exile
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .noneMatch(c -> c.getName().equals("Grizzly Bears"));
            assertThat(gd.playerExiledCards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
            // Card imprinted on source
            assertThat(vatPerm.getCard().getImprintedCard()).isSameAs(dyingCard);
        }

        @Test
        @DisplayName("Replaces previously imprinted card when a new creature dies")
        void replacesPreviousImprint() {
            Card vatCard = createCard("Mimic Vat");
            Permanent vatPerm = new Permanent(vatCard);

            // Set up previously imprinted card
            Card previousCard = createCreatureCard("Giant Spider");
            vatPerm.getCard().setImprintedCard(previousCard);
            gd.playerExiledCards.get(player1.getId()).add(previousCard);

            // New dying creature
            Card dyingCard = createCreatureCard("Grizzly Bears");
            gd.playerGraveyards.get(player2.getId()).add(dyingCard);

            ImprintDyingCreatureEffect effect = new ImprintDyingCreatureEffect(dyingCard.getId());
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, vatCard, player1.getId(), "Mimic Vat trigger",
                    List.of(effect), vatPerm.getId(), (UUID) null
            );

            when(gameQueryService.findPermanentById(gd, vatPerm.getId())).thenReturn(vatPerm);

            exileResolutionService.resolveImprintDyingCreature(gd, entry, effect);

            // New card should be imprinted
            assertThat(vatPerm.getCard().getImprintedCard()).isSameAs(dyingCard);
            // Previous card returned to owner's graveyard
            verify(graveyardService).addCardToGraveyard(gd, player1.getId(), previousCard);
            // Previous card removed from exile
            assertThat(gd.playerExiledCards.get(player1.getId()))
                    .noneMatch(c -> c.getName().equals("Giant Spider"));
        }

        @Test
        @DisplayName("Does nothing when source permanent is gone")
        void fizzlesWhenSourceGone() {
            Card dyingCard = createCreatureCard("Grizzly Bears");
            gd.playerGraveyards.get(player2.getId()).add(dyingCard);

            UUID vatId = UUID.randomUUID();
            Card vatCard = createCard("Mimic Vat");

            ImprintDyingCreatureEffect effect = new ImprintDyingCreatureEffect(dyingCard.getId());
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, vatCard, player1.getId(), "Mimic Vat trigger",
                    List.of(effect), vatId, (UUID) null
            );

            when(gameQueryService.findPermanentById(gd, vatId)).thenReturn(null);

            exileResolutionService.resolveImprintDyingCreature(gd, entry, effect);

            // Dying card should still be in graveyard
            assertThat(gd.playerGraveyards.get(player2.getId()))
                    .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        }

        @Test
        @DisplayName("Does nothing when dying card is no longer in graveyard")
        void fizzlesWhenDyingCardGone() {
            Card vatCard = createCard("Mimic Vat");
            Permanent vatPerm = new Permanent(vatCard);

            UUID dyingCardId = UUID.randomUUID();
            ImprintDyingCreatureEffect effect = new ImprintDyingCreatureEffect(dyingCardId);
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, vatCard, player1.getId(), "Mimic Vat trigger",
                    List.of(effect), vatPerm.getId(), (UUID) null
            );

            when(gameQueryService.findPermanentById(gd, vatPerm.getId())).thenReturn(vatPerm);

            exileResolutionService.resolveImprintDyingCreature(gd, entry, effect);

            assertThat(vatPerm.getCard().getImprintedCard()).isNull();
        }

        @Test
        @DisplayName("Logs imprint message")
        void logsImprint() {
            Card vatCard = createCard("Mimic Vat");
            Permanent vatPerm = new Permanent(vatCard);

            Card dyingCard = createCreatureCard("Grizzly Bears");
            gd.playerGraveyards.get(player2.getId()).add(dyingCard);

            ImprintDyingCreatureEffect effect = new ImprintDyingCreatureEffect(dyingCard.getId());
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, vatCard, player1.getId(), "Mimic Vat trigger",
                    List.of(effect), vatPerm.getId(), (UUID) null
            );

            when(gameQueryService.findPermanentById(gd, vatPerm.getId())).thenReturn(vatPerm);

            exileResolutionService.resolveImprintDyingCreature(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq("Grizzly Bears is exiled and imprinted on Mimic Vat."));
        }
    }

    // =========================================================================
    // ExileFromHandToImprintEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveExileFromHandToImprint")
    class ResolveExileFromHandToImprint {

        private final CardPredicate filter = new CardNotPredicate(new CardTypePredicate(CardType.LAND));

        @Test
        @DisplayName("Prompts player to choose a card from hand to imprint")
        void promptsCardChoice() {
            Card anvilCard = createCard("Semblance Anvil");
            Permanent anvilPerm = new Permanent(anvilCard);

            gd.playerHands.get(player1.getId()).clear();
            Card handCard = createCreatureCard("Grizzly Bears");
            gd.playerHands.get(player1.getId()).add(handCard);

            ExileFromHandToImprintEffect effect = new ExileFromHandToImprintEffect(filter, "a nonland card");
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, anvilCard, player1.getId(), "Semblance Anvil trigger",
                    List.of(effect), anvilPerm.getId(), (UUID) null
            );

            when(gameQueryService.findPermanentById(gd, anvilPerm.getId())).thenReturn(anvilPerm);
            when(gameQueryService.matchesCardPredicate(eq(handCard), any(), any())).thenReturn(true);

            exileResolutionService.resolveExileFromHandToImprint(gd, entry, effect);

            verify(playerInputService).beginImprintFromHandChoice(
                    eq(gd), eq(player1.getId()), eq(List.of(0)),
                    eq("Choose a nonland card from your hand to exile and imprint."),
                    eq(anvilPerm.getId()));
        }

        @Test
        @DisplayName("Skips when controller has no matching cards in hand")
        void skipsWhenNoMatchingCards() {
            Card anvilCard = createCard("Semblance Anvil");
            Permanent anvilPerm = new Permanent(anvilCard);

            gd.playerHands.get(player1.getId()).clear();

            ExileFromHandToImprintEffect effect = new ExileFromHandToImprintEffect(filter, "a nonland card");
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, anvilCard, player1.getId(), "Semblance Anvil trigger",
                    List.of(effect), anvilPerm.getId(), (UUID) null
            );

            when(gameQueryService.findPermanentById(gd, anvilPerm.getId())).thenReturn(anvilPerm);

            exileResolutionService.resolveExileFromHandToImprint(gd, entry, effect);

            verify(playerInputService, never()).beginImprintFromHandChoice(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Skips when no cards match the filter")
        void skipsWhenNoCardsMatchFilter() {
            Card anvilCard = createCard("Semblance Anvil");
            Permanent anvilPerm = new Permanent(anvilCard);

            gd.playerHands.get(player1.getId()).clear();
            Card handCard = createCreatureCard("Grizzly Bears");
            gd.playerHands.get(player1.getId()).add(handCard);

            ExileFromHandToImprintEffect effect = new ExileFromHandToImprintEffect(filter, "a nonland card");
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, anvilCard, player1.getId(), "Semblance Anvil trigger",
                    List.of(effect), anvilPerm.getId(), (UUID) null
            );

            when(gameQueryService.findPermanentById(gd, anvilPerm.getId())).thenReturn(anvilPerm);
            when(gameQueryService.matchesCardPredicate(eq(handCard), any(), any())).thenReturn(false);

            exileResolutionService.resolveExileFromHandToImprint(gd, entry, effect);

            verify(playerInputService, never()).beginImprintFromHandChoice(any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Does nothing when source permanent is gone")
        void fizzlesWhenSourceGone() {
            UUID anvilId = UUID.randomUUID();
            Card anvilCard = createCard("Semblance Anvil");

            ExileFromHandToImprintEffect effect = new ExileFromHandToImprintEffect(filter, "a nonland card");
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY, anvilCard, player1.getId(), "Semblance Anvil trigger",
                    List.of(effect), anvilId, (UUID) null
            );

            when(gameQueryService.findPermanentById(gd, anvilId)).thenReturn(null);

            exileResolutionService.resolveExileFromHandToImprint(gd, entry, effect);

            verify(playerInputService, never()).beginImprintFromHandChoice(any(), any(), any(), any(), any());
        }
    }
}
