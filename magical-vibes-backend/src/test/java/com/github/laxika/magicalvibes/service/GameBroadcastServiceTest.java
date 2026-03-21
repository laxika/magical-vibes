package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.networking.service.PermanentViewFactory;
import com.github.laxika.magicalvibes.networking.service.StackEntryViewFactory;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GameBroadcastServiceTest {

    @Mock private SessionManager sessionManager;
    @Mock private CardViewFactory cardViewFactory;
    @Mock private PermanentViewFactory permanentViewFactory;
    @Mock private StackEntryViewFactory stackEntryViewFactory;
    @Mock private GameQueryService gameQueryService;
    @Mock private ValidTargetService validTargetService;

    private GameBroadcastService svc;
    private GameData gd;
    private UUID player1Id;

    private static final GameQueryService.StaticBonus NO_BONUS = new GameQueryService.StaticBonus(
            0, 0, Set.of(), Set.of(), false, List.of(), List.of(), Set.of(), List.of(), Set.of(), Set.of(),
            false, false, false, Set.of(), false, 0, 0, false);

    @BeforeEach
    void setUp() {
        svc = new GameBroadcastService(sessionManager, cardViewFactory, permanentViewFactory,
                stackEntryViewFactory, gameQueryService, validTargetService);

        player1Id = UUID.randomUUID();
        UUID player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerHands.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerManaPools.put(player1Id, new ManaPool());
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerDecks.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerLifeTotals.put(player1Id, 20);
        gd.playerLifeTotals.put(player2Id, 20);
        gd.status = GameStatus.RUNNING;
        gd.activePlayerId = player1Id;
        gd.currentStep = TurnStep.PRECOMBAT_MAIN;
    }

    @Nested
    @DisplayName("isSpellCastingAllowed — legendary sorcery restriction")
    class LegendarySorceryRestriction {

        @Test
        @DisplayName("Rejects legendary sorcery when player controls no legendary creature or planeswalker")
        void rejectsLegendarySorceryWithoutLegendaryPermanent() {
            Card legendarySorcery = new Card();
            legendarySorcery.setName("Urza's Ruinous Blast");
            legendarySorcery.setType(CardType.SORCERY);
            legendarySorcery.setSupertypes(Set.of(CardSupertype.LEGENDARY));
            legendarySorcery.setManaCost("{4}{W}");

            // Only a non-legendary creature on battlefield
            Card bears = new Card();
            bears.setName("Grizzly Bears");
            bears.setType(CardType.CREATURE);
            Permanent bearsPerm = new Permanent(bears);
            gd.playerBattlefields.get(player1Id).add(bearsPerm);

            when(gameQueryService.computeStaticBonus(any(), any())).thenReturn(NO_BONUS);

            assertThat(svc.isSpellCastingAllowed(gd, player1Id, legendarySorcery)).isFalse();
        }

        @Test
        @DisplayName("Allows legendary sorcery when player controls a legendary creature")
        void allowsLegendarySorceryWithLegendaryCreature() {
            Card legendarySorcery = new Card();
            legendarySorcery.setName("Urza's Ruinous Blast");
            legendarySorcery.setType(CardType.SORCERY);
            legendarySorcery.setSupertypes(Set.of(CardSupertype.LEGENDARY));
            legendarySorcery.setManaCost("{4}{W}");

            Card legendaryCreature = new Card();
            legendaryCreature.setName("Arvad the Cursed");
            legendaryCreature.setType(CardType.CREATURE);
            legendaryCreature.setSupertypes(Set.of(CardSupertype.LEGENDARY));
            Permanent legendaryPerm = new Permanent(legendaryCreature);
            gd.playerBattlefields.get(player1Id).add(legendaryPerm);

            when(gameQueryService.computeStaticBonus(any(), any())).thenReturn(NO_BONUS);
            when(gameQueryService.isCreature(any(), any())).thenReturn(true);

            assertThat(svc.isSpellCastingAllowed(gd, player1Id, legendarySorcery)).isTrue();
        }

        @Test
        @DisplayName("Allows non-legendary sorcery regardless of battlefield state")
        void allowsNonLegendarySorcery() {
            Card normalSorcery = new Card();
            normalSorcery.setName("Divination");
            normalSorcery.setType(CardType.SORCERY);
            normalSorcery.setManaCost("{2}{U}");

            // Empty battlefield — no legendary permanents
            assertThat(svc.isSpellCastingAllowed(gd, player1Id, normalSorcery)).isTrue();
        }

        @Test
        @DisplayName("Allows legendary non-sorcery (e.g. legendary creature) regardless of battlefield state")
        void allowsLegendaryNonSorcery() {
            Card legendaryCreature = new Card();
            legendaryCreature.setName("Arvad the Cursed");
            legendaryCreature.setType(CardType.CREATURE);
            legendaryCreature.setSupertypes(Set.of(CardSupertype.LEGENDARY));
            legendaryCreature.setManaCost("{3}{W}{B}");

            // Empty battlefield
            assertThat(svc.isSpellCastingAllowed(gd, player1Id, legendaryCreature)).isTrue();
        }

        @Test
        @DisplayName("Rejects legendary sorcery with empty battlefield")
        void rejectsLegendarySorceryWithEmptyBattlefield() {
            Card legendarySorcery = new Card();
            legendarySorcery.setName("Kamahl's Druidic Vow");
            legendarySorcery.setType(CardType.SORCERY);
            legendarySorcery.setSupertypes(Set.of(CardSupertype.LEGENDARY));
            legendarySorcery.setManaCost("{X}{G}{G}");

            assertThat(svc.isSpellCastingAllowed(gd, player1Id, legendarySorcery)).isFalse();
        }
    }
}
