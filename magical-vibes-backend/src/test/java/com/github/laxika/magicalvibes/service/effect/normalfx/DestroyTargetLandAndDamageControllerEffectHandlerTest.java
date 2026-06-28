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
class DestroyTargetLandAndDamageControllerEffectHandlerTest {

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
    private DestroyTargetLandAndDamageControllerEffectHandler destroyLandAndDamageHandler;

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
        destroyLandAndDamageHandler = new DestroyTargetLandAndDamageControllerEffectHandler(
                destructionSupport, gameBroadcastService, gameOutcomeService, gameQueryService);

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

                destroyLandAndDamageHandler.resolve(gd, entry, effect);

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

                destroyLandAndDamageHandler.resolve(gd, entry, effect);

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

                destroyLandAndDamageHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(gd, "Mountain is destroyed.");
                verify(gameBroadcastService).logAndBroadcast(gd, "Melt Terrain deals 2 damage to Player2.");
            }
}
