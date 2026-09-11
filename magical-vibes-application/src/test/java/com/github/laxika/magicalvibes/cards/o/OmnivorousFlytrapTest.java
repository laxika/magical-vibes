package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DemonicCounsel;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OmnivorousFlytrap.class, GrizzlyBears.class, Forest.class, Shock.class,
        Pacifism.class, LeoninScimitar.class, DemonicCounsel.class})
class OmnivorousFlytrapTest extends BaseCardTest {

    @Test
    @DisplayName("ETB distributes two counters when delirium is met")
    void etbDistributesCountersWithDelirium() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, fourCardTypes());

        castFlytrap();
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Attacking with six graveyard card types doubles the distributed counters")
    void attackDoublesDistributedCountersWithSixCardTypes() {
        Permanent flytrap = addCreatureReady(player1, new OmnivorousFlytrap());
        Permanent firstTarget = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondTarget = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, sixCardTypes());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, firstTarget.getId());
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(flytrap.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(firstTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(secondTarget.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("The attack ability does not trigger without delirium")
    void attackDoesNotTriggerWithoutDelirium() {
        addCreatureReady(player1, new OmnivorousFlytrap());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new Forest(), new Shock()));

        declareAttackers(List.of(0));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castFlytrap() {
        harness.setHand(player1, List.of(new OmnivorousFlytrap()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }

    private List<com.github.laxika.magicalvibes.model.Card> fourCardTypes() {
        return List.of(new GrizzlyBears(), new Forest(), new Shock(), new Pacifism());
    }

    private List<com.github.laxika.magicalvibes.model.Card> sixCardTypes() {
        return List.of(new GrizzlyBears(), new Forest(), new Shock(), new Pacifism(),
                new LeoninScimitar(), new DemonicCounsel());
    }
}
