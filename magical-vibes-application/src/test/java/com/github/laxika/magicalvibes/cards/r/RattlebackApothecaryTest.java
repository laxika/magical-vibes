package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RattlebackApothecary.class, Shock.class, GrizzlyBears.class})
class RattlebackApothecaryTest extends BaseCardTest {

    @Test
    @DisplayName("After a crime, the controller can grant menace to a creature they control")
    void grantsChosenMenaceAfterCrime() {
        Permanent apothecary = addCreatureReady(player1, new RattlebackApothecary());
        harness.addToBattlefield(player2, new GrizzlyBears());

        commitCrime();
        harness.handleListChoice(player1, "MENACE");

        assertThat(gqs.hasKeyword(gd, apothecary, Keyword.MENACE)).isTrue();
        assertThat(gqs.hasKeyword(gd, apothecary, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("After a crime, the controller can grant lifelink to a creature they control")
    void grantsChosenLifelinkAfterCrime() {
        Permanent apothecary = addCreatureReady(player1, new RattlebackApothecary());

        commitCrime();
        harness.handleListChoice(player1, "LIFELINK");

        assertThat(gqs.hasKeyword(gd, apothecary, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, apothecary, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The chosen keyword wears off at the end of the turn")
    void chosenKeywordWearsOffAtEndOfTurn() {
        Permanent apothecary = addCreatureReady(player1, new RattlebackApothecary());

        commitCrime();
        harness.handleListChoice(player1, "MENACE");
        assertThat(gqs.hasKeyword(gd, apothecary, Keyword.MENACE)).isTrue();

        endTurn();

        assertThat(gqs.hasKeyword(gd, apothecary, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("Targeting yourself does not commit a crime")
    void doesNotTriggerWhenTargetingYourself() {
        Permanent apothecary = addCreatureReady(player1, new RattlebackApothecary());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, apothecary, Keyword.MENACE)).isFalse();
        assertThat(gqs.hasKeyword(gd, apothecary, Keyword.LIFELINK)).isFalse();
    }

    private void commitCrime() {
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    private void endTurn() {
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
