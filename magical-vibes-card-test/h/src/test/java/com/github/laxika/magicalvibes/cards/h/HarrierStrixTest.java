package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarrierStrix.class, Forest.class, GrizzlyBears.class, Island.class})
class HarrierStrixTest extends BaseCardTest {

    @Test
    @DisplayName("ETB taps target permanent")
    void etbTapsTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new HarrierStrix()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activated ability draws a card, then prompts for a discard")
    void activatedAbilityDrawsThenDiscards() {
        addReadyHarrierStrix(player1);
        Card discarded = new GrizzlyBears();
        Card drawn = new Island();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
    }

    @Test
    @DisplayName("ETB can tap a creature controlled by its controller")
    void etbCanTapOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new HarrierStrix());
        harness.setHand(player1, List.of(new HarrierStrix()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("ETB can target an already tapped permanent")
    void etbCanTargetTappedPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.tap();
        harness.setHand(player1, List.of(new HarrierStrix()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The controller can discard the card just drawn")
    void canDiscardNewlyDrawnCard() {
        harness.addToBattlefield(player1, new HarrierStrix());
        Card kept = new Forest();
        Card drawn = new Island();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept, drawn);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(drawn);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A tapped, summoning sick Strix can activate twice on the opponent's turn")
    void canActivateRepeatedlyWhileTappedAndSummoningSickOnOpponentsTurn() {
        Permanent harrier = harness.addToBattlefieldAndReturn(player1, new HarrierStrix());
        harrier.tap();
        harrier.setSummoningSick(true);
        Card kept = new Forest();
        Card firstDraw = new Island();
        Card secondDraw = new Island();
        harness.setHand(player1, List.of(kept));
        harness.setLibrary(player1, List.of(firstDraw, secondDraw));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(firstDraw, secondDraw);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(harrier.isTapped()).isTrue();
        assertThat(harrier.isSummoningSick()).isTrue();
    }

    private Permanent addReadyHarrierStrix(Player player) {
        Permanent harrier = harness.addToBattlefieldAndReturn(player, new HarrierStrix());
        harrier.setSummoningSick(false);
        return harrier;
    }

}
