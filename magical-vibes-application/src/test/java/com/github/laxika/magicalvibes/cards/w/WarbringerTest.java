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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Warbringer");
        harness.assertNotOnBattlefield(player1, "Warbringer");
    }

    @Test
    @DisplayName("Dash schedules its return without an enters-the-battlefield trigger")
    void dashDoesNotCreateAnEtbTrigger() {
        harness.setHand(player1, List.of(new Warbringer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Warbringer");
        assertThat(gd.stack).isEmpty();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Warbringer");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertInHand(player1, "Warbringer");
        harness.assertNotOnBattlefield(player1, "Warbringer");
    }

    @Test
    @DisplayName("A Warbringer on the battlefield reduces another Warbringer's dash cost")
    void reducesAnotherWarbringersDashCost() {
        harness.addToBattlefield(player1, new Warbringer());
        harness.setHand(player1, List.of(new Warbringer()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertNotInHand(player1, "Warbringer");
    }

    @Test
    @DisplayName("An opponent's Warbringer does not reduce dash costs")
    void doesNotReduceOpponentsDashCost() {
        harness.addToBattlefield(player2, new Warbringer());
        harness.setHand(player1, List.of(new ScreamreachBrawler()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (java.util.UUID) null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
        harness.assertInHand(player1, "Screamreach Brawler");
        harness.assertNotOnBattlefield(player1, "Screamreach Brawler");
    }

    @Test
    @DisplayName("Warbringer does not reduce normal casting costs")
    void doesNotReduceNormalCastingCost() {
        harness.addToBattlefield(player1, new Warbringer());
        harness.setHand(player1, List.of(new ScreamreachBrawler()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
        harness.assertInHand(player1, "Screamreach Brawler");
    }

    @Test
    @DisplayName("Multiple Warbringers cannot reduce the colored portion of dash costs")
    void doesNotReduceColoredDashCost() {
        harness.addToBattlefield(player1, new Warbringer());
        harness.addToBattlefield(player1, new Warbringer());
        harness.setHand(player1, List.of(new ScreamreachBrawler()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (java.util.UUID) null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
        harness.assertInHand(player1, "Screamreach Brawler");
        harness.assertNotOnBattlefield(player1, "Screamreach Brawler");
    }

    @Test
    @DisplayName("Normally cast Warbringer has no dash haste or end-step return")
    void normalCastDoesNotApplyDashBenefits() {
        harness.setHand(player1, List.of(new Warbringer()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Warbringer").hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.assertOnBattlefield(player1, "Warbringer");
        harness.assertNotInHand(player1, "Warbringer");
        assertThat(gd.stack).isEmpty();
    }
}
