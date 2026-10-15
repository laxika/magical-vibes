package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarduThunderkite.class, GrizzlyBears.class})
class MarduThunderkiteTest extends BaseCardTest {

    @Test
    @DisplayName("ETB choice perpetually grants the chosen keyword to your creatures")
    void etbChoiceGrantsMenaceToYourCreatures() {
        Permanent ownBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new MarduThunderkite()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "Menace");
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent thunderkite = findPermanent(player1, "Mardu Thunderkite");
        assertThat(gqs.hasKeyword(gd, ownBear, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, thunderkite, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingBear, Keyword.MENACE)).isFalse();
    }
}
