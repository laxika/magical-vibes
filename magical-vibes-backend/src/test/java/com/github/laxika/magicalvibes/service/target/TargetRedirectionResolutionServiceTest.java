package com.github.laxika.magicalvibes.service.target;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellToSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ChangeTargetOfTargetSpellWithSingleTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TargetRedirectionResolutionServiceTest {

    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PlayerInputService playerInputService;
    @Mock private TargetLegalityService targetLegalityService;

    @InjectMocks private TargetRedirectionResolutionService service;

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

        // Default: all check methods return valid (empty = no error)
        lenient().when(targetLegalityService.checkSpellTargeting(any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        lenient().when(targetLegalityService.checkGraveyardRetargetCandidate(any(), any(), any(), any()))
                .thenReturn(Optional.empty());
        lenient().when(targetLegalityService.checkSpellTargetOnStack(any(), any(), any(), any()))
                .thenReturn(Optional.empty());
    }

    // ===== Helper methods =====

    private Card createCard(String name) {
        Card card = new Card();
        card.setName(name);
        return card;
    }

    /** Creates a spell card that targets any permanent or player (e.g. Lightning Bolt). */
    private Card createDamageSpellCard(String name, int damage) {
        Card card = createCard(name);
        card.setType(CardType.INSTANT);
        card.addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(damage));
        return card;
    }

    private Permanent createCreature(String name) {
        Card card = createCard(name);
        card.setType(CardType.CREATURE);
        return new Permanent(card);
    }

    /** Creates a stack entry representing a spell that targets a single permanent/player. */
    private StackEntry spellEntry(Card card, UUID controllerId, UUID targetId) {
        return new StackEntry(StackEntryType.INSTANT_SPELL, card, controllerId,
                card.getName(), card.getEffects(EffectSlot.SPELL), 0, targetId, null);
    }

    /** Creates a stack entry for ChangeTargetOfTargetSpellToSourceEffect (e.g. Spellskite trigger). */
    private StackEntry redirectToSourceEntry(Card card, UUID controllerId, UUID targetSpellCardId, UUID sourcePermanentId) {
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                card.getName(), List.of(new ChangeTargetOfTargetSpellToSourceEffect()),
                targetSpellCardId, sourcePermanentId);
    }

    /** Creates a stack entry for ChangeTargetOfTargetSpellWithSingleTargetEffect (e.g. Deflection). */
    private StackEntry redirectWithSingleTargetEntry(Card card, UUID controllerId, UUID targetSpellCardId) {
        return new StackEntry(StackEntryType.INSTANT_SPELL, card, controllerId,
                card.getName(), List.of(new ChangeTargetOfTargetSpellWithSingleTargetEffect()),
                0, targetSpellCardId, null);
    }

    /** Adds a stack entry and stubs gameQueryService to find it by card ID. */
    private void addToStack(StackEntry stackEntry) {
        gd.stack.add(stackEntry);
        lenient().when(gameQueryService.findStackEntryByCardId(gd, stackEntry.getCard().getId()))
                .thenReturn(stackEntry);
    }

    private String captureLogMessage() {
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(gameBroadcastService).logAndBroadcast(eq(gd), captor.capture());
        return captor.getValue();
    }

    // =========================================================================
    // ChangeTargetOfTargetSpellWithSingleTargetEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveChangeTargetOfTargetSpellWithSingleTarget")
    class ResolveChangeTargetOfTargetSpellWithSingleTarget {

        @Test
        @DisplayName("Does nothing when target spell is no longer on the stack")
        void doesNothingWhenTargetSpellNotOnStack() {
            Card redirectCard = createCard("Deflection");
            UUID missingCardId = UUID.randomUUID();
            StackEntry entry = redirectWithSingleTargetEntry(redirectCard, player1Id, missingCardId);

            service.resolveChangeTargetOfTargetSpellWithSingleTarget(gd, entry);

            verify(playerInputService, never()).beginPermanentChoice(any(), any(), any(), any());
            verify(gameBroadcastService, never()).logAndBroadcast(any(), anyString());
        }

        @Test
        @DisplayName("Logs when target spell has multiple targets (not a single-target spell)")
        void logsWhenSpellHasMultipleTargets() {
            Card redirectCard = createCard("Deflection");
            Card targetSpellCard = createCard("Arc Trail");
            StackEntry targetSpell = new StackEntry(StackEntryType.INSTANT_SPELL, targetSpellCard,
                    player2Id, "Arc Trail", List.of(), 0, List.of(UUID.randomUUID(), UUID.randomUUID()));
            addToStack(targetSpell);

            StackEntry entry = redirectWithSingleTargetEntry(redirectCard, player1Id, targetSpellCard.getId());

            service.resolveChangeTargetOfTargetSpellWithSingleTarget(gd, entry);

            assertThat(captureLogMessage()).contains("no longer has a single target");
            verify(playerInputService, never()).beginPermanentChoice(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Logs when target spell has no target at all")
        void logsWhenSpellHasNoTarget() {
            Card redirectCard = createCard("Deflection");
            Card targetSpellCard = createCard("Wrath of God");
            StackEntry targetSpell = new StackEntry(StackEntryType.SORCERY_SPELL, targetSpellCard,
                    player2Id, "Wrath of God", List.of(), 0);
            addToStack(targetSpell);

            StackEntry entry = redirectWithSingleTargetEntry(redirectCard, player1Id, targetSpellCard.getId());

            service.resolveChangeTargetOfTargetSpellWithSingleTarget(gd, entry);

            assertThat(captureLogMessage()).contains("no longer has a single target");
            verify(playerInputService, never()).beginPermanentChoice(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Logs when no legal new targets are available")
        void logsWhenNoLegalNewTargets() {
            Card redirectCard = createCard("Deflection");
            Card targetSpellCard = createDamageSpellCard("Lightning Bolt", 3);
            Permanent target = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(target);

            StackEntry targetSpell = spellEntry(targetSpellCard, player2Id, target.getId());
            addToStack(targetSpell);

            StackEntry entry = redirectWithSingleTargetEntry(redirectCard, player1Id, targetSpellCard.getId());

            // All candidates fail validation
            when(targetLegalityService.checkSpellTargeting(any(), any(), any(), isNull(), any()))
                    .thenReturn(Optional.of("illegal target"));

            service.resolveChangeTargetOfTargetSpellWithSingleTarget(gd, entry);

            assertThat(captureLogMessage()).contains("No legal new target");
            verify(playerInputService, never()).beginPermanentChoice(any(), any(), any(), any());
        }

        @Test
        @DisplayName("Begins permanent choice with valid new targets")
        void beginsPermanentChoiceWithValidTargets() {
            Card redirectCard = createCard("Deflection");
            Card targetSpellCard = createDamageSpellCard("Lightning Bolt", 3);
            Permanent currentTarget = createCreature("Grizzly Bears");
            Permanent newTarget = createCreature("Serra Angel");
            gd.playerBattlefields.get(player2Id).add(currentTarget);
            gd.playerBattlefields.get(player1Id).add(newTarget);

            StackEntry targetSpell = spellEntry(targetSpellCard, player2Id, currentTarget.getId());
            addToStack(targetSpell);

            StackEntry entry = redirectWithSingleTargetEntry(redirectCard, player1Id, targetSpellCard.getId());

            // player IDs fail validation (not legal targets for this spell)
            lenient().when(targetLegalityService.checkSpellTargeting(eq(gd), eq(targetSpellCard), eq(player1Id), isNull(), eq(player2Id)))
                    .thenReturn(Optional.of("illegal"));
            lenient().when(targetLegalityService.checkSpellTargeting(eq(gd), eq(targetSpellCard), eq(player2Id), isNull(), eq(player2Id)))
                    .thenReturn(Optional.of("illegal"));

            service.resolveChangeTargetOfTargetSpellWithSingleTarget(gd, entry);

            assertThat(gd.interaction.permanentChoiceContext())
                    .isInstanceOf(PermanentChoiceContext.SpellRetarget.class);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<UUID>> idsCaptor = ArgumentCaptor.forClass(List.class);
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id),
                    idsCaptor.capture(), anyString());

            assertThat(idsCaptor.getValue())
                    .contains(newTarget.getId())
                    .doesNotContain(currentTarget.getId());
        }

        @Test
        @DisplayName("Excludes the current target from valid new targets")
        void excludesCurrentTarget() {
            Card redirectCard = createCard("Deflection");
            Card targetSpellCard = createDamageSpellCard("Lightning Bolt", 3);
            Permanent onlyTarget = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(onlyTarget);

            StackEntry targetSpell = spellEntry(targetSpellCard, player2Id, onlyTarget.getId());
            addToStack(targetSpell);

            StackEntry entry = redirectWithSingleTargetEntry(redirectCard, player1Id, targetSpellCard.getId());

            // Player IDs also fail validation
            when(targetLegalityService.checkSpellTargeting(eq(gd), eq(targetSpellCard), eq(player1Id), isNull(), eq(player2Id)))
                    .thenReturn(Optional.of("illegal"));
            when(targetLegalityService.checkSpellTargeting(eq(gd), eq(targetSpellCard), eq(player2Id), isNull(), eq(player2Id)))
                    .thenReturn(Optional.of("illegal"));

            service.resolveChangeTargetOfTargetSpellWithSingleTarget(gd, entry);

            assertThat(captureLogMessage()).contains("No legal new target");
        }

        @Test
        @DisplayName("Players are included as valid new targets when legal")
        void playersIncludedAsValidTargets() {
            Card redirectCard = createCard("Deflection");
            Card targetSpellCard = createDamageSpellCard("Lightning Bolt", 3);
            Permanent currentTarget = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(currentTarget);

            StackEntry targetSpell = spellEntry(targetSpellCard, player2Id, currentTarget.getId());
            addToStack(targetSpell);

            StackEntry entry = redirectWithSingleTargetEntry(redirectCard, player1Id, targetSpellCard.getId());

            // player2Id fails, player1Id passes (default: Optional.empty() = legal)
            lenient().when(targetLegalityService.checkSpellTargeting(eq(gd), eq(targetSpellCard), eq(player2Id), isNull(), eq(player2Id)))
                    .thenReturn(Optional.of("illegal"));

            service.resolveChangeTargetOfTargetSpellWithSingleTarget(gd, entry);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<UUID>> idsCaptor = ArgumentCaptor.forClass(List.class);
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id),
                    idsCaptor.capture(), anyString());

            assertThat(idsCaptor.getValue()).contains(player1Id);
        }

        @Test
        @DisplayName("Excludes invalid candidates on the stack via checkSpellTargetOnStack")
        void excludesInvalidStackCandidates() {
            Card redirectCard = createCard("Deflection");
            Card counterspellCard = createCard("Counterspell");
            counterspellCard.setType(CardType.INSTANT);
            counterspellCard.addEffect(EffectSlot.SPELL, new CounterSpellEffect());

            Card currentTargetSpell = createCard("Giant Growth");
            Card validSpell = createCard("Shock");
            Card invalidSpell = createCard("Lightning Bolt");

            StackEntry currentTargetEntry = new StackEntry(StackEntryType.INSTANT_SPELL,
                    currentTargetSpell, player2Id, "Giant Growth", List.of(), 0);
            StackEntry validEntry = new StackEntry(StackEntryType.INSTANT_SPELL,
                    validSpell, player2Id, "Shock", List.of(), 0);
            StackEntry invalidEntry = new StackEntry(StackEntryType.INSTANT_SPELL,
                    invalidSpell, player1Id, "Lightning Bolt", List.of(), 0);

            // Counterspell targets Giant Growth on the stack
            StackEntry counterspellEntry = new StackEntry(StackEntryType.INSTANT_SPELL,
                    counterspellCard, player2Id, "Counterspell", counterspellCard.getEffects(EffectSlot.SPELL),
                    currentTargetSpell.getId(), Zone.STACK);
            addToStack(currentTargetEntry);
            addToStack(validEntry);
            addToStack(invalidEntry);
            addToStack(counterspellEntry);

            StackEntry entry = redirectWithSingleTargetEntry(redirectCard, player1Id, counterspellCard.getId());

            // Lightning Bolt fails spell-target validation
            when(targetLegalityService.checkSpellTargetOnStack(eq(gd), eq(invalidSpell.getId()), any(), eq(player2Id)))
                    .thenReturn(Optional.of("not a valid spell target"));

            service.resolveChangeTargetOfTargetSpellWithSingleTarget(gd, entry);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<UUID>> idsCaptor = ArgumentCaptor.forClass(List.class);
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id),
                    idsCaptor.capture(), anyString());

            assertThat(idsCaptor.getValue())
                    .contains(validSpell.getId())
                    .doesNotContain(invalidSpell.getId())
                    .doesNotContain(currentTargetSpell.getId());
        }

        @Test
        @DisplayName("Collects targets from the stack when target zone is STACK")
        void collectsTargetsFromStackWhenZoneIsStack() {
            Card redirectCard = createCard("Deflection");
            Card counterspellCard = createCard("Counterspell");
            counterspellCard.setType(CardType.INSTANT);
            counterspellCard.addEffect(EffectSlot.SPELL, new CounterSpellEffect());

            Card targetOnStack = createCard("Giant Growth");
            Card anotherSpellOnStack = createCard("Lightning Bolt");

            StackEntry giantGrowthEntry = new StackEntry(StackEntryType.INSTANT_SPELL,
                    targetOnStack, player2Id, "Giant Growth", List.of(), 0);
            StackEntry anotherEntry = new StackEntry(StackEntryType.INSTANT_SPELL,
                    anotherSpellOnStack, player2Id, "Lightning Bolt", List.of(), 0);

            // Counterspell targets Giant Growth on the stack
            StackEntry counterspellEntry = new StackEntry(StackEntryType.INSTANT_SPELL,
                    counterspellCard, player2Id, "Counterspell", counterspellCard.getEffects(EffectSlot.SPELL),
                    targetOnStack.getId(), Zone.STACK);
            addToStack(giantGrowthEntry);
            addToStack(anotherEntry);
            addToStack(counterspellEntry);

            StackEntry entry = redirectWithSingleTargetEntry(redirectCard, player1Id, counterspellCard.getId());

            service.resolveChangeTargetOfTargetSpellWithSingleTarget(gd, entry);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<UUID>> idsCaptor = ArgumentCaptor.forClass(List.class);
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id),
                    idsCaptor.capture(), anyString());

            // Counterspell should NOT be a candidate for its own retargeting
            assertThat(idsCaptor.getValue())
                    .doesNotContain(counterspellCard.getId());
        }

        @Test
        @DisplayName("Collects targets from graveyards when target zone is GRAVEYARD")
        void collectsTargetsFromGraveyardWhenZoneIsGraveyard() {
            Card redirectCard = createCard("Deflection");
            Card graveyardSpellCard = createCard("Raise Dead");
            graveyardSpellCard.setType(CardType.SORCERY);
            ReturnCardFromGraveyardEffect graveyardEffect = ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.HAND)
                    .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                    .targetGraveyard(true)
                    .build();
            graveyardSpellCard.addEffect(EffectSlot.SPELL, graveyardEffect);

            Card currentGraveyardTarget = createCard("Grizzly Bears");
            currentGraveyardTarget.setType(CardType.CREATURE);
            Card anotherGraveyardCard = createCard("Serra Angel");
            anotherGraveyardCard.setType(CardType.CREATURE);
            gd.playerGraveyards.get(player1Id).add(currentGraveyardTarget);
            gd.playerGraveyards.get(player2Id).add(anotherGraveyardCard);

            StackEntry graveyardSpell = new StackEntry(StackEntryType.INSTANT_SPELL,
                    graveyardSpellCard, player2Id, "Raise Dead", graveyardSpellCard.getEffects(EffectSlot.SPELL),
                    currentGraveyardTarget.getId(), Zone.GRAVEYARD);
            addToStack(graveyardSpell);

            StackEntry entry = redirectWithSingleTargetEntry(redirectCard, player1Id, graveyardSpellCard.getId());

            service.resolveChangeTargetOfTargetSpellWithSingleTarget(gd, entry);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<UUID>> idsCaptor = ArgumentCaptor.forClass(List.class);
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id),
                    idsCaptor.capture(), anyString());

            assertThat(idsCaptor.getValue())
                    .contains(anotherGraveyardCard.getId())
                    .doesNotContain(currentGraveyardTarget.getId());
        }

        @Test
        @DisplayName("Filters graveyard targets by controller's graveyard scope")
        void filtersGraveyardTargetsByControllerScope() {
            Card redirectCard = createCard("Deflection");
            Card graveyardSpellCard = createCard("Disentomb");
            graveyardSpellCard.setType(CardType.SORCERY);
            ReturnCardFromGraveyardEffect graveyardEffect = ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.HAND)
                    .source(GraveyardSearchScope.CONTROLLERS_GRAVEYARD)
                    .targetGraveyard(true)
                    .build();
            graveyardSpellCard.addEffect(EffectSlot.SPELL, graveyardEffect);

            Card cardInControllerGraveyard = createCard("Grizzly Bears");
            cardInControllerGraveyard.setType(CardType.CREATURE);
            Card cardInOpponentGraveyard = createCard("Serra Angel");
            cardInOpponentGraveyard.setType(CardType.CREATURE);
            Card currentTarget = createCard("Elvish Mystic");
            currentTarget.setType(CardType.CREATURE);

            // Controller is player2 — only player2's graveyard should be valid
            gd.playerGraveyards.get(player2Id).add(cardInControllerGraveyard);
            gd.playerGraveyards.get(player2Id).add(currentTarget);
            gd.playerGraveyards.get(player1Id).add(cardInOpponentGraveyard);

            StackEntry graveyardSpell = new StackEntry(StackEntryType.INSTANT_SPELL,
                    graveyardSpellCard, player2Id, "Disentomb", graveyardSpellCard.getEffects(EffectSlot.SPELL),
                    currentTarget.getId(), Zone.GRAVEYARD);
            addToStack(graveyardSpell);

            StackEntry entry = redirectWithSingleTargetEntry(redirectCard, player1Id, graveyardSpellCard.getId());

            // Opponent's graveyard card fails validation (controller scope)
            when(targetLegalityService.checkGraveyardRetargetCandidate(
                            eq(gd), eq(graveyardSpellCard), eq(cardInOpponentGraveyard.getId()), eq(player2Id)))
                    .thenReturn(Optional.of("not in controller's graveyard"));

            service.resolveChangeTargetOfTargetSpellWithSingleTarget(gd, entry);

            @SuppressWarnings("unchecked")
            ArgumentCaptor<List<UUID>> idsCaptor = ArgumentCaptor.forClass(List.class);
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id),
                    idsCaptor.capture(), anyString());

            assertThat(idsCaptor.getValue())
                    .contains(cardInControllerGraveyard.getId())
                    .doesNotContain(cardInOpponentGraveyard.getId())
                    .doesNotContain(currentTarget.getId());
        }
    }

    // =========================================================================
    // ChangeTargetOfTargetSpellToSourceEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveChangeTargetOfTargetSpellToSource")
    class ResolveChangeTargetOfTargetSpellToSource {

        @Test
        @DisplayName("Does nothing when target spell is no longer on the stack")
        void doesNothingWhenTargetSpellNotOnStack() {
            Card redirectCard = createCard("Spellskite");
            UUID missingCardId = UUID.randomUUID();
            UUID sourcePermanentId = UUID.randomUUID();
            StackEntry entry = redirectToSourceEntry(redirectCard, player1Id, missingCardId, sourcePermanentId);

            service.resolveChangeTargetOfTargetSpellToSource(gd, entry);

            verify(gameBroadcastService, never()).logAndBroadcast(any(), anyString());
        }

        @Test
        @DisplayName("Logs when target spell has no targets at all")
        void logsWhenTargetSpellHasNoTargets() {
            Card redirectCard = createCard("Spellskite");
            Card targetSpellCard = createCard("Wrath of God");
            StackEntry targetSpell = new StackEntry(StackEntryType.SORCERY_SPELL, targetSpellCard,
                    player2Id, "Wrath of God", List.of(), 0);
            addToStack(targetSpell);

            UUID sourcePermanentId = UUID.randomUUID();
            StackEntry entry = redirectToSourceEntry(redirectCard, player1Id, targetSpellCard.getId(), sourcePermanentId);

            service.resolveChangeTargetOfTargetSpellToSource(gd, entry);

            assertThat(captureLogMessage()).contains("has no targets");
        }

        @Test
        @DisplayName("Logs when source permanent is no longer on the battlefield")
        void logsWhenSourcePermanentNotOnBattlefield() {
            Card redirectCard = createCard("Spellskite");
            Card targetSpellCard = createDamageSpellCard("Lightning Bolt", 3);
            Permanent target = createCreature("Grizzly Bears");
            gd.playerBattlefields.get(player2Id).add(target);

            StackEntry targetSpell = spellEntry(targetSpellCard, player2Id, target.getId());
            addToStack(targetSpell);

            UUID sourcePermanentId = UUID.randomUUID();
            StackEntry entry = redirectToSourceEntry(redirectCard, player1Id, targetSpellCard.getId(), sourcePermanentId);

            when(gameQueryService.findPermanentById(gd, sourcePermanentId)).thenReturn(null);

            service.resolveChangeTargetOfTargetSpellToSource(gd, entry);

            assertThat(captureLogMessage()).contains("source permanent no longer on the battlefield");
        }

        @Test
        @DisplayName("Changes target to source when source is a valid new target")
        void changesTargetToSourceWhenValid() {
            Card redirectCard = createCard("Spellskite");
            Card targetSpellCard = createDamageSpellCard("Lightning Bolt", 3);
            Permanent originalTarget = createCreature("Grizzly Bears");
            Permanent sourcePermanent = createCreature("Spellskite");
            gd.playerBattlefields.get(player2Id).add(originalTarget);
            gd.playerBattlefields.get(player1Id).add(sourcePermanent);

            StackEntry targetSpell = spellEntry(targetSpellCard, player2Id, originalTarget.getId());
            addToStack(targetSpell);

            StackEntry entry = redirectToSourceEntry(redirectCard, player1Id, targetSpellCard.getId(), sourcePermanent.getId());

            when(gameQueryService.findPermanentById(gd, sourcePermanent.getId())).thenReturn(sourcePermanent);

            service.resolveChangeTargetOfTargetSpellToSource(gd, entry);

            assertThat(targetSpell.getTargetId()).isEqualTo(sourcePermanent.getId());
            assertThat(captureLogMessage()).contains("target is changed to");
        }

        @Test
        @DisplayName("Logs when spell already targets the source permanent")
        void logsWhenSpellAlreadyTargetsSource() {
            Card redirectCard = createCard("Spellskite");
            Card targetSpellCard = createDamageSpellCard("Lightning Bolt", 3);
            Permanent sourcePermanent = createCreature("Spellskite");
            gd.playerBattlefields.get(player1Id).add(sourcePermanent);

            // Spell already targets the source permanent
            StackEntry targetSpell = spellEntry(targetSpellCard, player2Id, sourcePermanent.getId());
            addToStack(targetSpell);

            StackEntry entry = redirectToSourceEntry(redirectCard, player1Id, targetSpellCard.getId(), sourcePermanent.getId());

            when(gameQueryService.findPermanentById(gd, sourcePermanent.getId())).thenReturn(sourcePermanent);

            service.resolveChangeTargetOfTargetSpellToSource(gd, entry);

            assertThat(targetSpell.getTargetId()).isEqualTo(sourcePermanent.getId());
            assertThat(captureLogMessage()).contains("already targets");
        }

        @Test
        @DisplayName("Logs when source is not a legal target for the spell")
        void logsWhenSourceIsNotLegalTarget() {
            Card redirectCard = createCard("Spellskite");
            Card targetSpellCard = createDamageSpellCard("Lightning Bolt", 3);
            Permanent originalTarget = createCreature("Grizzly Bears");
            Permanent sourcePermanent = createCreature("Spellskite");
            gd.playerBattlefields.get(player2Id).add(originalTarget);
            gd.playerBattlefields.get(player1Id).add(sourcePermanent);

            StackEntry targetSpell = spellEntry(targetSpellCard, player2Id, originalTarget.getId());
            addToStack(targetSpell);

            StackEntry entry = redirectToSourceEntry(redirectCard, player1Id, targetSpellCard.getId(), sourcePermanent.getId());

            when(gameQueryService.findPermanentById(gd, sourcePermanent.getId())).thenReturn(sourcePermanent);
            when(targetLegalityService.checkSpellTargeting(
                            eq(gd), eq(targetSpellCard), eq(sourcePermanent.getId()), isNull(), eq(player2Id)))
                    .thenReturn(Optional.of("illegal target"));

            service.resolveChangeTargetOfTargetSpellToSource(gd, entry);

            // Target should remain unchanged
            assertThat(targetSpell.getTargetId()).isEqualTo(originalTarget.getId());
            assertThat(captureLogMessage()).contains("not a legal target");
        }

        @Test
        @DisplayName("Does not redirect multi-target spell (targetIds populated)")
        void doesNotRedirectMultiTargetSpell() {
            Card redirectCard = createCard("Spellskite");
            Card targetSpellCard = createCard("Arc Trail");
            UUID target1 = UUID.randomUUID();
            UUID target2 = UUID.randomUUID();
            StackEntry targetSpell = new StackEntry(StackEntryType.INSTANT_SPELL, targetSpellCard,
                    player2Id, "Arc Trail", List.of(), 0, List.of(target1, target2));
            addToStack(targetSpell);

            Permanent sourcePermanent = createCreature("Spellskite");
            gd.playerBattlefields.get(player1Id).add(sourcePermanent);

            StackEntry entry = redirectToSourceEntry(redirectCard, player1Id, targetSpellCard.getId(), sourcePermanent.getId());

            when(gameQueryService.findPermanentById(gd, sourcePermanent.getId())).thenReturn(sourcePermanent);

            service.resolveChangeTargetOfTargetSpellToSource(gd, entry);

            assertThat(captureLogMessage()).contains("does not have a single target");
        }

        @Test
        @DisplayName("Uses checkSpellTargetOnStack when target spell needs spell target")
        void usesSpellTargetCheckWhenTargetSpellNeedsSpellTarget() {
            Card redirectCard = createCard("Spellskite");
            Card counterspellCard = createCard("Counterspell");
            counterspellCard.setType(CardType.INSTANT);
            counterspellCard.addEffect(EffectSlot.SPELL, new CounterSpellEffect());

            Card spellOnStack = createCard("Giant Growth");
            StackEntry spellOnStackEntry = new StackEntry(StackEntryType.INSTANT_SPELL,
                    spellOnStack, player2Id, "Giant Growth", List.of(), 0);
            gd.stack.add(spellOnStackEntry);

            // Counterspell targets Giant Growth on the stack
            StackEntry counterspellEntry = new StackEntry(StackEntryType.INSTANT_SPELL,
                    counterspellCard, player2Id, "Counterspell", counterspellCard.getEffects(EffectSlot.SPELL),
                    spellOnStack.getId(), Zone.STACK);
            addToStack(counterspellEntry);

            Permanent sourcePermanent = createCreature("Spellskite");
            gd.playerBattlefields.get(player1Id).add(sourcePermanent);

            StackEntry entry = redirectToSourceEntry(redirectCard, player1Id, counterspellCard.getId(), sourcePermanent.getId());

            when(gameQueryService.findPermanentById(gd, sourcePermanent.getId())).thenReturn(sourcePermanent);
            // Source permanent is not a valid spell target
            when(targetLegalityService.checkSpellTargetOnStack(eq(gd), eq(sourcePermanent.getId()), any(), eq(player2Id)))
                    .thenReturn(Optional.of("not a spell"));

            service.resolveChangeTargetOfTargetSpellToSource(gd, entry);

            // Target should remain unchanged
            assertThat(counterspellEntry.getTargetId()).isEqualTo(spellOnStack.getId());
            assertThat(captureLogMessage()).contains("not a legal target");
        }

        @Test
        @DisplayName("Uses checkGraveyardRetargetCandidate when target spell targets graveyard")
        void usesGraveyardCheckWhenTargetSpellTargetsGraveyard() {
            Card redirectCard = createCard("Spellskite");
            Card graveyardSpellCard = createCard("Raise Dead");
            graveyardSpellCard.setType(CardType.SORCERY);
            ReturnCardFromGraveyardEffect graveyardEffect = ReturnCardFromGraveyardEffect.builder()
                    .destination(GraveyardChoiceDestination.HAND)
                    .source(GraveyardSearchScope.ALL_GRAVEYARDS)
                    .targetGraveyard(true)
                    .build();
            graveyardSpellCard.addEffect(EffectSlot.SPELL, graveyardEffect);

            Card graveyardTarget = createCard("Grizzly Bears");
            graveyardTarget.setType(CardType.CREATURE);
            gd.playerGraveyards.get(player2Id).add(graveyardTarget);

            StackEntry graveyardSpell = new StackEntry(StackEntryType.SORCERY_SPELL,
                    graveyardSpellCard, player2Id, "Raise Dead", graveyardSpellCard.getEffects(EffectSlot.SPELL),
                    graveyardTarget.getId(), Zone.GRAVEYARD);
            addToStack(graveyardSpell);

            Permanent sourcePermanent = createCreature("Spellskite");
            gd.playerBattlefields.get(player1Id).add(sourcePermanent);

            StackEntry entry = redirectToSourceEntry(redirectCard, player1Id, graveyardSpellCard.getId(), sourcePermanent.getId());

            when(gameQueryService.findPermanentById(gd, sourcePermanent.getId())).thenReturn(sourcePermanent);
            // Source permanent is not a valid graveyard target
            when(targetLegalityService.checkGraveyardRetargetCandidate(eq(gd), eq(graveyardSpellCard), eq(sourcePermanent.getId()), eq(player2Id)))
                    .thenReturn(Optional.of("not in graveyard"));

            service.resolveChangeTargetOfTargetSpellToSource(gd, entry);

            // Target should remain unchanged
            assertThat(graveyardSpell.getTargetId()).isEqualTo(graveyardTarget.getId());
            assertThat(captureLogMessage()).contains("not a legal target");
        }

        @Test
        @DisplayName("Does not redirect spell with only targetCardIds (e.g. graveyard multi-target)")
        void doesNotRedirectSpellWithOnlyTargetCardIds() {
            Card redirectCard = createCard("Spellskite");
            Card targetSpellCard = createCard("Surgical Extraction");
            StackEntry targetSpell = new StackEntry(StackEntryType.INSTANT_SPELL, targetSpellCard,
                    player2Id, "Surgical Extraction", List.of(), List.of(UUID.randomUUID()));
            addToStack(targetSpell);

            Permanent sourcePermanent = createCreature("Spellskite");
            gd.playerBattlefields.get(player1Id).add(sourcePermanent);

            StackEntry entry = redirectToSourceEntry(redirectCard, player1Id, targetSpellCard.getId(), sourcePermanent.getId());

            when(gameQueryService.findPermanentById(gd, sourcePermanent.getId())).thenReturn(sourcePermanent);

            service.resolveChangeTargetOfTargetSpellToSource(gd, entry);

            assertThat(captureLogMessage()).contains("does not have a single target");
        }
    }
}
