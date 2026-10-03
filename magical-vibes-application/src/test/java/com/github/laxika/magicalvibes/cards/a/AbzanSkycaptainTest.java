package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbzanSkycaptain.class, GrizzlyBears.class, HillGiant.class, Shock.class})
class AbzanSkycaptainTest extends BaseCardTest {

    @Test
    @DisplayName("When Abzan Skycaptain dies, bolster 2 puts counters on the least-tough creature")
    void deathTriggersBolsterTwo() {
        harness.addToBattlefield(player1, new AbzanSkycaptain());
        Permanent leastToughCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent largerCreature = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        destroySkycaptain();
        harness.passBothPriorities();

        assertThat(leastToughCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(largerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(leastToughCreature.getEffectivePower()).isEqualTo(4);
        assertThat(leastToughCreature.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("When creatures are tied for least toughness, bolster 2 lets the controller choose")
    void deathTriggerChoosesAmongTiedCreatures() {
        harness.addToBattlefield(player1, new AbzanSkycaptain());
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        destroySkycaptain();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                harness.getGameData().interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(choice.context()).isEqualTo(
                new MultiPermanentChoiceContext.OwnPermanentCounterPlacement(
                        CounterType.PLUS_ONE_PLUS_ONE, 2));

        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bolster ignores opposing creatures even when they have less toughness")
    void ignoresOpposingCreatures() {
        harness.addToBattlefield(player1, new AbzanSkycaptain());
        Permanent ally = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroySkycaptain();
        harness.passBothPriorities();

        assertThat(ally.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Bolster does nothing when its controller has no creatures left")
    void noCreaturesRemaining() {
        harness.addToBattlefield(player1, new AbzanSkycaptain());
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroySkycaptain();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Abzan Skycaptain");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class)).isNull();
    }

    @Test
    @DisplayName("Bolster chooses among creatures remaining when the death trigger resolves")
    void evaluatesCreaturesAtResolution() {
        harness.addToBattlefield(player1, new AbzanSkycaptain());
        Permanent smaller = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent larger = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        destroySkycaptain();
        assertThat(gd.stack).hasSize(1);
        assertThat(smaller.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, smaller.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(larger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Bolster compares toughness including existing counters")
    void usesEffectiveToughness() {
        harness.addToBattlefield(player1, new AbzanSkycaptain());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());

        destroySkycaptain();
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    private void destroySkycaptain() {
        UUID skycaptainId = harness.getPermanentId(player1, "Abzan Skycaptain");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, skycaptainId);
    }
}
