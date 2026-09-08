package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShaperParasite.class})
class ShaperParasiteTest extends BaseCardTest {

    @Test
    void turningFaceUpCanGiveItPlusTwoMinusTwoUntilEndOfTurn() {
        Permanent parasite = castFaceDownAndTurnFaceUp();

        harness.handlePermanentChosen(player1, parasite.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Gets +2/-2");

        assertThat(gqs.getEffectivePower(gd, parasite)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, parasite)).isEqualTo(1);
    }

    @Test
    void turningFaceUpCanGiveItMinusTwoPlusTwoUntilEndOfTurn() {
        Permanent parasite = castFaceDownAndTurnFaceUp();

        harness.handlePermanentChosen(player1, parasite.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Gets -2/+2");

        assertThat(gqs.getEffectivePower(gd, parasite)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, parasite)).isEqualTo(5);
    }

    private Permanent castFaceDownAndTurnFaceUp() {
        harness.setHand(player1, List.of(new ShaperParasite()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent parasite = findPermanent(player1, "Shaper Parasite");
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(parasite));
        return parasite;
    }
}
