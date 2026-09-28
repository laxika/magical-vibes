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

@CardUsed({SpellpyrePhoenix.class, RakingClaws.class, YokedPlowbeast.class})
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
        harness.setGraveyard(player1, List.of(new SpellpyrePhoenix()));
        harness.setHand(player1, List.of(new YokedPlowbeast(), new YokedPlowbeast()));
        harness.setLibrary(player1, List.of(new SpellpyrePhoenix(), new SpellpyrePhoenix()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 4);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Spellpyre Phoenix");
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
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Spellpyre Phoenix");
    }
}
