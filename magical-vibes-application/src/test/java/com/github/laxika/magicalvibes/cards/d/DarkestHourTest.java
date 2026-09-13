package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkestHour.class, Forest.class, GrizzlyBears.class, RagingGoblin.class})
class DarkestHourTest extends BaseCardTest {

    @Test
    @DisplayName("Your creatures become black, replacing their colors")
    void recolorsOwnCreatures() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.addToBattlefield(player1, new DarkestHour());

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.BLACK);
        assertThat(gqs.getEffectiveColors(gd, goblin)).doesNotContain(CardColor.RED);
    }

    @Test
    @DisplayName("Opponent's creatures also become black")
    void recolorsOpponentCreatures() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new DarkestHour());

        assertThat(gqs.getEffectiveColors(gd, bear)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("Noncreature permanents are not recolored")
    void doesNotRecolorNoncreatures() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addToBattlefield(player1, new DarkestHour());

        assertThat(gqs.getEffectiveColors(gd, forest)).doesNotContain(CardColor.BLACK);
    }

    @Test
    @DisplayName("Creatures entering after Darkest Hour are also black")
    void recolorsCreaturesEnteringLater() {
        harness.addToBattlefield(player1, new DarkestHour());
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new RagingGoblin());

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.BLACK);
    }

    @Test
    @DisplayName("Full flow: cast and resolve, then all creatures are black")
    void fullFlow() {
        Permanent goblin = harness.addToBattlefieldAndReturn(player1, new RagingGoblin());
        harness.setHand(player1, List.of(new DarkestHour()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, goblin)).containsExactly(CardColor.BLACK);
    }
}
