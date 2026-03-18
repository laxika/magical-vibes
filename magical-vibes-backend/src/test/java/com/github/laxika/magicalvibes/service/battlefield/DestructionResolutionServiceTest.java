package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEquipmentAttachedToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetLandAndDamageControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndBoostSelfByManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndGainLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOrDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.effect.LifeResolutionService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DestructionResolutionServiceTest {

    @Mock private BattlefieldEntryService battlefieldEntryService;
    @Mock private GraveyardService graveyardService;
    @Mock private DamagePreventionService damagePreventionService;
    @Mock private GameOutcomeService gameOutcomeService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PlayerInputService playerInputService;
    @Mock private LifeResolutionService lifeResolutionService;

    @InjectMocks private DestructionResolutionService service;

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

    private StackEntry sorceryEntry(Card card, UUID controllerId, UUID targetPermanentId) {
        return new StackEntry(StackEntryType.SORCERY_SPELL, card, controllerId,
                card.getName(), List.of(), 0, targetPermanentId, null);
    }

    private StackEntry instantEntry(Card card, UUID controllerId, UUID targetPermanentId) {
        return new StackEntry(StackEntryType.INSTANT_SPELL, card, controllerId,
                card.getName(), List.of(), 0, targetPermanentId, null);
    }

    private StackEntry triggeredAbilityEntry(Card card, UUID controllerId, UUID targetPermanentId, UUID sourcePermanentId) {
        return new StackEntry(StackEntryType.TRIGGERED_ABILITY, card, controllerId,
                card.getName(), List.of(), targetPermanentId, sourcePermanentId);
    }

    private StackEntry activatedAbilityEntry(Card card, UUID controllerId, UUID targetPermanentId, UUID sourcePermanentId) {
        return new StackEntry(StackEntryType.ACTIVATED_ABILITY, card, controllerId,
                card.getName(), List.of(), targetPermanentId, sourcePermanentId);
    }

    // =========================================================================
    // DestroyAllPermanentsEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDestroyAllPermanents")
    class ResolveDestroyAllPermanents {

        @Test
        @DisplayName("Destroys all creatures on both sides")
        void destroysAllCreaturesOnBothSides() {
            Permanent bears = addCreature(player1Id, "Grizzly Bears");
            Permanent angel = addCreature(player2Id, "Serra Angel");

            Card wrathCard = createCard("Wrath of God");
            StackEntry entry = sorceryEntry(wrathCard, player1Id, null);
            PermanentPredicate filter = new PermanentIsCreaturePredicate();
            DestroyAllPermanentsEffect effect = new DestroyAllPermanentsEffect(filter, true);

            when(gameQueryService.matchesPermanentPredicate(eq(bears), eq(filter), any())).thenReturn(true);
            when(gameQueryService.matchesPermanentPredicate(eq(angel), eq(filter), any())).thenReturn(true);
            when(gameQueryService.hasKeyword(eq(gd), any(), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);

            service.resolveDestroyAllPermanents(gd, entry, effect);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, bears);
            verify(permanentRemovalService).removePermanentToGraveyard(gd, angel);
            verify(gameBroadcastService).logAndBroadcast(gd, "Grizzly Bears is destroyed.");
            verify(gameBroadcastService).logAndBroadcast(gd, "Serra Angel is destroyed.");
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

            when(gameQueryService.matchesPermanentPredicate(eq(spellbook), eq(filter), any())).thenReturn(false);
            when(gameQueryService.matchesPermanentPredicate(eq(bears), eq(filter), any())).thenReturn(true);
            when(gameQueryService.hasKeyword(eq(gd), any(), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);

            service.resolveDestroyAllPermanents(gd, entry, effect);

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

            when(gameQueryService.matchesPermanentPredicate(eq(golem), eq(filter), any())).thenReturn(true);
            when(gameQueryService.matchesPermanentPredicate(eq(bears), eq(filter), any())).thenReturn(true);
            when(gameQueryService.hasKeyword(gd, golem, Keyword.INDESTRUCTIBLE)).thenReturn(true);
            when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);

            service.resolveDestroyAllPermanents(gd, entry, effect);

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

            when(gameQueryService.matchesPermanentPredicate(eq(golem), eq(filter), any())).thenReturn(true);
            when(gameQueryService.hasKeyword(gd, golem, Keyword.INDESTRUCTIBLE)).thenReturn(true);

            service.resolveDestroyAllPermanents(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(gd, "Indestructible Golem is indestructible.");
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

            when(gameQueryService.matchesPermanentPredicate(eq(bears), eq(filter), any())).thenReturn(false);
            when(gameQueryService.matchesPermanentPredicate(eq(elves), eq(filter), any())).thenReturn(true);
            when(gameQueryService.hasKeyword(gd, elves, Keyword.INDESTRUCTIBLE)).thenReturn(false);
            when(graveyardService.tryRegenerate(gd, elves)).thenReturn(true);

            service.resolveDestroyAllPermanents(gd, entry, effect);

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

            when(gameQueryService.matchesPermanentPredicate(eq(bears), eq(filter), any())).thenReturn(true);
            when(gameQueryService.hasKeyword(gd, bears, Keyword.INDESTRUCTIBLE)).thenReturn(false);

            service.resolveDestroyAllPermanents(gd, entry, effect);

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
            when(gameQueryService.matchesPermanentPredicate(eq(myBears), eq(filter), any())).thenReturn(false);
            when(gameQueryService.matchesPermanentPredicate(eq(angel), eq(filter), any())).thenReturn(true);
            when(gameQueryService.matchesPermanentPredicate(eq(elves), eq(filter), any())).thenReturn(true);
            when(gameQueryService.hasKeyword(eq(gd), any(), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);

            service.resolveDestroyAllPermanents(gd, entry, effect);

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

            when(gameQueryService.matchesPermanentPredicate(eq(bears), eq(filter), any())).thenReturn(true);
            when(gameQueryService.matchesPermanentPredicate(eq(elves), eq(filter), any())).thenReturn(true);
            when(gameQueryService.hasKeyword(eq(gd), any(), eq(Keyword.INDESTRUCTIBLE))).thenReturn(false);

            service.resolveDestroyAllPermanents(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(gd, "Grizzly Bears is destroyed.");
            verify(gameBroadcastService).logAndBroadcast(gd, "Llanowar Elves is destroyed.");
        }
    }

    // =========================================================================
    // DestroyTargetPermanentEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDestroyTargetPermanent")
    class ResolveDestroyTargetPermanent {

        @Test
        @DisplayName("Destroys target creature")
        void destroysTargetCreature() {
            Permanent bears = addCreature(player2Id, "Grizzly Bears");

            Card terrorCard = createCard("Terror");
            StackEntry entry = instantEntry(terrorCard, player1Id, bears.getId());
            DestroyTargetPermanentEffect effect = new DestroyTargetPermanentEffect();

            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.findPermanentController(gd, bears.getId())).thenReturn(player2Id);
            when(permanentRemovalService.tryDestroyPermanent(gd, bears, false)).thenReturn(true);

            service.resolveDestroyTargetPermanent(gd, entry, effect);

            verify(permanentRemovalService).tryDestroyPermanent(gd, bears, false);
            verify(gameBroadcastService).logAndBroadcast(gd, "Grizzly Bears is destroyed.");
        }

        @Test
        @DisplayName("Destroys target artifact")
        void destroysTargetArtifact() {
            Card spellbookCard = createArtifactCard("Spellbook", "{0}");
            Permanent spellbook = addPermanent(player2Id, spellbookCard);

            Card shatterCard = createCard("Shatter");
            StackEntry entry = instantEntry(shatterCard, player1Id, spellbook.getId());
            DestroyTargetPermanentEffect effect = new DestroyTargetPermanentEffect();

            when(gameQueryService.findPermanentById(gd, spellbook.getId())).thenReturn(spellbook);
            when(gameQueryService.findPermanentController(gd, spellbook.getId())).thenReturn(player2Id);
            when(permanentRemovalService.tryDestroyPermanent(gd, spellbook, false)).thenReturn(true);

            service.resolveDestroyTargetPermanent(gd, entry, effect);

            verify(permanentRemovalService).tryDestroyPermanent(gd, spellbook, false);
            verify(gameBroadcastService).logAndBroadcast(gd, "Spellbook is destroyed.");
        }

        @Test
        @DisplayName("Does nothing when target is not found (fizzle)")
        void fizzlesWhenTargetRemoved() {
            UUID removedId = UUID.randomUUID();

            Card terrorCard = createCard("Terror");
            StackEntry entry = instantEntry(terrorCard, player1Id, removedId);
            DestroyTargetPermanentEffect effect = new DestroyTargetPermanentEffect();

            when(gameQueryService.findPermanentById(gd, removedId)).thenReturn(null);

            service.resolveDestroyTargetPermanent(gd, entry, effect);

            verify(permanentRemovalService, never()).tryDestroyPermanent(any(), any(), any(boolean.class));
        }

        @Test
        @DisplayName("Indestructible creature survives targeted destruction")
        void indestructibleSurvivesTargetedDestruction() {
            Permanent golem = addCreature(player2Id, "Indestructible Golem");

            Card terrorCard = createCard("Terror");
            StackEntry entry = instantEntry(terrorCard, player1Id, golem.getId());
            DestroyTargetPermanentEffect effect = new DestroyTargetPermanentEffect();

            when(gameQueryService.findPermanentById(gd, golem.getId())).thenReturn(golem);
            when(gameQueryService.findPermanentController(gd, golem.getId())).thenReturn(player2Id);
            when(permanentRemovalService.tryDestroyPermanent(gd, golem, false)).thenReturn(false);

            service.resolveDestroyTargetPermanent(gd, entry, effect);

            verify(permanentRemovalService).tryDestroyPermanent(gd, golem, false);
            verify(gameBroadcastService, never()).logAndBroadcast(eq(gd), eq("Indestructible Golem is destroyed."));
        }

        @Test
        @DisplayName("Destruction is logged")
        void destructionIsLogged() {
            Permanent bears = addCreature(player2Id, "Grizzly Bears");

            Card terrorCard = createCard("Terror");
            StackEntry entry = instantEntry(terrorCard, player1Id, bears.getId());
            DestroyTargetPermanentEffect effect = new DestroyTargetPermanentEffect();

            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.findPermanentController(gd, bears.getId())).thenReturn(player2Id);
            when(permanentRemovalService.tryDestroyPermanent(gd, bears, false)).thenReturn(true);

            service.resolveDestroyTargetPermanent(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(gd, "Grizzly Bears is destroyed.");
        }

        @Test
        @DisplayName("Creates token for target's controller when tokenForController is set")
        void createsTokenForControllerWhenSpecified() {
            Permanent bears = addCreature(player2Id, "Grizzly Bears");

            Card beastWithinCard = createCard("Beast Within");
            StackEntry entry = instantEntry(beastWithinCard, player1Id, bears.getId());
            CreateCreatureTokenEffect token = new CreateCreatureTokenEffect(
                    "Beast", 3, 3, CardColor.GREEN, List.of(CardSubtype.BEAST),
                    Set.of(), Set.of());
            DestroyTargetPermanentEffect effect = new DestroyTargetPermanentEffect(false, token);

            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.findPermanentController(gd, bears.getId())).thenReturn(player2Id);
            when(permanentRemovalService.tryDestroyPermanent(gd, bears, false)).thenReturn(true);
            when(battlefieldEntryService.snapshotEnterTappedTypes(gd)).thenReturn(Set.of());
            when(gameQueryService.getTokenMultiplier(gd, player2Id)).thenReturn(1);

            service.resolveDestroyTargetPermanent(gd, entry, effect);

            verify(permanentRemovalService).tryDestroyPermanent(gd, bears, false);
            verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player2Id), any(Permanent.class), any());
            verify(battlefieldEntryService).handleCreatureEnteredBattlefield(eq(gd), eq(player2Id), any(Card.class), eq(null), eq(false));
            verify(gameBroadcastService).logAndBroadcast(gd, "Player2 creates a 3/3 green Beast creature token.");
        }
    }

    // =========================================================================
    // DestroyEquipmentAttachedToTargetCreatureEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDestroyEquipmentAttachedToTargetCreature")
    class ResolveDestroyEquipmentAttachedToTargetCreature {

        @Test
        @DisplayName("Destroys equipment attached to target creature")
        void destroysAttachedEquipment() {
            Permanent angel = addCreature(player2Id, "Serra Angel");

            Card scimitarCard = createArtifactCard("Leonin Scimitar", "{1}");
            scimitarCard.setSubtypes(List.of(CardSubtype.EQUIPMENT));
            Permanent scimitar = addPermanent(player2Id, scimitarCard);
            scimitar.setAttachedTo(angel.getId());

            Card turnToSlagCard = createCard("Turn to Slag");
            StackEntry entry = sorceryEntry(turnToSlagCard, player1Id, angel.getId());

            when(permanentRemovalService.tryDestroyPermanent(gd, scimitar, false)).thenReturn(true);

            service.resolveDestroyEquipmentAttachedToTargetCreature(gd, entry);

            verify(permanentRemovalService).tryDestroyPermanent(gd, scimitar, false);
            verify(gameBroadcastService).logAndBroadcast(gd, "Leonin Scimitar is destroyed.");
        }

        @Test
        @DisplayName("No equipment to destroy resolves without error")
        void noEquipmentResolvesCleanly() {
            Permanent angel = addCreature(player2Id, "Serra Angel");

            Card turnToSlagCard = createCard("Turn to Slag");
            StackEntry entry = sorceryEntry(turnToSlagCard, player1Id, angel.getId());

            service.resolveDestroyEquipmentAttachedToTargetCreature(gd, entry);

            verify(permanentRemovalService, never()).tryDestroyPermanent(any(), any(), any(boolean.class));
        }
    }

    // =========================================================================
    // DestroyTargetLandAndDamageControllerEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDestroyTargetLandAndDamageController")
    class ResolveDestroyTargetLandAndDamageController {

        @Test
        @DisplayName("Destroys target land and deals 2 damage to its controller")
        void destroysLandAndDealsDamage() {
            Card mountainCard = createLandCard("Mountain");
            Permanent mountain = addPermanent(player2Id, mountainCard);

            Card meltTerrainCard = createCard("Melt Terrain");
            StackEntry entry = sorceryEntry(meltTerrainCard, player1Id, mountain.getId());
            DestroyTargetLandAndDamageControllerEffect effect = new DestroyTargetLandAndDamageControllerEffect(2);

            when(gameQueryService.findPermanentById(gd, mountain.getId())).thenReturn(mountain);
            when(gameQueryService.findPermanentController(gd, mountain.getId())).thenReturn(player2Id);
            when(permanentRemovalService.tryDestroyPermanent(gd, mountain, false)).thenReturn(true);
            when(gameQueryService.applyDamageMultiplier(gd, 2)).thenReturn(2);
            when(gameQueryService.isDamagePreventable(gd)).thenReturn(true);
            when(gameQueryService.isDamageFromSourcePrevented(eq(gd), any())).thenReturn(false);
            when(damagePreventionService.applyColorDamagePreventionForPlayer(eq(gd), eq(player2Id), any())).thenReturn(false);
            when(damagePreventionService.applyPlayerPreventionShield(gd, player2Id, 2)).thenReturn(2);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gd, player2Id, 2, "Melt Terrain")).thenReturn(2);
            when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(true);

            service.resolveDestroyTargetLandAndDamageController(gd, entry, effect);

            verify(permanentRemovalService).tryDestroyPermanent(gd, mountain, false);
            assertThat(gd.getLife(player2Id)).isEqualTo(18);
            verify(gameOutcomeService).checkWinCondition(gd);
        }

        @Test
        @DisplayName("Does nothing when target is not found (fizzle)")
        void fizzlesWhenTargetRemoved() {
            UUID removedId = UUID.randomUUID();

            Card meltTerrainCard = createCard("Melt Terrain");
            StackEntry entry = sorceryEntry(meltTerrainCard, player1Id, removedId);
            DestroyTargetLandAndDamageControllerEffect effect = new DestroyTargetLandAndDamageControllerEffect(2);

            when(gameQueryService.findPermanentById(gd, removedId)).thenReturn(null);

            service.resolveDestroyTargetLandAndDamageController(gd, entry, effect);

            assertThat(gd.getLife(player2Id)).isEqualTo(20);
            verify(permanentRemovalService, never()).tryDestroyPermanent(any(), any(), any(boolean.class));
        }

        @Test
        @DisplayName("Destruction and damage are logged")
        void destructionAndDamageAreLogged() {
            Card mountainCard = createLandCard("Mountain");
            Permanent mountain = addPermanent(player2Id, mountainCard);

            Card meltTerrainCard = createCard("Melt Terrain");
            StackEntry entry = sorceryEntry(meltTerrainCard, player1Id, mountain.getId());
            DestroyTargetLandAndDamageControllerEffect effect = new DestroyTargetLandAndDamageControllerEffect(2);

            when(gameQueryService.findPermanentById(gd, mountain.getId())).thenReturn(mountain);
            when(gameQueryService.findPermanentController(gd, mountain.getId())).thenReturn(player2Id);
            when(permanentRemovalService.tryDestroyPermanent(gd, mountain, false)).thenReturn(true);
            when(gameQueryService.applyDamageMultiplier(gd, 2)).thenReturn(2);
            when(gameQueryService.isDamagePreventable(gd)).thenReturn(true);
            when(gameQueryService.isDamageFromSourcePrevented(eq(gd), any())).thenReturn(false);
            when(damagePreventionService.applyColorDamagePreventionForPlayer(eq(gd), eq(player2Id), any())).thenReturn(false);
            when(damagePreventionService.applyPlayerPreventionShield(gd, player2Id, 2)).thenReturn(2);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gd, player2Id, 2, "Melt Terrain")).thenReturn(2);
            when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(true);

            service.resolveDestroyTargetLandAndDamageController(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(gd, "Mountain is destroyed.");
            verify(gameBroadcastService).logAndBroadcast(gd, "Melt Terrain deals 2 damage to Player2.");
        }
    }

    // =========================================================================
    // SacrificeCreatureEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveSacrificeCreature")
    class ResolveSacrificeCreature {

        @Test
        @DisplayName("Opponent with one creature sacrifices it automatically")
        void autoSacrificesOnlyCreature() {
            Permanent bears = addCreature(player2Id, "Grizzly Bears");

            Card edictCard = createCard("Cruel Edict");
            StackEntry entry = sorceryEntry(edictCard, player1Id, player2Id);

            when(gameQueryService.isCreature(gd, bears)).thenReturn(true);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

            service.resolveSacrificeCreature(gd, entry);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, bears);
            verify(gameBroadcastService).logAndBroadcast(gd, "Player2 sacrifices Grizzly Bears.");
        }

        @Test
        @DisplayName("Opponent with multiple creatures is prompted to choose")
        void promptsChoiceWithMultipleCreatures() {
            Permanent bears = addCreature(player2Id, "Grizzly Bears");
            Permanent spider = addCreature(player2Id, "Giant Spider");

            Card edictCard = createCard("Cruel Edict");
            StackEntry entry = sorceryEntry(edictCard, player1Id, player2Id);

            when(gameQueryService.isCreature(gd, bears)).thenReturn(true);
            when(gameQueryService.isCreature(gd, spider)).thenReturn(true);

            service.resolveSacrificeCreature(gd, entry);

            assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player2Id), any(), anyString());
        }

        @Test
        @DisplayName("No effect when opponent has no creatures")
        void noEffectWithNoCreatures() {
            Card edictCard = createCard("Cruel Edict");
            StackEntry entry = sorceryEntry(edictCard, player1Id, player2Id);

            service.resolveSacrificeCreature(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(gd, "Player2 has no creatures to sacrifice.");
            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
        }

        @Test
        @DisplayName("Sacrifice is logged")
        void sacrificeIsLogged() {
            Permanent bears = addCreature(player2Id, "Grizzly Bears");

            Card edictCard = createCard("Cruel Edict");
            StackEntry entry = sorceryEntry(edictCard, player1Id, player2Id);

            when(gameQueryService.isCreature(gd, bears)).thenReturn(true);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

            service.resolveSacrificeCreature(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(gd, "Player2 sacrifices Grizzly Bears.");
        }
    }

    // =========================================================================
    // SacrificeOtherCreatureOrDamageEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveSacrificeOtherCreatureOrDamage")
    class ResolveSacrificeOtherCreatureOrDamage {

        @Test
        @DisplayName("Deals 7 damage to controller when no other creatures are present")
        void dealsDamageWhenNoOtherCreatures() {
            Card lordCard = createCreatureCard("Lord of the Pit");
            Permanent lord = addPermanent(player1Id, lordCard);

            StackEntry entry = triggeredAbilityEntry(lordCard, player1Id, null, lord.getId());
            SacrificeOtherCreatureOrDamageEffect effect = new SacrificeOtherCreatureOrDamageEffect(7);

            // Lord is a creature but is the source, so no "other" creatures
            when(gameQueryService.isCreature(gd, lord)).thenReturn(true);
            when(gameQueryService.applyDamageMultiplier(gd, 7)).thenReturn(7);
            when(gameQueryService.isDamagePreventable(gd)).thenReturn(true);
            when(gameQueryService.isDamageFromSourcePrevented(eq(gd), any())).thenReturn(false);
            when(damagePreventionService.applyColorDamagePreventionForPlayer(eq(gd), eq(player1Id), any())).thenReturn(false);
            when(damagePreventionService.applyPlayerPreventionShield(gd, player1Id, 7)).thenReturn(7);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player1Id), eq(7), eq("Lord of the Pit"))).thenReturn(7);
            when(gameQueryService.canPlayerLifeChange(gd, player1Id)).thenReturn(true);

            service.resolveSacrificeOtherCreatureOrDamage(gd, entry, effect);

            assertThat(gd.getLife(player1Id)).isEqualTo(13);
            verify(gameOutcomeService).checkWinCondition(gd);
        }

        @Test
        @DisplayName("Sacrifices the only other creature automatically")
        void autoSacrificesOnlyOtherCreature() {
            Card lordCard = createCreatureCard("Lord of the Pit");
            Permanent lord = addPermanent(player1Id, lordCard);

            Card elvesCard = createCreatureCard("Llanowar Elves");
            Permanent elves = addPermanent(player1Id, elvesCard);

            StackEntry entry = triggeredAbilityEntry(lordCard, player1Id, null, lord.getId());
            SacrificeOtherCreatureOrDamageEffect effect = new SacrificeOtherCreatureOrDamageEffect(7);

            when(gameQueryService.isCreature(gd, lord)).thenReturn(true);
            when(gameQueryService.isCreature(gd, elves)).thenReturn(true);
            when(gameQueryService.findPermanentById(gd, elves.getId())).thenReturn(elves);

            service.resolveSacrificeOtherCreatureOrDamage(gd, entry, effect);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, elves);
            verify(gameBroadcastService).logAndBroadcast(gd, "Player1 sacrifices Llanowar Elves.");
            // No damage dealt
            assertThat(gd.getLife(player1Id)).isEqualTo(20);
        }

        @Test
        @DisplayName("Prompts choice when multiple other creatures exist")
        void promptsChoiceWithMultipleOtherCreatures() {
            Card lordCard = createCreatureCard("Lord of the Pit");
            Permanent lord = addPermanent(player1Id, lordCard);

            Permanent bears = addCreature(player1Id, "Grizzly Bears");
            Permanent elves = addCreature(player1Id, "Llanowar Elves");

            StackEntry entry = triggeredAbilityEntry(lordCard, player1Id, null, lord.getId());
            SacrificeOtherCreatureOrDamageEffect effect = new SacrificeOtherCreatureOrDamageEffect(7);

            when(gameQueryService.isCreature(gd, lord)).thenReturn(true);
            when(gameQueryService.isCreature(gd, bears)).thenReturn(true);
            when(gameQueryService.isCreature(gd, elves)).thenReturn(true);

            service.resolveSacrificeOtherCreatureOrDamage(gd, entry, effect);

            assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
            verify(playerInputService).beginPermanentChoice(eq(gd), eq(player1Id), any(), anyString());
        }

        @Test
        @DisplayName("Damage is logged when no creatures to sacrifice")
        void damageIsLogged() {
            Card lordCard = createCreatureCard("Lord of the Pit");
            Permanent lord = addPermanent(player1Id, lordCard);

            StackEntry entry = triggeredAbilityEntry(lordCard, player1Id, null, lord.getId());
            SacrificeOtherCreatureOrDamageEffect effect = new SacrificeOtherCreatureOrDamageEffect(7);

            when(gameQueryService.isCreature(gd, lord)).thenReturn(true);
            when(gameQueryService.applyDamageMultiplier(gd, 7)).thenReturn(7);
            when(gameQueryService.isDamagePreventable(gd)).thenReturn(true);
            when(gameQueryService.isDamageFromSourcePrevented(eq(gd), any())).thenReturn(false);
            when(damagePreventionService.applyColorDamagePreventionForPlayer(eq(gd), eq(player1Id), any())).thenReturn(false);
            when(damagePreventionService.applyPlayerPreventionShield(gd, player1Id, 7)).thenReturn(7);
            when(permanentRemovalService.redirectPlayerDamageToEnchantedCreature(eq(gd), eq(player1Id), eq(7), eq("Lord of the Pit"))).thenReturn(7);
            when(gameQueryService.canPlayerLifeChange(gd, player1Id)).thenReturn(true);

            service.resolveSacrificeOtherCreatureOrDamage(gd, entry, effect);

            verify(gameBroadcastService).logAndBroadcast(gd, "Lord of the Pit deals 7 damage to Player1.");
        }
    }

    // =========================================================================
    // DestroyTargetPermanentAndBoostSelfByManaValueEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDestroyTargetArtifactAndBoostSelfByManaValue")
    class ResolveDestroyTargetArtifactAndBoostSelfByManaValue {

        @Test
        @DisplayName("Destroys target artifact and boosts self by its mana value")
        void destroysArtifactAndBoostsSelf() {
            Card dragonCard = createCreatureCard("Hoard-Smelter Dragon");
            Permanent dragon = addPermanent(player1Id, dragonCard);

            Card scimitarCard = createArtifactCard("Leonin Scimitar", "{1}");
            Permanent scimitar = addPermanent(player2Id, scimitarCard);

            StackEntry entry = activatedAbilityEntry(dragonCard, player1Id, scimitar.getId(), dragon.getId());

            when(gameQueryService.findPermanentById(gd, scimitar.getId())).thenReturn(scimitar);
            when(gameQueryService.findPermanentById(gd, dragon.getId())).thenReturn(dragon);
            when(permanentRemovalService.tryDestroyPermanent(gd, scimitar, false)).thenReturn(true);

            service.resolveDestroyTargetArtifactAndBoostSelfByManaValue(gd, entry);

            verify(permanentRemovalService).tryDestroyPermanent(gd, scimitar, false);
            assertThat(dragon.getPowerModifier()).isEqualTo(1);
        }

        @Test
        @DisplayName("Boost is zero when artifact has mana value 0")
        void noBoostForZeroManaValue() {
            Card dragonCard = createCreatureCard("Hoard-Smelter Dragon");
            Permanent dragon = addPermanent(player1Id, dragonCard);

            Card spellbookCard = createArtifactCard("Spellbook", "{0}");
            Permanent spellbook = addPermanent(player2Id, spellbookCard);

            StackEntry entry = activatedAbilityEntry(dragonCard, player1Id, spellbook.getId(), dragon.getId());

            when(gameQueryService.findPermanentById(gd, spellbook.getId())).thenReturn(spellbook);
            when(gameQueryService.findPermanentById(gd, dragon.getId())).thenReturn(dragon);
            when(permanentRemovalService.tryDestroyPermanent(gd, spellbook, false)).thenReturn(true);

            service.resolveDestroyTargetArtifactAndBoostSelfByManaValue(gd, entry);

            verify(permanentRemovalService).tryDestroyPermanent(gd, spellbook, false);
            // Dragon is found (self != null) but mana value is 0, so no boost is applied
            assertThat(dragon.getPowerModifier()).isEqualTo(0);
            verify(gameBroadcastService, never()).logAndBroadcast(eq(gd), argThat(msg -> msg.contains("+0/+0")));
        }

        @Test
        @DisplayName("Destruction and boost are logged")
        void destructionAndBoostAreLogged() {
            Card dragonCard = createCreatureCard("Hoard-Smelter Dragon");
            Permanent dragon = addPermanent(player1Id, dragonCard);

            Card scimitarCard = createArtifactCard("Leonin Scimitar", "{1}");
            Permanent scimitar = addPermanent(player2Id, scimitarCard);

            StackEntry entry = activatedAbilityEntry(dragonCard, player1Id, scimitar.getId(), dragon.getId());

            when(gameQueryService.findPermanentById(gd, scimitar.getId())).thenReturn(scimitar);
            when(gameQueryService.findPermanentById(gd, dragon.getId())).thenReturn(dragon);
            when(permanentRemovalService.tryDestroyPermanent(gd, scimitar, false)).thenReturn(true);

            service.resolveDestroyTargetArtifactAndBoostSelfByManaValue(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(gd, "Leonin Scimitar is destroyed.");
            verify(gameBroadcastService).logAndBroadcast(gd, "Hoard-Smelter Dragon gets +1/+0 until end of turn.");
        }
    }

    // =========================================================================
    // DestroyTargetPermanentAndGainLifeEqualToManaValueEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDestroyTargetPermanentAndGainLifeEqualToManaValue")
    class ResolveDestroyTargetPermanentAndGainLifeEqualToManaValue {

        @Test
        @DisplayName("Destroys artifact and gains life equal to its mana value")
        void destroysArtifactAndGainsLife() {
            Card scimitarCard = createArtifactCard("Leonin Scimitar", "{1}");
            Permanent scimitar = addPermanent(player2Id, scimitarCard);

            Card divineOfferingCard = createCard("Divine Offering");
            StackEntry entry = instantEntry(divineOfferingCard, player1Id, scimitar.getId());

            when(gameQueryService.findPermanentById(gd, scimitar.getId())).thenReturn(scimitar);
            when(permanentRemovalService.tryDestroyPermanent(gd, scimitar, false)).thenReturn(true);

            service.resolveDestroyTargetPermanentAndGainLifeEqualToManaValue(gd, entry);

            verify(permanentRemovalService).tryDestroyPermanent(gd, scimitar, false);
            verify(lifeResolutionService).applyGainLife(gd, player1Id, 1,
                    "equal to Leonin Scimitar's mana value");
        }

        @Test
        @DisplayName("Gains no life from zero mana value artifact")
        void gainsNoLifeFromZeroManaValue() {
            Card spellbookCard = createArtifactCard("Spellbook", "{0}");
            Permanent spellbook = addPermanent(player2Id, spellbookCard);

            Card divineOfferingCard = createCard("Divine Offering");
            StackEntry entry = instantEntry(divineOfferingCard, player1Id, spellbook.getId());

            when(gameQueryService.findPermanentById(gd, spellbook.getId())).thenReturn(spellbook);
            when(permanentRemovalService.tryDestroyPermanent(gd, spellbook, false)).thenReturn(true);

            service.resolveDestroyTargetPermanentAndGainLifeEqualToManaValue(gd, entry);

            verify(lifeResolutionService, never()).applyGainLife(any(), any(), any(int.class), anyString());
        }

        @Test
        @DisplayName("Does nothing when target is not found (fizzle)")
        void fizzlesWhenTargetRemoved() {
            UUID removedId = UUID.randomUUID();

            Card divineOfferingCard = createCard("Divine Offering");
            StackEntry entry = instantEntry(divineOfferingCard, player1Id, removedId);

            when(gameQueryService.findPermanentById(gd, removedId)).thenReturn(null);

            service.resolveDestroyTargetPermanentAndGainLifeEqualToManaValue(gd, entry);

            verify(permanentRemovalService, never()).tryDestroyPermanent(any(), any(), any(boolean.class));
            verify(lifeResolutionService, never()).applyGainLife(any(), any(), any(int.class), anyString());
        }

        @Test
        @DisplayName("Life gain is logged via lifeResolutionService")
        void lifeGainIsDelegated() {
            Card scimitarCard = createArtifactCard("Leonin Scimitar", "{1}");
            Permanent scimitar = addPermanent(player2Id, scimitarCard);

            Card divineOfferingCard = createCard("Divine Offering");
            StackEntry entry = instantEntry(divineOfferingCard, player1Id, scimitar.getId());

            when(gameQueryService.findPermanentById(gd, scimitar.getId())).thenReturn(scimitar);
            when(permanentRemovalService.tryDestroyPermanent(gd, scimitar, false)).thenReturn(true);

            service.resolveDestroyTargetPermanentAndGainLifeEqualToManaValue(gd, entry);

            verify(lifeResolutionService).applyGainLife(gd, player1Id, 1,
                    "equal to Leonin Scimitar's mana value");
        }
    }

    // =========================================================================
    // DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveDestroyTargetAndControllerLosesLifePerCreatureDeaths")
    class ResolveDestroyTargetAndControllerLosesLifePerCreatureDeaths {

        @Test
        @DisplayName("Controller loses life equal to creature deaths this turn")
        void countsAllCreatureDeathsThisTurn() {
            Permanent bears = addCreature(player2Id, "Grizzly Bears");

            Card fleshAllergyCard = createCard("Flesh Allergy");
            StackEntry entry = sorceryEntry(fleshAllergyCard, player1Id, bears.getId());
            DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect effect =
                    new DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect();

            // Simulate 2 creatures already died this turn
            gd.creatureDeathCountThisTurn.put(player1Id, 1);
            gd.creatureDeathCountThisTurn.put(player2Id, 1);

            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.findPermanentController(gd, bears.getId())).thenReturn(player2Id);
            when(permanentRemovalService.tryDestroyPermanent(gd, bears, false)).thenReturn(true);
            when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(true);

            service.resolveDestroyTargetAndControllerLosesLifePerCreatureDeaths(gd, entry, effect);

            assertThat(gd.getLife(player2Id)).isEqualTo(18);
            verify(gameBroadcastService).logAndBroadcast(gd, "Player2 loses 2 life (Flesh Allergy).");
            verify(gameOutcomeService).checkWinCondition(gd);
        }

        @Test
        @DisplayName("Includes earlier deaths from the same turn")
        void includesEarlierDeaths() {
            Permanent bears = addCreature(player2Id, "Grizzly Bears");

            Card fleshAllergyCard = createCard("Flesh Allergy");
            StackEntry entry = sorceryEntry(fleshAllergyCard, player1Id, bears.getId());
            DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect effect =
                    new DestroyTargetAndControllerLosesLifePerCreatureDeathsEffect();

            // 4 creature deaths: 2 earlier + sacrifice + destroyed = 4
            gd.creatureDeathCountThisTurn.put(player1Id, 3);
            gd.creatureDeathCountThisTurn.put(player2Id, 1);

            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
            when(gameQueryService.findPermanentController(gd, bears.getId())).thenReturn(player2Id);
            when(permanentRemovalService.tryDestroyPermanent(gd, bears, false)).thenReturn(true);
            when(gameQueryService.canPlayerLifeChange(gd, player2Id)).thenReturn(true);

            service.resolveDestroyTargetAndControllerLosesLifePerCreatureDeaths(gd, entry, effect);

            assertThat(gd.getLife(player2Id)).isEqualTo(16);
            verify(gameBroadcastService).logAndBroadcast(gd, "Player2 loses 4 life (Flesh Allergy).");
        }
    }

    // =========================================================================
    // EachOpponentSacrificesCreatureEffect
    // =========================================================================

    @Nested
    @DisplayName("resolveEachOpponentSacrificesCreature")
    class ResolveEachOpponentSacrificesCreature {

        @Test
        @DisplayName("Opponent with one creature sacrifices it")
        void opponentSacrificesOnlyCreature() {
            Permanent bears = addCreature(player2Id, "Grizzly Bears");

            Card gravePactCard = createCard("Grave Pact");
            StackEntry entry = triggeredAbilityEntry(gravePactCard, player1Id, null, null);

            when(gameQueryService.isCreature(gd, bears)).thenReturn(true);
            when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

            service.resolveEachOpponentSacrificesCreature(gd, entry);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, bears);
            verify(gameBroadcastService).logAndBroadcast(gd, "Player2 sacrifices Grizzly Bears.");
        }

        @Test
        @DisplayName("No effect when opponent has no creatures")
        void noEffectWhenOpponentHasNoCreatures() {
            Card gravePactCard = createCard("Grave Pact");
            StackEntry entry = triggeredAbilityEntry(gravePactCard, player1Id, null, null);

            service.resolveEachOpponentSacrificesCreature(gd, entry);

            verify(gameBroadcastService).logAndBroadcast(gd, "Player2 has no creatures to sacrifice.");
            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
        }
    }
}
