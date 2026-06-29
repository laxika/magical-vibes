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
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOrDamageEffect;
import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SacrificeOtherCreatureOrDamageEffectHandlerTest {

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
    private SacrificeOtherCreatureOrDamageEffectHandler sacrificeOtherOrDamageHandler;

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
        sacrificeOtherOrDamageHandler = new SacrificeOtherCreatureOrDamageEffectHandler(
                destructionSupport, gameOutcomeService, gameQueryService, playerInputService);

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

                sacrificeOtherOrDamageHandler.resolve(gd, entry, effect);

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

                sacrificeOtherOrDamageHandler.resolve(gd, entry, effect);

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

                sacrificeOtherOrDamageHandler.resolve(gd, entry, effect);

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

                sacrificeOtherOrDamageHandler.resolve(gd, entry, effect);

                verify(gameBroadcastService).logAndBroadcast(gd, "Lord of the Pit deals 7 damage to Player1.");
            }
}
