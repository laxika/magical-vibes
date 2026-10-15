package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RiotGear;
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

@CardUsed({BlindObedience.class, Forest.class, GrizzlyBears.class, Ornithopter.class, RiotGear.class})
class BlindObedienceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell offers Extort and paying drains the opponent")
    void payingExtortDrainsOpponent() {
        harness.addToBattlefield(player1, new BlindObedience());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Opponent creatures enter tapped")
    void opponentCreaturesEnterTapped() {
        harness.addToBattlefield(player1, new BlindObedience());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent artifacts enter tapped")
    void opponentArtifactsEnterTapped() {
        harness.addToBattlefield(player1, new BlindObedience());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new Ornithopter(), "{0}");
        harness.passBothPriorities();

        Permanent ornithopter = findPermanent(player2, "Ornithopter");
        assertThat(ornithopter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Opponent lands do not enter tapped")
    void opponentLandsDoNotEnterTapped() {
        harness.addToBattlefield(player1, new BlindObedience());
        harness.setHand(player2, List.of(new Forest()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.playLand(player2, 0);

        Permanent forest = findPermanent(player2, "Forest");
        assertThat(forest.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Controller's creatures enter untapped")
    void controllerCreaturesEnterUntapped() {
        harness.addToBattlefield(player1, new BlindObedience());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Extort payment is chosen only when its trigger resolves")
    void extortPaymentWaitsForResolution() {
        harness.addToBattlefield(player1, new BlindObedience());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castFromHand(player1, new Ornithopter(), "{0}");

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Extort accepts black mana")
    void extortCanBePaidWithBlackMana() {
        harness.addToBattlefield(player1, new BlindObedience());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(findPermanent(player1, "Ornithopter").isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining extort leaves life totals unchanged")
    void decliningExtortDoesNotDrain() {
        harness.addToBattlefield(player1, new BlindObedience());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(findPermanent(player1, "Ornithopter")).isNotNull();
    }

    @Test
    @DisplayName("Two copies of Blind Obedience extort independently")
    void multipleCopiesExtortIndependently() {
        harness.addToBattlefield(player1, new BlindObedience());
        harness.addToBattlefield(player1, new BlindObedience());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castFromHand(player1, new Ornithopter(), "{0}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Opponent noncreature artifacts enter tapped without triggering extort")
    void opponentNoncreatureArtifactsEnterTapped() {
        harness.addToBattlefield(player1, new BlindObedience());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, new RiotGear(), "{2}");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Riot Gear").isTapped()).isTrue();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Opponent creatures put onto the battlefield enter tapped")
    void creaturesEnteringWithoutBeingCastEnterTapped() {
        harness.addToBattlefield(player1, new BlindObedience());

        Permanent bears = harness.enterBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(bears.isTapped()).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
