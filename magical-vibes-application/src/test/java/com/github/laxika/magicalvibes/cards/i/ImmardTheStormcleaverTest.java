package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImmardTheStormcleaver.class, GrizzlyBears.class})
class ImmardTheStormcleaverTest extends BaseCardTest {

    private static final String PUT_COUNTER = "Put a charge counter on Immard";
    private static final String REMOVE_COUNTER = "Remove a charge counter from Immard";
    private static final String DAMAGE = "Immard deals 4 damage to any target";
    private static final String KEYWORDS = "Immard gains lifelink and indestructible until end of turn";

    @Test
    void entersAndPutsAChargeCounterOnIt() {
        Permanent immard = castImmard();

        harness.handleListChoice(player1, PUT_COUNTER);
        harness.passBothPriorities();

        assertThat(immard.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    void removingAChargeCounterLetsImmardGainLifelinkAndIndestructible() {
        Permanent immard = addImmardWithChargeCounter(player1, 1);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, REMOVE_COUNTER);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, KEYWORDS);

        assertThat(immard.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gqs.hasKeyword(gd, immard, Keyword.LIFELINK)).isTrue();
        assertThat(gqs.hasKeyword(gd, immard, Keyword.INDESTRUCTIBLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, immard, Keyword.LIFELINK)).isFalse();
        assertThat(gqs.hasKeyword(gd, immard, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void removingAChargeCounterDealsFourDamageToTheChosenTarget() {
        Permanent immard = addImmardWithChargeCounter(player1, 1);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        harness.handleListChoice(player1, REMOVE_COUNTER);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleListChoice(player1, DAMAGE);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(immard.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(target.getMarkedDamage()).isEqualTo(4);
    }

    private Permanent castImmard() {
        harness.setHand(player1, List.of(new ImmardTheStormcleaver()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.WHITE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.RED, 1);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Immard, the Stormcleaver");
    }

    private Permanent addImmardWithChargeCounter(Player player, int counters) {
        Permanent immard = addCreatureReady(player, new ImmardTheStormcleaver());
        immard.setCounterCount(CounterType.CHARGE, counters);
        return immard;
    }
}
