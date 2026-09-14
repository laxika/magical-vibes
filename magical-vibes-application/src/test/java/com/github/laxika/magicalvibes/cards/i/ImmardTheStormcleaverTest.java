package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ImmardTheStormcleaver.class)
class ImmardTheStormcleaverTest extends BaseCardTest {

    private static final String PUT_CHARGE_COUNTER = "Put a charge counter on Immard";
    private static final String REMOVE_CHARGE_COUNTER = "Remove a charge counter from Immard";
    private static final String DEAL_DAMAGE = "Immard deals 4 damage to any target";
    private static final String GRANT_KEYWORDS =
            "Immard gains lifelink and indestructible until end of turn";

    @Test
    @DisplayName("Entering the battlefield can put a charge counter on Immard")
    void enteringCanPutChargeCounter() {
        harness.setHand(player1, List.of(new ImmardTheStormcleaver()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, PUT_CHARGE_COUNTER);
        harness.passBothPriorities();

        Permanent immard = findPermanent(player1, "Immard, the Stormcleaver");
        assertThat(immard.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Attacking can remove a charge counter and deal 4 damage to any target")
    void attackingRemovesCounterAndDealsDamage() {
        Permanent immard = addCreatureReady(player1, new ImmardTheStormcleaver());
        immard.setCounterCount(CounterType.CHARGE, 1);
        int lifeBefore = gd.getLife(player2.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, REMOVE_CHARGE_COUNTER);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, DEAL_DAMAGE);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(immard.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.getLife(player2.getId())).isEqualTo(lifeBefore - 8);
    }

    @Test
    @DisplayName("Removing a counter can grant lifelink and indestructible until end of turn")
    void attackingCanGrantKeywordsUntilEndOfTurn() {
        Permanent immard = addCreatureReady(player1, new ImmardTheStormcleaver());
        immard.setCounterCount(CounterType.CHARGE, 1);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, REMOVE_CHARGE_COUNTER);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, GRANT_KEYWORDS);

        assertThat(gqs.hasKeyword(gd, immard, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, immard, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, immard, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, immard, Keyword.INDESTRUCTIBLE)).isFalse();
    }
}
