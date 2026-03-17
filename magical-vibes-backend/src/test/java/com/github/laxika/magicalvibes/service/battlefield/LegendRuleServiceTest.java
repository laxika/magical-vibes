package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.service.PlayerInputService;
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
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LegendRuleServiceTest {

    @Mock
    private PlayerInputService playerInputService;

    @InjectMocks
    private LegendRuleService svc;

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
    }

    // ===== Helper methods =====

    private Card createLegendaryCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setSupertypes(EnumSet.of(CardSupertype.LEGENDARY));
        card.setManaCost("");
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Card createCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("");
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Permanent addPermanent(UUID playerId, Card card) {
        Permanent perm = new Permanent(card);
        gd.playerBattlefields.get(playerId).add(perm);
        return perm;
    }

    @Nested
    @DisplayName("No legend rule violation")
    class NoViolation {

        @Test
        @DisplayName("Returns false when battlefield is empty")
        void returnsFalseWhenBattlefieldEmpty() {
            boolean result = svc.checkLegendRule(gd, player1Id);

            assertThat(result).isFalse();
            assertThat(gd.interaction.permanentChoice()).isNull();
            verify(playerInputService, never()).beginPermanentChoice(any(), any(), anyList(), anyString());
        }

        @Test
        @DisplayName("Returns false with a single legendary permanent")
        void returnsFalseWithSingleLegendary() {
            addPermanent(player1Id, createLegendaryCreature("Test Legend"));

            boolean result = svc.checkLegendRule(gd, player1Id);

            assertThat(result).isFalse();
            assertThat(gd.interaction.permanentChoice()).isNull();
            verify(playerInputService, never()).beginPermanentChoice(any(), any(), anyList(), anyString());
        }

        @Test
        @DisplayName("Returns false with only non-legendary permanents")
        void returnsFalseWithNonLegendary() {
            addPermanent(player1Id, createCreature("Grizzly Bears"));
            addPermanent(player1Id, createCreature("Grizzly Bears"));

            boolean result = svc.checkLegendRule(gd, player1Id);

            assertThat(result).isFalse();
            verify(playerInputService, never()).beginPermanentChoice(any(), any(), anyList(), anyString());
        }

        @Test
        @DisplayName("Returns false with two different legendary permanents")
        void returnsFalseWithDifferentLegendaries() {
            addPermanent(player1Id, createLegendaryCreature("Legend A"));
            addPermanent(player1Id, createLegendaryCreature("Legend B"));

            boolean result = svc.checkLegendRule(gd, player1Id);

            assertThat(result).isFalse();
            verify(playerInputService, never()).beginPermanentChoice(any(), any(), anyList(), anyString());
        }

        @Test
        @DisplayName("Does not trigger when the same legendary is controlled by different players")
        void noTriggerAcrossPlayers() {
            addPermanent(player1Id, createLegendaryCreature("Shared Legend"));
            addPermanent(player2Id, createLegendaryCreature("Shared Legend"));

            boolean result1 = svc.checkLegendRule(gd, player1Id);
            boolean result2 = svc.checkLegendRule(gd, player2Id);

            assertThat(result1).isFalse();
            assertThat(result2).isFalse();
            verify(playerInputService, never()).beginPermanentChoice(any(), any(), anyList(), anyString());
        }
    }

    @Nested
    @DisplayName("Legend rule violation detected")
    class ViolationDetected {

        @Test
        @DisplayName("Returns true and prompts choice when two legendary permanents share a name")
        void returnsTrueWithDuplicateLegendary() {
            addPermanent(player1Id, createLegendaryCreature("Test Legend"));
            addPermanent(player1Id, createLegendaryCreature("Test Legend"));

            boolean result = svc.checkLegendRule(gd, player1Id);

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Calls PlayerInputService.beginPermanentChoice on violation")
        void callsPlayerInputServiceOnViolation() {
            Permanent perm1 = addPermanent(player1Id, createLegendaryCreature("Test Legend"));
            Permanent perm2 = addPermanent(player1Id, createLegendaryCreature("Test Legend"));

            svc.checkLegendRule(gd, player1Id);

            verify(playerInputService).beginPermanentChoice(
                    eq(gd),
                    eq(player1Id),
                    eq(List.of(perm1.getId(), perm2.getId())),
                    eq("You control multiple legendary permanents named Test Legend. Choose one to keep.")
            );
        }

        @Test
        @DisplayName("Sets LegendRule context with the correct card name")
        void setsLegendRuleContext() {
            addPermanent(player1Id, createLegendaryCreature("Test Legend"));
            addPermanent(player1Id, createLegendaryCreature("Test Legend"));

            svc.checkLegendRule(gd, player1Id);

            assertThat(gd.interaction.permanentChoiceContext())
                    .isInstanceOf(PermanentChoiceContext.LegendRule.class);
            assertThat(((PermanentChoiceContext.LegendRule) gd.interaction.permanentChoiceContext()).cardName())
                    .isEqualTo("Test Legend");
        }

        @Test
        @DisplayName("Valid choice IDs contain both duplicate permanents")
        void validChoiceIdsContainBothPermanents() {
            Permanent perm1 = addPermanent(player1Id, createLegendaryCreature("Test Legend"));
            Permanent perm2 = addPermanent(player1Id, createLegendaryCreature("Test Legend"));

            svc.checkLegendRule(gd, player1Id);

            verify(playerInputService).beginPermanentChoice(
                    eq(gd),
                    eq(player1Id),
                    eq(List.of(perm1.getId(), perm2.getId())),
                    anyString()
            );
        }

        @Test
        @DisplayName("Only detects the first violation when multiple exist")
        void detectsFirstViolationOnly() {
            addPermanent(player1Id, createLegendaryCreature("Legend A"));
            addPermanent(player1Id, createLegendaryCreature("Legend A"));
            addPermanent(player1Id, createLegendaryCreature("Legend B"));
            addPermanent(player1Id, createLegendaryCreature("Legend B"));

            svc.checkLegendRule(gd, player1Id);

            assertThat(gd.interaction.permanentChoiceContext())
                    .isInstanceOf(PermanentChoiceContext.LegendRule.class);
            // Only one violation is processed at a time
            String choiceName = ((PermanentChoiceContext.LegendRule) gd.interaction.permanentChoiceContext()).cardName();
            assertThat(choiceName).isIn("Legend A", "Legend B");

            // PlayerInputService should be called exactly once (only first violation)
            verify(playerInputService).beginPermanentChoice(any(), any(), anyList(), anyString());
        }
    }
}
