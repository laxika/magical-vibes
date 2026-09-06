package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({AllSeeingArbiter.class, Censor.class, Forest.class, GrizzlyBears.class, Shock.class})
class AllSeeingArbiterTest extends BaseCardTest {

    @Test
    @DisplayName("Entering the battlefield draws two cards, then discards a card")
    void entersDrawsTwoThenDiscards() {
        harness.setHand(player1, List.of(new AllSeeingArbiter()));
        harness.setLibrary(player1, List.of(new Forest(), new Shock()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Attacking draws two cards, then discards a card")
    void attacksDrawsTwoThenDiscards() {
        addCreatureReady(player1, new AllSeeingArbiter());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Shock()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Discarding a card gives an opponent creature -X/-0 for distinct graveyard mana values")
    void discardShrinksOpponentCreatureByDistinctManaValues() {
        harness.addToBattlefield(player1, new AllSeeingArbiter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new GrizzlyBears(), new Shock()));
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-1);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The discard debuff remains through the turn and expires on the controller's next turn")
    void discardDebuffExpiresOnNextTurn() {
        harness.addToBattlefield(player1, new AllSeeingArbiter());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest(), new Shock(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Censor()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-1);

        endTurn(player1);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-1);
        endTurn(player2);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    private void endTurn(com.github.laxika.magicalvibes.model.Player activePlayer) {
        harness.setHand(activePlayer, List.of());
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        for (int step = 0; step < 10 && activePlayer.getId().equals(gd.activePlayerId); step++) {
            harness.clearPriorityPassed();
            harness.passBothPriorities();
        }
    }
}
