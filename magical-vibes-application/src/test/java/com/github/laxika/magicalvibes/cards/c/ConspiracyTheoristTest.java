package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.ThrillingDiscovery;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ConspiracyTheorist.class, GrizzlyBears.class, Shock.class, ThrillingDiscovery.class})
class ConspiracyTheoristTest extends BaseCardTest {

    @Test
    @DisplayName("attacking may pay to discard and draw")
    void attackingMayPayToDiscardAndDraw() {
        addCreatureReady(player1, new ConspiracyTheorist());
        Card discarded = new Shock();
        Card drawn = new GrizzlyBears();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            harness.handleMayAbilityChosen(player1, true);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
            harness.handleCardChosen(player1, 0);
        });

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    @DisplayName("discard trigger exiles a discarded nonland and permits casting it this turn")
    void discardTriggerExilesAndPermitsCastingThisTurn() {
        Permanent source = addCreatureReady(player1, new ConspiracyTheorist());
        Card discarded = new Shock();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getCardsExiledByPermanent(source.getId())).containsExactly(discarded);

        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        int lifeBeforeCast = gd.getLife(player2.getId());
        harness.castFromExile(player1, discarded.getId(), player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBeforeCast - 2);
    }

    @Test
    void decliningAttackPaymentDoesNotDiscardOrDraw() {
        addCreatureReady(player1, new ConspiracyTheorist());
        Card kept = new ConspiracyTheorist();
        Card undrawn = new ConspiracyTheorist();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(undrawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
        });

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void simultaneousDiscardOffersOnlyOneExile() {
        Permanent source = addCreatureReady(player1, new ConspiracyTheorist());
        Card first = new ConspiracyTheorist();
        Card second = new ConspiracyTheorist();
        harness.setHand(player1, List.of(new ThrillingDiscovery(), first, second));
        harness.setLibrary(player1, List.of(
                new ConspiracyTheorist(), new ConspiracyTheorist(), new ConspiracyTheorist()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.castSorcery(player1, 0, 0);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleCardChosen(player1, 0);
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            if (gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class) != null) {
                harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
            }
            resolveAllTriggers();

            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            assertThat(gd.getCardsExiledByPermanent(source.getId())).hasSize(1);
            assertThat(gd.playerGraveyards.get(player1.getId())).containsAnyOf(first, second);
        });
    }

    @Test
    void exiledCardRemainsCastableAfterSourceDies() {
        Permanent source = addCreatureReady(player1, new ConspiracyTheorist());
        Card discarded = new Shock();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.clearPriorityPassed();
            harness.addMana(player1, ManaColor.RED, 2);
            harness.castInstant(player1, 0, source.getId());
            harness.passBothPriorities();
            assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);

            harness.clearPriorityPassed();
            int lifeBefore = gd.getLife(player2.getId());
            harness.castFromExile(player1, discarded.getId(), player2.getId());
            harness.passBothPriorities();
            assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 2);
        });
    }

    @Test
    void decliningExileLeavesDiscardedCardInGraveyard() {
        Permanent source = addCreatureReady(player1, new ConspiracyTheorist());
        Card discarded = new ConspiracyTheorist();
        Card drawn = new ConspiracyTheorist();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, true);
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
            harness.handleMayAbilityChosen(player1, false);
        });

        assertThat(gd.getCardsExiledByPermanent(source.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }
}
