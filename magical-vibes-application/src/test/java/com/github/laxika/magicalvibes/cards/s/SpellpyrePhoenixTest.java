package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RakingClaws;
import com.github.laxika.magicalvibes.cards.y.YokedPlowbeast;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpellpyrePhoenix.class, RakingClaws.class, YokedPlowbeast.class, SliceAndDice.class})
class SpellpyrePhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Returns an instant with cycling from its enter-the-battlefield trigger")
    void returnsTargetInstantWithCycling() {
        RakingClaws rakingClaws = new RakingClaws();
        harness.setGraveyard(player1, List.of(rakingClaws));

        harness.enterBattlefieldAndReturn(player1, new SpellpyrePhoenix());
        harness.handleMultipleCardsChosen(player1, List.of(rakingClaws.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Raking Claws");
    }

    @Test
    @DisplayName("Returns itself from the graveyard at end step after two cards are cycled")
    void returnsItselfAfterTwoCycles() {
        SpellpyrePhoenix phoenix = new SpellpyrePhoenix();
        harness.setGraveyard(player1, List.of(phoenix));
        harness.setHand(player1, List.of(new YokedPlowbeast(), new YokedPlowbeast()));
        harness.setLibrary(player1, List.of(new SpellpyrePhoenix(), new SpellpyrePhoenix()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getId()).contains(phoenix.getId());
        harness.assertNotInGraveyard(player1, "Spellpyre Phoenix");
        assertThat(gd.cardsCycledThisTurn.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not return itself after only one cycle")
    void doesNotReturnAfterOneCycle() {
        harness.setGraveyard(player1, List.of(new SpellpyrePhoenix()));
        harness.setHand(player1, List.of(new YokedPlowbeast()));
        harness.setLibrary(player1, List.of(new SpellpyrePhoenix()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertInGraveyard(player1, "Spellpyre Phoenix");
    }

    @Test
    void canDeclineReturnAfterChoosingTarget() {
        SliceAndDice card = new SliceAndDice();
        harness.setGraveyard(player1, List.of(card));
        harness.enterBattlefieldAndReturn(player1, new SpellpyrePhoenix());
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Slice and Dice");
        harness.assertNotInHand(player1, "Slice and Dice");
    }

    @Test
    void mustChooseTargetEvenWhenReturnIsOptional() {
        harness.setGraveyard(player1, List.of(new SliceAndDice()));
        harness.enterBattlefieldAndReturn(player1, new SpellpyrePhoenix());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsSorceryWithCyclingWhenAccepted() {
        SliceAndDice card = new SliceAndDice();
        harness.setGraveyard(player1, List.of(card));
        harness.enterBattlefieldAndReturn(player1, new SpellpyrePhoenix());
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Slice and Dice");
        harness.assertNotInGraveyard(player1, "Slice and Dice");
    }

    @Test
    void cannotTargetCreatureWithCycling() {
        YokedPlowbeast creature = new YokedPlowbeast();
        harness.setGraveyard(player1, List.of(creature, new SliceAndDice()));
        harness.enterBattlefieldAndReturn(player1, new SpellpyrePhoenix());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Yoked Plowbeast");
    }

    @Test
    void cannotTargetOpponentsCyclingCard() {
        SliceAndDice opponentCard = new SliceAndDice();
        harness.setGraveyard(player1, List.of(new SliceAndDice()));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.enterBattlefieldAndReturn(player1, new SpellpyrePhoenix());

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player2, "Slice and Dice");
    }

    @Test
    void opponentsCyclesDoNotReturnPhoenix() {
        harness.setGraveyard(player1, List.of(new SpellpyrePhoenix()));
        harness.setHand(player2, List.of(new RakingClaws(), new RakingClaws()));
        harness.setLibrary(player2, List.of(new RakingClaws(), new RakingClaws()));
        harness.addMana(player2, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 4);
        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();
        harness.activateHandAbility(player2, 0, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.assertInGraveyard(player1, "Spellpyre Phoenix");
        harness.assertNotInHand(player1, "Spellpyre Phoenix");
    }

    @Test
    void returnsDuringOpponentsEndStepAndCountsEarlierCycles() {
        harness.forceActivePlayer(player2);
        harness.setHand(player1, List.of(new RakingClaws(), new RakingClaws()));
        harness.setLibrary(player1, List.of(new RakingClaws(), new RakingClaws()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 4);
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        SpellpyrePhoenix phoenix = new SpellpyrePhoenix();
        harness.setGraveyard(player1, List.of(phoenix));

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(card -> card.getId()).contains(phoenix.getId());
        harness.assertNotInGraveyard(player1, "Spellpyre Phoenix");
    }
}
