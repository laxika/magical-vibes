package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MalametVeteran.class, GrizzlyBears.class, Opt.class})
class MalametVeteranTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with four permanent cards in the graveyard puts a counter on target creature")
    void attacksWithDescendPutsCounterOnTargetCreature() {
        addReadyVeteran();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Descend does not count nonpermanent or opponent graveyard cards")
    void attacksWithoutFourOwnPermanentCardsDoNotPutCounter() {
        addReadyVeteran();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new Opt()));
        harness.setGraveyard(player2, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Descend is checked again when the attack trigger resolves")
    void descendMustStillBeMetOnResolution() {
        addReadyVeteran();
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private Permanent addReadyVeteran() {
        return addCreatureReady(player1, new MalametVeteran());
    }
}
