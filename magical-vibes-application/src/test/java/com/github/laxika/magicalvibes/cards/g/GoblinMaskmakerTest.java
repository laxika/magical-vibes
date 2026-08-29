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
    void attackingReducesTheNextFaceDownSpellCostThisTurn() {
        addCreatureReady(player1, new GoblinMaskmaker());
        harness.setHand(player1, List.of(new BasilicaStalker()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
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
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
