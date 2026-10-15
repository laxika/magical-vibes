package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NarsetParterOfVeils;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShakedownHeavy.class, Forest.class, NarsetParterOfVeils.class})
class ShakedownHeavyTest extends BaseCardTest {

    @Test
    @DisplayName("The defending player may have its controller draw, untap it, and remove it from combat")
    void defendingPlayerAccepts() {
        Permanent heavy = addCreatureReady(player1, new ShakedownHeavy());
        harness.setLibrary(player1, List.of(new Forest()));
        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();
        int defendingHandBefore = gd.playerHands.get(player2.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(heavy.isTapped()).isTrue();
        assertThat(heavy.isAttacking()).isTrue();

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(defendingHandBefore);
        assertThat(heavy.isTapped()).isFalse();
        assertThat(heavy.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("The defending player may decline")
    void defendingPlayerDeclines() {
        Permanent heavy = addCreatureReady(player1, new ShakedownHeavy());
        harness.setLibrary(player1, List.of(new Forest()));
        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore);
        assertThat(heavy.isTapped()).isTrue();
        assertThat(heavy.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("The attack trigger is on the stack before the defender makes its choice")
    void defenderChoosesOnlyDuringResolution() {
        Permanent heavy = addCreatureReady(player1, new ShakedownHeavy());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(List.of(0));

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(heavy.isAttacking()).isTrue();

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);

        assertThat(heavy.isTapped()).isFalse();
        assertThat(heavy.isAttacking()).isFalse();
    }

    @Test
    @CardUsed({NarsetParterOfVeils.class})
    @DisplayName("The defender cannot choose a draw prohibited by Narset")
    void cannotChooseDrawWhenControllerHasReachedDrawLimit() {
        Permanent heavy = addCreatureReady(player1, new ShakedownHeavy());
        harness.addToBattlefield(player2, new NarsetParterOfVeils());
        Forest card = new Forest();
        harness.setLibrary(player1, List.of(card));
        gd.cardsDrawnThisTurn.put(player1.getId(), 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(card);
        assertThat(heavy.isTapped()).isTrue();
        assertThat(heavy.isAttacking()).isTrue();
    }

    @Test
    @CardUsed({NarsetParterOfVeils.class})
    @DisplayName("Restricting the defender's draws does not prevent choosing the controller's draw")
    void defenderDrawRestrictionDoesNotPreventControllerDraw() {
        Permanent heavy = addCreatureReady(player1, new ShakedownHeavy());
        harness.addToBattlefield(player1, new NarsetParterOfVeils());
        Forest card = new Forest();
        harness.setLibrary(player1, List.of(card));
        gd.cardsDrawnThisTurn.put(player2.getId(), 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(card);
        assertThat(heavy.isTapped()).isFalse();
        assertThat(heavy.isAttacking()).isFalse();
    }
}
