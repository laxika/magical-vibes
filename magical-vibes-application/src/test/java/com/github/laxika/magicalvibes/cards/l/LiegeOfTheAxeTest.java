package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(LiegeOfTheAxe.class)
class LiegeOfTheAxeTest extends BaseCardTest {

    @Test
    void turningFaceUpUntapsIt() {
        harness.setHand(player1, List.of(new LiegeOfTheAxe()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent liege = findPermanent(player1, "Liege of the Axe");
        assertThat(liege.isFaceDown()).isTrue();

        liege.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(liege));
        harness.passBothPriorities();

        assertThat(liege.isFaceDown()).isFalse();
        assertThat(liege.isTapped()).isFalse();
    }
}
