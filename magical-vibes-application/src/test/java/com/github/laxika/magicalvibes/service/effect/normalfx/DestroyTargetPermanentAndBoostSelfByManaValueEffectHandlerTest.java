package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAndBoostSelfByManaValueEffect;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestroyTargetPermanentAndBoostSelfByManaValueEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DestroyTargetPermanentAndBoostSelfByManaValueEffectHandlerTest {

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
    private DestroyTargetPermanentAndBoostSelfByManaValueEffectHandler destroyAndBoostHandler;

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
        destroyAndBoostHandler = new DestroyTargetPermanentAndBoostSelfByManaValueEffectHandler(
                destructionSupport, gameBroadcastService, gameQueryService);

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

                destroyAndBoostHandler.resolve(gd, entry, new DestroyTargetPermanentAndBoostSelfByManaValueEffect());

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

                destroyAndBoostHandler.resolve(gd, entry, new DestroyTargetPermanentAndBoostSelfByManaValueEffect());

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

                destroyAndBoostHandler.resolve(gd, entry, new DestroyTargetPermanentAndBoostSelfByManaValueEffect());

                verify(gameBroadcastService).logAndBroadcast(gd, "Leonin Scimitar is destroyed.");
                verify(gameBroadcastService).logAndBroadcast(gd, "Hoard-Smelter Dragon gets +1/+0 until end of turn.");
            }
}
