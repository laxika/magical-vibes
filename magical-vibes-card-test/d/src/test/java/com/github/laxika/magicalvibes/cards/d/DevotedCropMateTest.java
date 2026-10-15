package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GustWalker;
import com.github.laxika.magicalvibes.cards.u.UnwaveringInitiate;
import com.github.laxika.magicalvibes.cards.d.DjerusResolve;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SupplyCaravan;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DevotedCropMate.class, GustWalker.class, UnwaveringInitiate.class,
        DjerusResolve.class, SupplyCaravan.class, Plains.class})
class DevotedCropMateTest extends BaseCardTest {

    @CardUsed({DevotedCropMate.class, GustWalker.class, UnwaveringInitiate.class,
            DjerusResolve.class, SupplyCaravan.class, Plains.class})
    @Nested
    @DisplayName("Attack trigger")
    class AttackTrigger {

        @Test
        @DisplayName("Attacking with Devoted Crop-Mate triggers may ability prompt")
        void attackTriggersMayPrompt() {
            harness.setGraveyard(player1, List.of(new GustWalker()));
            addReadyCropMate(player1);

            declareAttackers(List.of(0));
            harness.passBothPriorities();

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                    .isEqualTo(player1.getId());
        }

        @Test
        @DisplayName("Accepting may and picking a creature returns it to battlefield")
        void returnsCreatureWithLowManaValue() {
            harness.setGraveyard(player1, List.of(new GustWalker()));
            addReadyCropMate(player1);

            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
            harness.handleMultipleCardsChosen(player1, List.of(gd.playerGraveyards.get(player1.getId()).get(0).getId()));
            resolveAllTriggers();

            harness.assertOnBattlefield(player1, "Gust Walker");
            harness.assertNotInGraveyard(player1, "Gust Walker");
        }

        @Test
        @DisplayName("Cannot return creature with MV 3 from graveyard (boundary — filter is MV ≤ 2)")
        void cannotReturnManaValueThreeCreature() {
            harness.setGraveyard(player1, List.of(new UnwaveringInitiate())); // MV 3
            addReadyCropMate(player1);

            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        }

        @Test
        @DisplayName("Cannot return creature with MV > 2 from graveyard")
        void cannotReturnHighManaValueCreature() {
            harness.setGraveyard(player1, List.of(new SupplyCaravan())); // MV 5
            addReadyCropMate(player1);

            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        }

        @Test
        @DisplayName("Cannot return non-creature card (instant) from graveyard")
        void cannotReturnNonCreature() {
            harness.setGraveyard(player1, List.of(new DjerusResolve()));
            addReadyCropMate(player1);

            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        }

        @Test
        @DisplayName("Declining may ability does not return anything")
        void decliningMaySkipsReturn() {
            harness.setGraveyard(player1, List.of(new GustWalker()));
            addReadyCropMate(player1);

            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, false);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
            harness.assertInGraveyard(player1, "Gust Walker");
        }

        @Test
        @DisplayName("Attack resolves with no effect if graveyard is empty")
        void noEffectWithEmptyGraveyard() {
            addReadyCropMate(player1);

            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        }

        @Test
        @DisplayName("Only creature cards with MV ≤ 2 are valid — filters out MV 3, MV 5, non-creatures, and lands")
        void filtersCorrectly() {
            Card walker = new GustWalker();   // creature, MV 2 — valid
            Card resolve = new DjerusResolve();      // instant — invalid (non-creature)
            Card initiate = new UnwaveringInitiate();  // creature, MV 3 — invalid (MV > 2)
            Card caravan = new SupplyCaravan(); // creature, MV 5 — invalid (MV > 2)
            Card plains = new Plains();        // land, MV 0 — invalid (non-creature)
            harness.setGraveyard(player1, List.of(walker, resolve, initiate, caravan, plains));
            addReadyCropMate(player1);

            declareAttackers(List.of(0));
            harness.handleMayAbilityChosen(player1, true);

            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

            assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).cards())
                    .containsExactly(walker);
            harness.handleMultipleCardsChosen(player1, List.of(walker.getId()));
            resolveAllTriggers();

            harness.assertOnBattlefield(player1, "Gust Walker");
        }
    }


    @Test
    @DisplayName("Exerting prevents untapping during only the controller's next untap step")
    void exertSkipsOnlyNextControllerUntap() {
        harness.setGraveyard(player1, List.of(new GustWalker()));
        Permanent cropMate = addReadyCropMate(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1, List.of(gd.playerGraveyards.get(player1.getId()).get(0).getId()));
        resolveAllTriggers();

        harness.performUntapStep(player2);
        assertThat(cropMate.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(cropMate.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(cropMate.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining exert allows the creature to untap normally")
    void decliningExertAllowsNextUntap() {
        harness.setGraveyard(player1, List.of(new GustWalker()));
        Permanent cropMate = addReadyCropMate(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, false);

        harness.performUntapStep(player1);
        assertThat(cropMate.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Gust Walker");
    }

    @Test
    @DisplayName("Exerting skips the next untap even with no legal graveyard target")
    void exertWithoutTargetStillSkipsUntap() {
        Permanent cropMate = addReadyCropMate(player1);

        declareAttackers(List.of(0));
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(cropMate.getSkipUntapCount()).isPositive();
        harness.performUntapStep(player1);
        assertThat(cropMate.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(cropMate.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The exert decision is offered during attacker declaration before priority")
    void exertChoiceIsMadeAsAttackersAreDeclared() {
        harness.setGraveyard(player1, List.of(new GustWalker()));
        addReadyCropMate(player1);

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private Permanent addReadyCropMate(Player player) {
        return addCreatureReady(player, new DevotedCropMate());
    }
}
