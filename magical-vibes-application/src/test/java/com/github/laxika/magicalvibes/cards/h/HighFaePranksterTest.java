package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HighFaePrankster.class, GrizzlyBears.class, HillGiant.class})
class HighFaePranksterTest extends BaseCardTest {

    @Test
    void perpetuallyExchangesBasePowerOfTwoTargetCreatures() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        castPrankster();

        harness.handleListChoice(player1,
                "Perpetually exchange target creature's base power with another target creature's base power.");
        harness.handlePermanentChosen(player1, bear.getId());
        harness.handlePermanentChosen(player1, giant.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }

    @Test
    void canDeclineTheOptionalEtbChoice() {
        castPrankster();

        harness.handleListChoice(player1, "Choose no modes");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, findPermanent(player1, "High Fae Prankster"))).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, findPermanent(player1, "High Fae Prankster"))).isEqualTo(4);
    }

    @Test
    void perpetuallySetsPranksterToFourOne() {
        castPrankster();

        harness.handleListChoice(player1,
                "High Fae Prankster perpetually has base power and toughness 4/1.");
        harness.passBothPriorities();

        Permanent prankster = findPermanent(player1, "High Fae Prankster");
        assertThat(gqs.getEffectivePower(gd, prankster)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, prankster)).isEqualTo(1);
    }

    private void castPrankster() {
        harness.setHand(player1, List.of(new HighFaePrankster()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
