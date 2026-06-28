package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

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
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
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
import com.github.laxika.magicalvibes.service.effect.normalfx.DestroyAllPermanentsEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestroyEquipmentAttachedToTargetCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestroyTargetAndControllerLosesLifePerCreatureDeathsEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestroyTargetLandAndDamageControllerEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestroyTargetPermanentAndBoostSelfByManaValueEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestroyTargetPermanentAndGainLifeEqualToManaValueEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestroyTargetPermanentEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.EachOpponentSacrificesCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.SacrificeCreatureEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.SacrificeOtherCreatureOrDamageEffectHandler;
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
class DestroyTargetPermanentEffectHandlerTest {

    @Mock private BattlefieldEntryService battlefieldEntryService;
    @Mock private GraveyardService graveyardService;
    @Mock private DamagePreventionService damagePreventionService;
    @Mock private GameOutcomeService gameOutcomeService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PlayerInputService playerInputService;
    @Mock private LifeSupport lifeSupport;
    @InjectMocks private DestructionSupport destructionSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private DestroyTargetPermanentEffectHandler destroyTargetPermanentHandler;

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
        destroyTargetPermanentHandler = new DestroyTargetPermanentEffectHandler(destructionSupport, gameQueryService);

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
            @DisplayName("Destroys target creature")
            void destroysTargetCreature() {
                Permanent bears = addCreature(player2Id, "Grizzly Bears");

                Card terrorCard = createCard("Terror");
                StackEntry entry = instantEntry(terrorCard, player1Id, bears.getId());
                DestroyTargetPermanentEffect effect = new DestroyTargetPermanentEffect();

                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
                when(gameQueryService.findPermanentController(gd, bears.getId())).thenReturn(player2Id);
                when(permanentRemovalService.tryDestroyPermanent(gd, bears, false)).thenReturn(true);

                destroyTargetPermanentHandler.resolve(gd, entry, effect);

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

                destroyTargetPermanentHandler.resolve(gd, entry, effect);

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

                destroyTargetPermanentHandler.resolve(gd, entry, effect);

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

                destroyTargetPermanentHandler.resolve(gd, entry, effect);

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

                destroyTargetPermanentHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(gd, "Grizzly Bears is destroyed.");
            }

            @Test
            @DisplayName("Creates token for target's controller when tokenForController is set")
            void createsTokenForControllerWhenSpecified() {
                Permanent bears = addCreature(player2Id, "Grizzly Bears");

                Card beastWithinCard = createCard("Beast Within");
                StackEntry entry = instantEntry(beastWithinCard, player1Id, bears.getId());
                CreateTokenEffect token = new CreateTokenEffect(
                        "Beast", 3, 3, CardColor.GREEN, List.of(CardSubtype.BEAST),
                        Set.of(), Set.of());
                DestroyTargetPermanentEffect effect = new DestroyTargetPermanentEffect(false, token);

                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);
                when(gameQueryService.findPermanentController(gd, bears.getId())).thenReturn(player2Id);
                when(permanentRemovalService.tryDestroyPermanent(gd, bears, false)).thenReturn(true);
                when(battlefieldEntryService.snapshotEnterTappedTypes(gd)).thenReturn(Set.of());
                when(gameQueryService.getTokenMultiplier(gd, player2Id)).thenReturn(1);

                destroyTargetPermanentHandler.resolve(gd, entry, effect);

                verify(permanentRemovalService).tryDestroyPermanent(gd, bears, false);
                verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player2Id), any(Permanent.class), any());
                verify(battlefieldEntryService).handleCreatureEnteredBattlefield(eq(gd), eq(player2Id), any(Card.class), eq(null), eq(false));
                verify(gameBroadcastService).logAndBroadcast(gd, "Player2 creates a 3/3 green Beast creature token.");
            }
}
