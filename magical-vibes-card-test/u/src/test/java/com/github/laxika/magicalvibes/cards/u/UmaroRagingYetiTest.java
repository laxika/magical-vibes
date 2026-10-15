package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UmaroRagingYeti.class, GrizzlyBears.class})
class UmaroRagingYetiTest extends BaseCardTest {

    @Test
    void randomlyAppliesEachMode() {
        Player controller = player1;
        Player opponent = player2;
        Permanent umaro = harness.addToBattlefieldAndReturn(controller, new UmaroRagingYeti());
        Permanent bear = harness.addToBattlefieldAndReturn(controller, new GrizzlyBears());
        harness.setHand(controller, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(controller, IntStream.range(0, 400)
                .<Card>mapToObj(i -> new GrizzlyBears()).toList());
        harness.setLife(opponent, 1000);

        Set<String> modes = new HashSet<>();
        for (int i = 0; i < 90 && modes.size() < 3; i++) {
            int bearPower = bear.getEffectivePower();
            int graveyardSize = gd.playerGraveyards.get(controller.getId()).size();
            int opponentLife = gd.playerLifeTotals.get(opponent.getId());

            harness.forceActivePlayer(controller);
            harness.forceStep(TurnStep.PRECOMBAT_MAIN);
            harness.clearPriorityPassed();
            harness.passBothPriorities();

            if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
                harness.handlePermanentChosen(controller, opponent.getId());
            }
            resolveAllTriggers();

            if (bear.getEffectivePower() > bearPower) {
                modes.add("pump");
                assertThat(gqs.hasKeyword(gd, bear, Keyword.TRAMPLE)).isTrue();
            }
            if (gd.playerGraveyards.get(controller.getId()).size() > graveyardSize) {
                modes.add("discard-and-draw");
            }
            if (gd.playerLifeTotals.get(opponent.getId()) < opponentLife) {
                modes.add("damage");
            }
        }

        assertThat(modes).containsExactlyInAnyOrder("pump", "discard-and-draw", "damage");
        assertThat(umaro.getEffectivePower()).isEqualTo(6);
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        harness.addToBattlefield(player1, new UmaroRagingYeti());
        harness.setHand(player1, List.of(new UmaroRagingYeti()));
        harness.setLibrary(player1, List.of(new UmaroRagingYeti()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void drawsFourEvenWhenHandIsEmpty() {
        harness.addToBattlefield(player1, new UmaroRagingYeti());
        harness.setLibrary(player1, IntStream.range(0, 400)
                .mapToObj(i -> new UmaroRagingYeti()).toList());
        harness.setLife(player2, 1000);

        for (int i = 0; i < 90; i++) {
            harness.setHand(player1, List.of());
            enterOwnCombatAndResolve();
            if (!gd.playerHands.get(player1.getId()).isEmpty()) {
                assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
                assertThat(gd.playerDecks.get(player1.getId())).hasSize(396);
                assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
                return;
            }
        }
        throw new AssertionError("Discard-and-draw mode was never selected");
    }

    @Test
    void pumpAffectsOnlyOtherCreaturesControlledAtResolution() {
        Permanent umaro = harness.addToBattlefieldAndReturn(player1, new UmaroRagingYeti());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, IntStream.range(0, 400)
                .mapToObj(i -> new UmaroRagingYeti()).toList());
        harness.setLife(player2, 1000);

        for (int i = 0; i < 90; i++) {
            enterOwnCombatAndResolve();
            if (ally.getEffectivePower() != 2) {
                assertThat(ally.getEffectivePower()).isEqualTo(5);
                assertThat(ally.getEffectiveToughness()).isEqualTo(2);
                assertThat(gqs.hasKeyword(gd, ally, Keyword.TRAMPLE)).isTrue();
                assertThat(umaro.getEffectivePower()).isEqualTo(6);
                assertThat(opponent.getEffectivePower()).isEqualTo(2);
                assertThat(gqs.hasKeyword(gd, opponent, Keyword.TRAMPLE)).isFalse();
                Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
                assertThat(laterCreature.getEffectivePower()).isEqualTo(2);
                assertThat(gqs.hasKeyword(gd, laterCreature, Keyword.TRAMPLE)).isFalse();
                return;
            }
        }
        throw new AssertionError("Pump mode was never selected");
    }

    private void enterOwnCombatAndResolve() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.BEGINNING_OF_COMBAT);
        if (gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class) != null) {
            harness.handlePermanentChosen(player1, player2.getId());
        }
        resolveAllTriggers();
    }
}
