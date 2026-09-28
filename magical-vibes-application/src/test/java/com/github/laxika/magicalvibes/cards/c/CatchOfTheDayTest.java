package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CatchOfTheDay.class, GrizzlyBears.class})
class CatchOfTheDayTest extends BaseCardTest {

    @Test
    void choosesOneModeFromEachIndependentGroup() {
        harness.setHand(player1, List.of(new CatchOfTheDay()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Vigilance");
        harness.handleListChoice(player1, "Scry 2");
        harness.handleListChoice(player1, "4/4");

        Permanent catchOfTheDay = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Catch of the Day"))
                .findFirst()
                .orElseThrow();
        assertThat(catchOfTheDay.getChosenModeLabels()).containsExactlyInAnyOrder("Vigilance", "Scry 2", "4/4");
        assertThat(gqs.getEffectivePower(gd, catchOfTheDay)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, catchOfTheDay)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, catchOfTheDay, Keyword.VIGILANCE)).isTrue();
    }
}
