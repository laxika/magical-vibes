package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MassProduction;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DenryKlinEditorInChief.class, GrizzlyBears.class, MassProduction.class, SwordsToPlowshares.class})
class DenryKlinEditorInChiefTest extends BaseCardTest {

    @Test
    void entersWithChosenCounter() {
        Permanent denry = castDenry("vigilance");

        assertThat(denry.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(denry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(denry.getCounterCount(CounterType.FIRST_STRIKE)).isZero();
    }

    @Test
    void ownEntryTriggersAndDoublesChosenCounter() {
        Permanent denry = castDenry("first strike");

        harness.passBothPriorities();

        assertThat(denry.getCounterCount(CounterType.FIRST_STRIKE)).isEqualTo(2);
    }

    @Test
    void copiesLastKnownCountersAfterDenryLeavesBattlefield() {
        Permanent denry = castDenry("+1/+1");
        denry.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        denry.setCounterCount(CounterType.VIGILANCE, 1);
        castBears();

        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(player1, 0, denry.getId());
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(bears.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
    }

    @Test
    void copiesCountersPresentAtResolutionRatherThanAtEntry() {
        Permanent denry = castDenry("vigilance");
        castBears();
        denry.setCounterCount(CounterType.VIGILANCE, 0);
        denry.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(bears.getCounterCount(CounterType.VIGILANCE)).isZero();
    }

    @Test
    void doesNotTriggerWithoutCountersEvenIfCountersAreAddedLater() {
        Permanent denry = castDenry("+1/+1");
        denry.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        castBears();
        assertThat(gd.stack).isEmpty();
        denry.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void copiesEverySourceCounterKindAndCountToNontokenCreature() {
        Permanent denry = castDenry("+1/+1");
        denry.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        denry.setCounterCount(CounterType.FIRST_STRIKE, 1);
        denry.setCounterCount(CounterType.VIGILANCE, 3);

        castBears();
        harness.passBothPriorities();
        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.FIRST_STRIKE)).isEqualTo(1);
        assertThat(bears.getCounterCount(CounterType.VIGILANCE)).isEqualTo(3);
    }

    @Test
    void interveningIfIsCheckedWhenTriggerResolves() {
        Permanent denry = castDenry("+1/+1");

        castBears();
        denry.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotTriggerForTokens() {
        Permanent denry = castDenry("+1/+1");

        harness.setHand(player1, List.of(new MassProduction()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Soldier")).isNotEmpty()
                .allMatch(token -> token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) == 0);
        assertThat(denry.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private Permanent castDenry(String counterType) {
        harness.setHand(player1, List.of(new DenryKlinEditorInChief()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        PendingInteraction.ColorChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.options()).containsExactly("+1/+1", "first strike", "vigilance");
        harness.handleListChoice(player1, counterType);

        return findPermanent(player1, "Denry Klin, Editor in Chief");
    }

    private void castBears() {
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
