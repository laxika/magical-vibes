package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ColossodonYearling;
import com.github.laxika.magicalvibes.cards.d.DragonScarredBear;
import com.github.laxika.magicalvibes.cards.f.Flatten;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AvenTactician.class, GrizzlyBears.class, HillGiant.class,
        ColossodonYearling.class, DragonScarredBear.class, Flatten.class})
class AvenTacticianTest extends BaseCardTest {

    @Test
    void entersAndBolstersTheCreatureWithTheLeastToughness() {
        Permanent leastToughCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent largerCreature = addCreatureReady(player1, new HillGiant());

        harness.setHand(player1, List.of(new AvenTactician()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(leastToughCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(largerCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void bolstersItselfWhenItIsTheOnlyCreature() {
        Permanent aven = harness.enterBattlefieldAndReturn(player1, new AvenTactician());

        resolveAllTriggers();

        assertThat(aven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void choosesExactlyOneCreatureAmongThoseTiedForLeastToughness() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        Permanent larger = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        Permanent aven = harness.enterBattlefieldAndReturn(player1, new AvenTactician());

        resolveAllTriggers();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(larger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(aven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void ignoresOpposingCreaturesWithLessToughness() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new DragonScarredBear());
        Permanent larger = harness.addToBattlefieldAndReturn(player1, new ColossodonYearling());
        Permanent aven = harness.enterBattlefieldAndReturn(player1, new AvenTactician());

        resolveAllTriggers();

        assertThat(aven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(larger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void comparesCurrentToughnessWhenTheTriggerResolves() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        Permanent aven = harness.enterBattlefieldAndReturn(player1, new AvenTactician());
        assertThat(gd.stack).hasSize(1);
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        resolveAllTriggers();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(aven.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotDeclineTheMandatoryChoiceWhenCreaturesAreTied() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        harness.enterBattlefieldAndReturn(player1, new AvenTactician());
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(first.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggerStillBolstersAfterAvenLeavesTheBattlefield() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new DragonScarredBear());
        Permanent aven = harness.enterBattlefieldAndReturn(player1, new AvenTactician());
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, aven.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Aven Tactician");
        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void triggerDoesNothingWhenNoControlledCreaturesRemain() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new DragonScarredBear());
        Permanent aven = harness.enterBattlefieldAndReturn(player1, new AvenTactician());
        harness.setHand(player1, List.of(new Flatten()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveInstant(player1, 0, aven.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Aven Tactician");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
