package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ExileAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.OmenMachineDrawStepEffect;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.ExileSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.OmenMachineDrawStepEffectHandler;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OmenMachineDrawStepEffectHandlerTest {

    @Mock private GraveyardService graveyardService;
    @Mock private GameQueryService gameQueryService;
    @Mock private GameBroadcastService gameBroadcastService;
    @Mock private PermanentRemovalService permanentRemovalService;
    @Mock private PlayerInputService playerInputService;
    @Mock private CardViewFactory cardViewFactory;
    @Mock private TriggerCollectionService triggerCollectionService;
    @Mock private BattlefieldEntryService battlefieldEntryService;
    @Mock private ExileService exileService;
    @InjectMocks
    private ExileSupport exileSupport;
    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;
    private OmenMachineDrawStepEffectHandler omenMachineDrawStepHandler;

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
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        omenMachineDrawStepHandler = new OmenMachineDrawStepEffectHandler(exileSupport, gameQueryService, gameBroadcastService, battlefieldEntryService, exileService, playerInputService, triggerCollectionService);

    }

    // ===== Helper methods =====

        private Card createCard(String name) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.ARTIFACT);
            return card;
        }

        private Card createCreatureCard(String name) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.CREATURE);
            card.setPower(2);
            card.setToughness(2);
            return card;
        }

        private Card createSorceryCard(String name) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.SORCERY);
            return card;
        }

        private Card createLandCard(String name) {
            Card card = new Card();
            card.setName(name);
            card.setType(CardType.LAND);
            return card;
        }

        private Permanent addPermanent(UUID playerId, Card card) {
            Permanent perm = new Permanent(card);
            perm.setSummoningSick(false);
            gd.playerBattlefields.get(playerId).add(perm);
            return perm;
        }

        private StackEntry createSingleTargetEntry(Card sourceCard, UUID controllerId, UUID targetId) {
            return new StackEntry(
                    StackEntryType.SORCERY_SPELL, sourceCard, controllerId, sourceCard.getName(),
                    List.of(new ExileTargetPermanentEffect()), 0, targetId, null
            );
        }

        private StackEntry createMultiTargetEntry(Card sourceCard, UUID controllerId, List<UUID> targetIds) {
            return new StackEntry(
                    StackEntryType.INSTANT_SPELL, sourceCard, controllerId, sourceCard.getName(),
                    List.of(new ExileTargetPermanentEffect()), 0, targetIds
            );
        }

        /** Makes the mocked exileService actually add cards to exile so assertions on GameData work. */
        private void stubExileCardTrackedWithSource() {
            doAnswer(inv -> {
                GameData gameData = inv.getArgument(0);
                UUID ownerId = inv.getArgument(1);
                Card card = inv.getArgument(2);
                UUID sourcePermanentId = inv.getArgument(3);
                gameData.addToExile(ownerId, card, sourcePermanentId);
                return null;
            }).when(exileService).exileCard(any(), any(), any(), any());
        }

        // =========================================================================
        // ExileAllPermanentsEffect
        // =========================================================================

    @Test
            @DisplayName("Logs and returns when library is empty")
            void logsWhenLibraryEmpty() {
                Card sourceCard = createCard("Omen Machine");

                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                        List.of(new OmenMachineDrawStepEffect()), 0, player1Id, null
                );

                omenMachineDrawStepHandler.resolve(gd, entry, entry.getEffectsToResolve().getFirst());

                verify(gameBroadcastService).logAndBroadcast(eq(gd),
                        eq("Player1's library is empty (Omen Machine)."));
            }

            @Test
            @DisplayName("Puts land directly onto the battlefield")
            void putsLandOntoBattlefield() {
                Card sourceCard = createCard("Omen Machine");
                Card landCard = createLandCard("Forest");
                gd.playerDecks.get(player1Id).add(landCard);

                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                        List.of(new OmenMachineDrawStepEffect()), 0, player1Id, null
                );

                omenMachineDrawStepHandler.resolve(gd, entry, entry.getEffectsToResolve().getFirst());

                verify(battlefieldEntryService).putPermanentOntoBattlefield(eq(gd), eq(player1Id), any(Permanent.class));
                verify(gameBroadcastService).logAndBroadcast(eq(gd),
                        eq("Player1 puts Forest onto the battlefield."));
                assertThat(gd.playerDecks.get(player1Id)).isEmpty();
            }

            @Test
            @DisplayName("Puts non-targeted non-land spell on the stack")
            void putsNonTargetedSpellOnStack() {
                Card sourceCard = createCard("Omen Machine");
                Card sorceryCard = createSorceryCard("Wrath of God");
                gd.playerDecks.get(player1Id).add(sorceryCard);

                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                        List.of(new OmenMachineDrawStepEffect()), 0, player1Id, null
                );

                omenMachineDrawStepHandler.resolve(gd, entry, entry.getEffectsToResolve().getFirst());

                assertThat(gd.stack).anyMatch(se -> se.getCard() == sorceryCard);
                assertThat(gd.getSpellsCastThisTurnCount(player1Id)).isEqualTo(1);
                verify(triggerCollectionService).checkSpellCastTriggers(gd, sorceryCard, player1Id, false);
            }

            @Test
            @DisplayName("Targeted spell with no valid targets stays in exile")
            void targetedSpellNoTargetsStaysInExile() {
                Card sourceCard = createCard("Omen Machine");
                Card targetedCard = createSorceryCard("Doom Blade");
                targetedCard.addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
                gd.playerDecks.get(player1Id).add(targetedCard);

                StackEntry entry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY, sourceCard, player1Id, sourceCard.getName(),
                        List.of(new OmenMachineDrawStepEffect()), 0, player1Id, null
                );

                omenMachineDrawStepHandler.resolve(gd, entry, entry.getEffectsToResolve().getFirst());

                verify(gameBroadcastService).logAndBroadcast(eq(gd),
                        eq("Doom Blade has no valid targets and remains in exile."));
            }
}
