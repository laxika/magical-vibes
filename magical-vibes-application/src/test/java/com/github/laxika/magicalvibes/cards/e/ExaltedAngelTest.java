package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ExaltedAngel.class})
class ExaltedAngelTest extends BaseCardTest {

    @Test
    void canBeCastFaceDownAndTurnedFaceUpForMorphCost() {
        harness.setHand(player1, List.of(new ExaltedAngel()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent angel = findPermanent(player1, "Exalted Angel");
        assertThat(angel.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(angel));
        harness.passBothPriorities();

        assertThat(angel.isFaceDown()).isFalse();
    }

    @Test
    void gainsLifeEqualToDamageDealt() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        ExaltedAngel card = new ExaltedAngel();
        card.setPower(4);
        Permanent angel = new Permanent(card);
        angel.setSummoningSick(false);
        angel.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(angel);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(14);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
