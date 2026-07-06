package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureEffect;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SacrificeCreatureEffectHandlerTest {

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
    private SacrificeCreatureEffectHandler sacrificeCreatureHandler;

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
        sacrificeCreatureHandler = new SacrificeCreatureEffectHandler(destructionSupport);

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
            @DisplayName("Opponent with one creature sacrifices it automatically")
            void autoSacrificesOnlyCreature() {
                Permanent bears = addCreature(player2Id, "Grizzly Bears");

                Card edictCard = createCard("Cruel Edict");
                StackEntry entry = sorceryEntry(edictCard, player1Id, player2Id);

                when(gameQueryService.isCreature(gd, bears)).thenReturn(true);
                when(gameQueryService.findPermanentById(gd, bears.getId())).thenReturn(bears);

                sacrificeCreatureHandler.resolve(gd, entry, new SacrificeCreatureEffect());

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

                sacrificeCreatureHandler.resolve(gd, entry, new SacrificeCreatureEffect());

                assertThat(gd.interaction.permanentChoiceContext()).isInstanceOf(PermanentChoiceContext.SacrificeCreature.class);
                verify(playerInputService).beginPermanentChoice(eq(gd), eq(player2Id), any(), anyString());
            }

            @Test
            @DisplayName("No effect when opponent has no creatures")
            void noEffectWithNoCreatures() {
                Card edictCard = createCard("Cruel Edict");
                StackEntry entry = sorceryEntry(edictCard, player1Id, player2Id);

                sacrificeCreatureHandler.resolve(gd, entry, new SacrificeCreatureEffect());

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

                sacrificeCreatureHandler.resolve(gd, entry, new SacrificeCreatureEffect());

                verify(gameBroadcastService).logAndBroadcast(gd, "Player2 sacrifices Grizzly Bears.");
            }
}
