package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Plaxmanta.class, GrizzlyBears.class})
class PlaxmantaTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your creatures shroud until end of turn when green mana was spent")
    void givesYourCreaturesShroudWhenGreenManaWasSpent() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Plaxmanta()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent plaxmanta = findPermanent(player1, "Plaxmanta");
        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isTrue();
        assertThat(gqs.hasKeyword(gd, plaxmanta, Keyword.SHROUD)).isTrue();
        harness.assertOnBattlefield(player1, "Plaxmanta");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.SHROUD)).isFalse();
        assertThat(gqs.hasKeyword(gd, plaxmanta, Keyword.SHROUD)).isFalse();
    }

    @Test
    @DisplayName("Is sacrificed when green mana was not spent")
    void isSacrificedWithoutGreenMana() {
        harness.setHand(player1, List.of(new Plaxmanta()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Plaxmanta");
        harness.assertInGraveyard(player1, "Plaxmanta");
    }
}
