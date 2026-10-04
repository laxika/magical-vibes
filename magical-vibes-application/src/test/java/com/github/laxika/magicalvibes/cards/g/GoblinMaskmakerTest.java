package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.b.BasilicaStalker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinMaskmaker.class, BasilicaStalker.class})
class GoblinMaskmakerTest extends BaseCardTest {

    @Test
    void attackingReducesFaceDownSpellCostThisTurn() {
        addCreatureReady(player1, new GoblinMaskmaker());
        harness.setHand(player1, List.of(new BasilicaStalker()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent stalker = findPermanent(player1, "Basilica Stalker");
        assertThat(stalker.isFaceDown()).isTrue();
    }

    @Test
    void attackingDoesNotReduceFaceUpSpells() {
        addCreatureReady(player1, new GoblinMaskmaker());
        harness.setHand(player1, List.of(new BasilicaStalker()));
        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reductionAppliesToEveryFaceDownSpellThisTurn() {
        addCreatureReady(player1, new GoblinMaskmaker());
        harness.setHand(player1, List.of(new BasilicaStalker(), new BasilicaStalker()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Basilica Stalker"))
                .hasSize(2)
                .allMatch(Permanent::isFaceDown);
    }

    @Test
    void reductionsFromMultipleAttackersStack() {
        addCreatureReady(player1, new GoblinMaskmaker());
        addCreatureReady(player1, new GoblinMaskmaker());
        harness.setHand(player1, List.of(new BasilicaStalker()));

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Basilica Stalker").isFaceDown()).isTrue();
    }

    @Test
    void reductionDoesNotApplyBeforeAttacking() {
        addCreatureReady(player1, new GoblinMaskmaker());
        harness.setHand(player1, List.of(new BasilicaStalker()));
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reductionDoesNotApplyToOpponentsSpells() {
        addCreatureReady(player1, new GoblinMaskmaker());
        harness.setHand(player2, List.of(new BasilicaStalker()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.forceActivePlayer(player2);
        harness.clearPriorityPassed();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player2, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreatureWithMorph(player2, 0);
        harness.passBothPriorities();
        assertThat(findPermanent(player2, "Basilica Stalker").isFaceDown()).isTrue();
    }

    @Test
    void reductionExpiresAtEndOfTurn() {
        addCreatureReady(player1, new GoblinMaskmaker());
        harness.setHand(player1, List.of(new BasilicaStalker()));
        harness.setLibrary(player1, List.of(new BasilicaStalker()));
        harness.setLibrary(player2, List.of(new BasilicaStalker()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreatureWithMorph(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        assertThat(findPermanent(player1, "Basilica Stalker").isFaceDown()).isTrue();
    }
}
