package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.service.PlayerInputService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class LegendRuleServiceTest extends BaseCardTest {

    @Mock
    private PlayerInputService playerInputService;

    @InjectMocks
    private LegendRuleService svc;

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

    @Nested
    @DisplayName("No legend rule violation")
    class NoViolation {

        @Test
        @DisplayName("Returns false when battlefield is empty")
        void returnsFalseWhenBattlefieldEmpty() {
            boolean result = svc.checkLegendRule(gd, player1.getId());

            assertThat(result).isFalse();
            assertThat(gd.interaction.permanentChoice()).isNull();
            verify(playerInputService, never()).beginPermanentChoice(any(), any(), anyList(), anyString());
        }

        @Test
        @DisplayName("Returns false with a single legendary permanent")
        void returnsFalseWithSingleLegendary() {
            harness.addToBattlefield(player1, createLegendaryCreature("Test Legend"));

            boolean result = svc.checkLegendRule(gd, player1.getId());

            assertThat(result).isFalse();
            assertThat(gd.interaction.permanentChoice()).isNull();
            verify(playerInputService, never()).beginPermanentChoice(any(), any(), anyList(), anyString());
        }

        @Test
        @DisplayName("Returns false with only non-legendary permanents")
        void returnsFalseWithNonLegendary() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player1, new GrizzlyBears());

            boolean result = svc.checkLegendRule(gd, player1.getId());

            assertThat(result).isFalse();
            verify(playerInputService, never()).beginPermanentChoice(any(), any(), anyList(), anyString());
        }

        @Test
        @DisplayName("Returns false with two different legendary permanents")
        void returnsFalseWithDifferentLegendaries() {
            harness.addToBattlefield(player1, createLegendaryCreature("Legend A"));
            harness.addToBattlefield(player1, createLegendaryCreature("Legend B"));

            boolean result = svc.checkLegendRule(gd, player1.getId());

            assertThat(result).isFalse();
            verify(playerInputService, never()).beginPermanentChoice(any(), any(), anyList(), anyString());
        }

        @Test
        @DisplayName("Does not trigger when the same legendary is controlled by different players")
        void noTriggerAcrossPlayers() {
            harness.addToBattlefield(player1, createLegendaryCreature("Shared Legend"));
            harness.addToBattlefield(player2, createLegendaryCreature("Shared Legend"));

            boolean result1 = svc.checkLegendRule(gd, player1.getId());
            boolean result2 = svc.checkLegendRule(gd, player2.getId());

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
            harness.addToBattlefield(player1, createLegendaryCreature("Test Legend"));
            harness.addToBattlefield(player1, createLegendaryCreature("Test Legend"));

            boolean result = svc.checkLegendRule(gd, player1.getId());

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Calls PlayerInputService.beginPermanentChoice on violation")
        void callsPlayerInputServiceOnViolation() {
            harness.addToBattlefield(player1, createLegendaryCreature("Test Legend"));
            harness.addToBattlefield(player1, createLegendaryCreature("Test Legend"));

            List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
            UUID id1 = battlefield.get(0).getId();
            UUID id2 = battlefield.get(1).getId();

            svc.checkLegendRule(gd, player1.getId());

            verify(playerInputService).beginPermanentChoice(
                    eq(gd),
                    eq(player1.getId()),
                    eq(List.of(id1, id2)),
                    eq("You control multiple legendary permanents named Test Legend. Choose one to keep.")
            );
        }

        @Test
        @DisplayName("Sets LegendRule context with the correct card name")
        void setsLegendRuleContext() {
            harness.addToBattlefield(player1, createLegendaryCreature("Test Legend"));
            harness.addToBattlefield(player1, createLegendaryCreature("Test Legend"));

            svc.checkLegendRule(gd, player1.getId());

            assertThat(gd.interaction.permanentChoiceContext())
                    .isInstanceOf(PermanentChoiceContext.LegendRule.class);
            assertThat(((PermanentChoiceContext.LegendRule) gd.interaction.permanentChoiceContext()).cardName())
                    .isEqualTo("Test Legend");
        }

        @Test
        @DisplayName("Valid choice IDs contain both duplicate permanents")
        void validChoiceIdsContainBothPermanents() {
            Card legend1 = createLegendaryCreature("Test Legend");
            Card legend2 = createLegendaryCreature("Test Legend");
            harness.addToBattlefield(player1, legend1);
            harness.addToBattlefield(player1, legend2);

            List<Permanent> battlefield = gd.playerBattlefields.get(player1.getId());
            UUID id1 = battlefield.get(0).getId();
            UUID id2 = battlefield.get(1).getId();

            svc.checkLegendRule(gd, player1.getId());

            verify(playerInputService).beginPermanentChoice(
                    eq(gd),
                    eq(player1.getId()),
                    eq(List.of(id1, id2)),
                    anyString()
            );
        }

        @Test
        @DisplayName("Only detects the first violation when multiple exist")
        void detectsFirstViolationOnly() {
            harness.addToBattlefield(player1, createLegendaryCreature("Legend A"));
            harness.addToBattlefield(player1, createLegendaryCreature("Legend A"));
            harness.addToBattlefield(player1, createLegendaryCreature("Legend B"));
            harness.addToBattlefield(player1, createLegendaryCreature("Legend B"));

            svc.checkLegendRule(gd, player1.getId());

            assertThat(gd.interaction.permanentChoiceContext())
                    .isInstanceOf(PermanentChoiceContext.LegendRule.class);
            // Only one violation is processed at a time
            String choiceName = ((PermanentChoiceContext.LegendRule) gd.interaction.permanentChoiceContext()).cardName();
            assertThat(choiceName).isIn("Legend A", "Legend B");

            // PlayerInputService should be called exactly once (only first violation)
            verify(playerInputService).beginPermanentChoice(any(), any(), anyList(), anyString());
        }
    }

    @Nested
    @DisplayName("Legend rule resolution via handlePermanentChosen")
    class Resolution {

        @Test
        @DisplayName("Choosing a permanent keeps it and sends the other to graveyard")
        void keepChosenSendOtherToGraveyard() {
            Card legend1 = createLegendaryCreature("Test Legend");
            Card legend2 = createLegendaryCreature("Test Legend");
            harness.addToBattlefield(player1, legend1);
            harness.addToBattlefield(player1, legend2);

            UUID keepId = gd.playerBattlefields.get(player1.getId()).get(0).getId();

            LegendRuleService harnessLrs = harness.getLegendRuleService();
            harnessLrs.checkLegendRule(gd, player1.getId());
            harness.handlePermanentChosen(player1, keepId);

            long legendCount = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Test Legend"))
                    .count();
            assertThat(legendCount).isEqualTo(1);

            Permanent kept = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Test Legend"))
                    .findFirst().orElseThrow();
            assertThat(kept.getId()).isEqualTo(keepId);
        }

        @Test
        @DisplayName("The non-chosen legendary goes to graveyard")
        void nonChosenGoesToGraveyard() {
            Card legend1 = createLegendaryCreature("Test Legend");
            Card legend2 = createLegendaryCreature("Test Legend");
            harness.addToBattlefield(player1, legend1);
            harness.addToBattlefield(player1, legend2);

            UUID keepId = gd.playerBattlefields.get(player1.getId()).get(0).getId();

            LegendRuleService harnessLrs = harness.getLegendRuleService();
            harnessLrs.checkLegendRule(gd, player1.getId());
            harness.handlePermanentChosen(player1, keepId);

            assertThat(gd.playerGraveyards.get(player1.getId()))
                    .anyMatch(c -> c.getName().equals("Test Legend"));
        }

        @Test
        @DisplayName("Does not affect non-legendary permanents or other player's legendaries")
        void doesNotAffectOtherPermanents() {
            harness.addToBattlefield(player1, createLegendaryCreature("Test Legend"));
            harness.addToBattlefield(player1, createLegendaryCreature("Test Legend"));
            harness.addToBattlefield(player1, new GrizzlyBears());
            harness.addToBattlefield(player2, createLegendaryCreature("Test Legend"));

            UUID keepId = gd.playerBattlefields.get(player1.getId()).get(0).getId();

            LegendRuleService harnessLrs = harness.getLegendRuleService();
            harnessLrs.checkLegendRule(gd, player1.getId());
            harness.handlePermanentChosen(player1, keepId);

            // Grizzly Bears should still be on player1's battlefield
            harness.assertOnBattlefield(player1, "Grizzly Bears");
            // Player 2's legendary should be unaffected
            harness.assertOnBattlefield(player2, "Test Legend");
        }

        @Test
        @DisplayName("Resolves with three copies — keeps one, sends two to graveyard")
        void resolvesWithThreeCopies() {
            harness.addToBattlefield(player1, createLegendaryCreature("Test Legend"));
            harness.addToBattlefield(player1, createLegendaryCreature("Test Legend"));
            harness.addToBattlefield(player1, createLegendaryCreature("Test Legend"));

            UUID keepId = gd.playerBattlefields.get(player1.getId()).get(1).getId();

            LegendRuleService harnessLrs = harness.getLegendRuleService();
            harnessLrs.checkLegendRule(gd, player1.getId());
            harness.handlePermanentChosen(player1, keepId);

            long legendCount = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Test Legend"))
                    .count();
            assertThat(legendCount).isEqualTo(1);

            long graveyardCount = gd.playerGraveyards.get(player1.getId()).stream()
                    .filter(c -> c.getName().equals("Test Legend"))
                    .count();
            assertThat(graveyardCount).isEqualTo(2);
        }
    }
}
