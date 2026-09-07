package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.s.ScreamreachBrawler;
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

@CardUsed({Warbringer.class, ScreamreachBrawler.class})
class WarbringerTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces the generic portion of a controlled creature's dash cost")
    void reducesControlledCreatureDashCost() {
        harness.addToBattlefield(player1, new Warbringer());
        harness.setHand(player1, List.of(new ScreamreachBrawler()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent brawler = findPermanent(player1, "Screamreach Brawler");
        assertThat(brawler.hasKeyword(Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Dash grants haste and returns Warbringer to its owner's hand")
    void dashReturnsWarbringerAtEndStep() {
        harness.setHand(player1, List.of(new Warbringer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent warbringer = findPermanent(player1, "Warbringer");
        assertThat(warbringer.hasKeyword(Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Warbringer");
        harness.assertNotOnBattlefield(player1, "Warbringer");
    }
}
