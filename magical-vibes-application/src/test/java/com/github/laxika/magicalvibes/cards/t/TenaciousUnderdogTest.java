package com.github.laxika.magicalvibes.cards.t;

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

@CardUsed({TenaciousUnderdog.class, GrizzlyBears.class})
class TenaciousUnderdogTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast does not grant blitz haste or delayed sacrifice")
    void normalCastDoesNotUseBlitz() {
        harness.setHand(player1, List.of(new TenaciousUnderdog()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent underdog = findPermanent(player1, "Tenacious Underdog");
        assertThat(gqs.hasKeyword(gd, underdog, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(underdog);
    }

    @Test
    @DisplayName("Blitz from hand grants haste, draws on death, and sacrifices at the next end step")
    void blitzFromHandGrantsHasteDrawsAndSacrifices() {
        harness.setHand(player1, List.of(new TenaciousUnderdog()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent underdog = findPermanent(player1, "Tenacious Underdog");
        assertThat(gqs.hasKeyword(gd, underdog, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(underdog);
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Tenacious Underdog");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Blitz can be cast from the graveyard")
    void blitzFromGraveyardGrantsHasteAndSacrifices() {
        harness.setGraveyard(player1, List.of(new TenaciousUnderdog()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castFromGraveyard(player1, 0);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        harness.passBothPriorities();

        Permanent underdog = findPermanent(player1, "Tenacious Underdog");
        assertThat(gqs.hasKeyword(gd, underdog, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Tenacious Underdog");
        harness.assertInHand(player1, "Grizzly Bears");
    }
}
