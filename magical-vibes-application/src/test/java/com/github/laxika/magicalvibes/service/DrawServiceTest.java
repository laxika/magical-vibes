package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DrawServiceTest {

    @Mock
    private GameQueryService gameQueryService;

    @Mock
    private GameBroadcastService gameBroadcastService;

    @Mock
    private GameOutcomeService gameOutcomeService;

    @Mock
    private TriggeredAbilityQueueService triggeredAbilityQueueService;

    @Mock
    private InteractionHandlerRegistry interactionHandlerRegistry;

    @InjectMocks
    private DrawService sut;

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
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
    }

    // ===== Helpers =====

    // Effects must be added before wrapping in a Permanent — the Permanent constructor
    // freezes the card.
    private static Card createCard(String name, CardType type) {
        Card card = new Card();
        card.setName(name);
        card.setType(type);
        return card;
    }

    private Permanent addEquipmentWithDrawTrigger(UUID controllerId) {
        Card card = createCard("Diviner's Wand", CardType.ARTIFACT);
        card.addEffect(EffectSlot.ON_CONTROLLER_DRAWS,
                new BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(1, 1, Keyword.FLYING));
        Permanent equipment = new Permanent(card);
        gd.playerBattlefields.get(controllerId).add(equipment);
        return equipment;
    }

    // ===== checkControllerDrawTriggers — equipment-granted draw trigger =====

    @Nested
    @DisplayName("checkControllerDrawTriggers — equipment-granted draw trigger")
    class EquipmentGrantedDrawTrigger {

        @Test
        @DisplayName("does not trigger while the equipment is unattached")
        void unattachedEquipmentDoesNotTrigger() {
            addEquipmentWithDrawTrigger(player1Id);

            sut.checkControllerDrawTriggers(gd, player1Id);

            assertThat(gd.stack).isEmpty();
            verify(gameBroadcastService, never()).logAndBroadcast(eq(gd), any());
        }

        @Test
        @DisplayName("pushes the trigger onto the stack while the equipment is attached")
        void attachedEquipmentTriggers() {
            Permanent creature = new Permanent(createCard("Grizzly Bears", CardType.CREATURE));
            gd.playerBattlefields.get(player1Id).add(creature);
            Permanent equipment = addEquipmentWithDrawTrigger(player1Id);
            equipment.setAttachedTo(creature.getId());

            sut.checkControllerDrawTriggers(gd, player1Id);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(equipment.getId());
            assertThat(gd.stack.getFirst().getEffectsToResolve())
                    .containsExactly(new BoostEquippedCreatureAndGrantKeywordUntilEndOfTurnEffect(1, 1, Keyword.FLYING));
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq(GameLog.text("Diviner's Wand's ability triggers.")));
        }

        @Test
        @DisplayName("non-equipment draw trigger still fires while another permanent is unattached")
        void nonEquipmentDrawTriggerUnaffected() {
            addEquipmentWithDrawTrigger(player1Id); // unattached — must stay silent
            Card crawlerCard = createCard("Psychosis Crawler", CardType.CREATURE);
            crawlerCard.addEffect(EffectSlot.ON_CONTROLLER_DRAWS, new BoostSelfEffect(1, 1));
            Permanent crawler = new Permanent(crawlerCard);
            gd.playerBattlefields.get(player1Id).add(crawler);

            sut.checkControllerDrawTriggers(gd, player1Id);

            assertThat(gd.stack).hasSize(1);
            assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(crawler.getId());
            verify(gameBroadcastService).logAndBroadcast(eq(gd),
                    eq(GameLog.text("Psychosis Crawler's ability triggers.")));
        }
    }
}
